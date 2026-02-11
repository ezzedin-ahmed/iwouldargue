use anyhow::Result;
use livekit_api::access_token::{AccessToken, VideoGrants};

use crate::state::AppState;

pub async fn create_token(
    host_id: &str,
    host_name: &str,
    grants: VideoGrants,
    state: &AppState,
) -> Result<String> {
    let token = AccessToken::with_api_key(&state.livekit_api_key, &state.livekit_api_secret)
        .with_identity(host_id)
        .with_name(host_name)
        .with_grants(grants)
        .to_jwt()?;
    Ok(token)
}

pub async fn create_admin_token(livekit_api_key: &str, livekit_api_secret: &str) -> Result<String> {
    let grants = VideoGrants {
        room_create: true,
        room_list: true,
        room_record: true,
        room_admin: true,
        // room_join: true,
        can_publish: false,
        can_subscribe: true,
        can_publish_data: true,
        can_update_own_metadata: true,
        ingress_admin: true,
        hidden: true,
        recorder: true,
        ..Default::default()
    };
    let token = AccessToken::with_api_key(livekit_api_key, livekit_api_secret)
        .with_identity("admin")
        .with_name("admin")
        .with_grants(grants)
        .to_jwt()?;
    Ok(token)
}
