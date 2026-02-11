use serde::Deserialize;

#[derive(Clone, Deserialize)]
pub struct CreateDiscussionRequest {
    #[serde(rename = "discussionId")]
    pub discussion_id: String,
    pub title: String,
    #[serde(rename = "isRecorded")]
    pub is_recorded: bool,
}

#[derive(Clone, Deserialize)]
pub struct CreateTokenRequest {
    #[serde(rename = "discussionId")]
    pub discussion_id: String,
    #[serde(rename = "userId")]
    pub user_id: String,
    #[serde(rename = "userName")]
    pub user_name: String,
}

#[derive(Clone, Deserialize)]
pub struct SetParticipantRequest {
    #[serde(rename = "discussionId")]
    pub discussion_id: String,
    #[serde(rename = "attendeeId")]
    pub attendee_id: String,
}
