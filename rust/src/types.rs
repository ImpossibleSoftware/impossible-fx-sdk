use std::collections::HashMap;

use serde::{Deserialize, Serialize};
use serde_json::Value;

/// Options for a render request.
#[derive(Debug, Clone, Default, Serialize)]
pub struct RenderOptions {
    /// Output format (e.g. "mp4", "gif", "png").
    #[serde(skip_serializing_if = "Option::is_none")]
    pub format: Option<String>,

    /// If true, return immediately and use [`ImpossibleFX::get_progress`] to poll.
    #[serde(skip_serializing_if = "Option::is_none", rename = "async")]
    pub async_: Option<bool>,

    /// Routing key for render affinity.
    #[serde(skip_serializing_if = "Option::is_none", rename = "routingKey")]
    pub routing_key: Option<String>,
}

/// Result of a render request.
#[derive(Debug, Clone, Deserialize)]
pub struct RenderResult {
    /// The render token.
    pub token: String,

    /// Direct URL to the rendered output.
    pub url: String,

    /// Expiration timestamp (Unix seconds).
    pub expires: u64,

    /// HTTP status code.
    pub status: u16,
}

/// Options for creating a render token.
#[derive(Debug, Clone, Default, Serialize)]
pub struct TokenOptions {
    /// Routing key for render affinity.
    #[serde(skip_serializing_if = "Option::is_none", rename = "routingKey")]
    pub routing_key: Option<String>,
}

/// Result of a token creation request.
#[derive(Debug, Clone, Deserialize)]
pub struct TokenResult {
    /// The render token.
    pub token: String,

    /// Expiration timestamp (Unix seconds).
    pub expires: u64,

    /// HTTP status code.
    pub status: u16,
}

/// Render progress information.
#[derive(Debug, Clone, Deserialize)]
pub struct Progress {
    /// The render token.
    pub token: String,

    /// Number of completed frames/steps.
    pub done: u64,

    /// Total number of frames/steps.
    pub total: u64,
}

/// Information about a created batch.
#[derive(Debug, Clone, Deserialize)]
pub struct BatchInfo {
    /// The batch identifier.
    pub id: String,
}

/// A single task within a batch render.
#[derive(Debug, Clone, Serialize)]
pub struct BatchTask {
    /// Movie template name.
    pub movie: String,

    /// Render parameters.
    pub params: HashMap<String, Value>,

    /// Output format (e.g. "mp4", "gif", "png").
    #[serde(skip_serializing_if = "Option::is_none")]
    pub format: Option<String>,
}

/// Status of a batch run.
#[derive(Debug, Clone, Deserialize)]
pub struct BatchStatus {
    /// Batch identifier.
    pub id: String,

    /// Current batch status (e.g. "pending", "running", "completed").
    pub status: String,

    /// Total number of tasks in the batch.
    pub tasks: u64,
}

/// Result of a single task within a completed batch.
#[derive(Debug, Clone, Deserialize)]
pub struct BatchResult {
    /// The render token.
    pub token: String,

    /// Direct URL to the rendered output.
    pub url: String,

    /// Task status (e.g. "completed", "failed").
    pub status: String,
}
