use std::sync::Arc;

use livekit_api::{
    access_token::TokenVerifier,
    services::{egress::EgressClient, room::RoomClient},
};

#[derive(Clone, Debug)]
pub struct AppState {
    pub livekit_url: String,
    pub livekit_api_key: String,
    pub livekit_api_secret: String,
    // pub admin_token: String,
    pub room_client: Arc<RoomClient>,
    pub egress_client: Arc<EgressClient>,
    pub token_verifier: Arc<TokenVerifier>,
    pub records_dir: String,
}
