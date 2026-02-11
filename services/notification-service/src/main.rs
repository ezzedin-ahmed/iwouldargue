use std::sync::Arc;
use std::{collections::HashMap, env};

use axum::extract::{Path, State};
use axum::http::{HeaderMap, StatusCode};
use axum::routing::post;
use axum::{
    Router,
    response::sse::{Event, KeepAlive, Sse},
    routing::get,
};
use dotenvy::dotenv;
use futures_util::stream::Stream;
use redis::sentinel::{SentinelClient, SentinelServerType};
use std::convert::Infallible;
use tokio::sync::broadcast;

use futures_util::StreamExt;
use serde::{Deserialize, Serialize};
use tokio::{net::TcpListener, sync::RwLock};
use tokio_stream::wrappers::BroadcastStream;

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    tracing_subscriber::fmt::init();
    dotenv()?;

    let worker_id = env::var("WORKER_ID")?;
    let redis_url = env::var("REDIS_URL")?;
    let redis_master = env::var("REDIS_MASTER")?;
    let host = env::var("HOST").unwrap_or("127.0.0.1".to_string());
    let port: u16 = env::var("PORT").unwrap_or("8000".to_string()).parse()?;

    let connections = Arc::new(RwLock::new(HashMap::new()));

    let state = AppState {
        connections: Arc::clone(&connections),
        worker_id: worker_id.clone(),
    };

    let router = Router::new()
        .route("/api/v1/connect/{discussion_id}", get(handle_connect))
        .route("/api/v1/manage/{discussion_id}", post(handle_manage))
        .with_state(state);
    let listener = TcpListener::bind((host.as_ref(), port)).await?;

    tokio::spawn(async move {
        if let Err(e) = axum::serve(listener, router).await {
            tracing::error!("{}", e);
            std::process::exit(1);
        }
    });

    let (tx, mut rx) = tokio::sync::mpsc::channel::<ListenerEvent>(10000);

    let mut conn = SentinelClient::build(
        vec![redis_url],
        redis_master,
        None,
        SentinelServerType::Replica,
    )?;
    let mut conn = conn.get_client()?.get_async_pubsub().await?;
    conn.subscribe(worker_id).await?;
    tokio::spawn(async move {
        let mut msgs = conn.on_message();
        while let Some(msg) = msgs.next().await {
            let Ok(event) = serde_json::from_slice(msg.get_payload_bytes()) else {
                tracing::error!("Event is not serializable: {:?}", msg);
                continue;
            };
            if let Err(e) = tx.send(event).await {
                tracing::error!("No recievers found: {e}")
            }
        }
    });

    while let Some(event) = rx.recv().await {
        let mut map = connections.write().await;
        let Some(tx) = map.get_mut(&event.discussion_id) else {
            tracing::error!(
                "Discussion with ID {} is not managed by this service.",
                event.discussion_id
            );
            continue;
        };
        if let Err(e) = tx.send(event) {
            tracing::error!("No reciever found: {e}");
        }
    }

    Ok(())
}

#[derive(Clone)]
struct AppState {
    connections: Arc<RwLock<DiscussionsChannels>>,
    worker_id: String,
}

#[derive(Serialize, Deserialize, Clone)]
enum EventData {
    Comment { body: String, sender: String },

    Attachment { id: String, sender: String },
}

#[derive(Serialize, Deserialize, Clone)]
struct ListenerEvent {
    data: EventData,
    #[serde(rename = "discussionId")]
    discussion_id: String,
}

type DiscussionsChannels = HashMap<String, broadcast::Sender<ListenerEvent>>;

async fn handle_connect(
    Path(discussion_id): Path<String>,
    State(state): State<AppState>,
) -> Result<Sse<impl Stream<Item = Result<Event, Infallible>>>, (StatusCode, String)> {
    let map = state.connections.read().await;
    let Some(tx) = map.get(&discussion_id) else {
        return Err((
            StatusCode::NOT_FOUND,
            format!("Discussion with ID {discussion_id} is not managed by this service.")
                .to_string(),
        ));
    };

    let rx = tx.subscribe();
    let stream = BroadcastStream::new(rx).filter_map(|event| async move {
        match event {
            Ok(event) => Some(Ok(Event::default()
                .json_data(event)
                .expect("Event is not JSON serializable."))),
            Err(e) => {
                tracing::error!("{}", e);
                None
            }
        }
    });

    Ok(Sse::new(stream).keep_alive(KeepAlive::default()))
}

async fn handle_manage(
    Path(discussion_id): Path<String>,
    State(state): State<AppState>,
) -> (StatusCode, HeaderMap) {
    let mut map = state.connections.write().await;
    let (tx, _) = tokio::sync::broadcast::channel::<ListenerEvent>(100);
    map.insert(discussion_id, tx);

    let mut headers = HeaderMap::new();
    headers.insert("X-Worker-Id", state.worker_id.parse().unwrap());

    (StatusCode::OK, headers)
}
