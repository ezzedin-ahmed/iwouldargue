use anyhow::Result;
use axum::{
    Router,
    routing::{post, put},
};
use dotenvy::dotenv;
use livekit_api::{
    access_token::TokenVerifier,
    services::{egress::EgressClient, room::RoomClient},
};
use std::{env, sync::Arc};
use tokio::net::TcpListener;

use media_service::{handlers, state::AppState};

#[tokio::main]
async fn main() -> Result<()> {
    tracing_subscriber::fmt::init();

    dotenv()?;
    let state = {
        let records_dir = env::var("RECORDS_DIR")?;

        let livekit_url = env::var("LIVEKIT_URL")?;
        let livekit_api_key = env::var("LIVEKIT_API_KEY")?;
        let livekit_api_secret = env::var("LIVEKIT_API_SECRET")?;

        let room_client = Arc::new(RoomClient::new(&livekit_url)?);
        let token_verifier = Arc::new(TokenVerifier::with_api_key(
            &livekit_api_key,
            &livekit_api_secret,
        ));

        let egress_client = Arc::new(EgressClient::with_api_key(
            &livekit_url,
            &livekit_api_key,
            &livekit_api_secret,
        ));

        Arc::new(AppState {
            livekit_url,
            livekit_api_key,
            livekit_api_secret,
            room_client,
            egress_client,
            token_verifier,
            records_dir,
        })
    };
    tracing::debug!("State Loaded: {:?}", state);

    let listener = {
        let host = env::var("HOST").unwrap_or("127.0.0.1".to_string());
        let port: u16 = env::var("PORT").unwrap_or("8000".to_string()).parse()?;
        let l = TcpListener::bind((host.as_ref(), port)).await?;
        tracing::debug!("Binded {}{}", host, port);
        l
    };

    let app = Router::new()
        .route("/api/v1/discussions", post(handlers::create_discussion))
        .route("/api/v1/tokens/host", post(handlers::create_host_token))
        .route(
            "/api/v1/tokens/attendee",
            post(handlers::create_attendee_token),
        )
        .route("/api/v1/participants/set", put(handlers::set_participant))
        .route(
            "/api/v1/participants/unset",
            put(handlers::unset_participant),
        )
        .with_state(state);
    tracing::debug!("Server created");

    tracing::info!("Starting listening...");
    axum::serve(listener, app).await?;

    Ok(())
}
