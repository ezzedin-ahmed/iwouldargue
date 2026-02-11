use std::sync::Arc;

use axum::{Json, extract::State, http::StatusCode};
use livekit_api::services::egress::EgressOutput;
use livekit_api::services::room::CreateRoomOptions;
use livekit_protocol::EncodedFileOutput;

use crate::{dtos::CreateDiscussionRequest, state::AppState};

pub async fn create_discussion(
    State(state): State<Arc<AppState>>,
    Json(payload): Json<CreateDiscussionRequest>,
) -> StatusCode {
    let room_name = match state
        .room_client
        .create_room(&payload.title, CreateRoomOptions::default())
        .await
    {
        Ok(r) => r.name,
        Err(e) => {
            tracing::error!("{}", e);
            return StatusCode::BAD_REQUEST;
        }
    };

    if payload.is_recorded
        && let Err(e) = tokio::spawn(async move {
            // let discussion_id = payload.discussion_id.clone();
            let records_dir = state.records_dir.clone();
            if let Err(e) = state
                .egress_client
                .start_room_composite_egress(
                    &room_name,
                    vec![EgressOutput::File(EncodedFileOutput {
                        file_type: 3,
                        filepath: records_dir,
                        ..Default::default()
                    })],
                    Default::default(),
                )
                .await
            {
                tracing::error!("{}", e);
            };
        })
        .await
    {
        tracing::error!("{}", e);
        return StatusCode::BAD_REQUEST;
    }

    StatusCode::OK
}
