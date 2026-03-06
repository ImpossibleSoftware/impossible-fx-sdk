use thiserror::Error;

/// Errors that can occur when using the Impossible FX SDK.
#[derive(Debug, Error)]
pub enum Error {
    /// An HTTP request failed.
    #[error("HTTP error: {0}")]
    Http(#[from] reqwest::Error),

    /// The API returned a non-success status code.
    #[error("API error (status {status}): {message}")]
    Api {
        /// HTTP status code returned by the server.
        status: u16,
        /// Error message from the response body, if available.
        message: String,
    },

    /// Failed to deserialize the response body.
    #[error("Deserialization error: {0}")]
    Deserialization(#[from] serde_json::Error),

    /// Invalid header value.
    #[error("Invalid header value: {0}")]
    InvalidHeader(#[from] reqwest::header::InvalidHeaderValue),
}

/// A `Result` alias where the error type is [`Error`].
pub type Result<T> = std::result::Result<T, Error>;
