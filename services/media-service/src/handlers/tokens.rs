use std::sync::Arc;

use anyhow::Result;
use axum::{Json, extract::State, http::StatusCode};
use livekit_api::{access_token::VideoGrants, services::room::UpdateParticipantOptions};
use livekit_protocol::ParticipantPermission;

use crate::{
    dtos::{CreateTokenRequest, SetParticipantRequest},
    state::AppState,
    utils::create_token,
};

pub async fn create_host_token(
    State(state): State<Arc<AppState>>,
    Json(payload): Json<CreateTokenRequest>,
) -> (StatusCode, String) {
    let grants = VideoGrants {
        room_create: true,
        room_list: false,
        room_record: false,
        room_admin: true,
        room_join: true,
        room: payload.discussion_id.clone(),
        destination_room: payload.discussion_id,
        can_publish: true,
        can_subscribe: true,
        can_publish_data: true,
        can_update_own_metadata: false,
        ingress_admin: false,
        hidden: false,
        recorder: false,
        ..Default::default()
    };
    wrap_token_response(create_token(&payload.user_id, &payload.user_name, grants, &state).await)
        .await
}
pub async fn create_participant_token(
    State(state): State<Arc<AppState>>,
    Json(payload): Json<CreateTokenRequest>,
) -> (StatusCode, String) {
    let grants = VideoGrants {
        room_create: false,
        room_list: false,
        room_record: false,
        room_admin: false,
        room_join: true,
        room: payload.discussion_id.clone(),
        destination_room: payload.discussion_id,
        can_publish: true,
        can_subscribe: true,
        can_publish_data: true,
        can_update_own_metadata: false,
        ingress_admin: false,
        hidden: false,
        recorder: false,
        ..Default::default()
    };
    wrap_token_response(create_token(&payload.user_id, &payload.user_name, grants, &state).await)
        .await
}
pub async fn create_attendee_token(
    State(state): State<Arc<AppState>>,
    Json(payload): Json<CreateTokenRequest>,
) -> (StatusCode, String) {
    let grants = VideoGrants {
        room_create: false,
        room_list: false,
        room_record: false,
        room_admin: false,
        room_join: true,
        room: payload.discussion_id.clone(),
        destination_room: payload.discussion_id,
        can_publish: false,
        can_subscribe: true,
        can_publish_data: false,
        can_update_own_metadata: false,
        ingress_admin: false,
        hidden: false,
        recorder: false,
        ..Default::default()
    };
    wrap_token_response(create_token(&payload.user_id, &payload.user_name, grants, &state).await)
        .await
}

pub async fn set_participant(
    State(state): State<Arc<AppState>>,
    Json(payload): Json<SetParticipantRequest>,
) -> StatusCode {
    set_participant_state(&state, &payload.discussion_id, &payload.attendee_id, true).await
}

pub async fn unset_participant(
    State(state): State<Arc<AppState>>,
    Json(payload): Json<SetParticipantRequest>,
) -> StatusCode {
    set_participant_state(&state, &payload.discussion_id, &payload.attendee_id, false).await
}

async fn set_participant_state(
    state: &AppState,
    discussion_id: &str,
    attendee_id: &str,
    value: bool,
) -> StatusCode {
    if let Err(e) = state
        .room_client
        .update_participant(
            discussion_id,
            attendee_id,
            UpdateParticipantOptions {
                permission: Some(ParticipantPermission {
                    can_subscribe: value,
                    can_publish: value,
                    can_publish_data: value,
                    hidden: false,
                    can_update_metadata: false,
                    can_subscribe_metrics: false,
                    ..Default::default()
                }),
                ..Default::default()
            },
        )
        .await
    {
        tracing::error!("{}", e);
        return StatusCode::BAD_REQUEST;
    };
    StatusCode::OK
}

async fn wrap_token_response(result: Result<String>) -> (StatusCode, String) {
    match result {
        Ok(token) => (StatusCode::OK, token),
        Err(e) => {
            tracing::error!("{}", e);
            (
                StatusCode::INTERNAL_SERVER_ERROR,
                "Incorrect claims".to_string(),
            )
        }
    }
}
