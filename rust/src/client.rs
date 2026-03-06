use std::collections::HashMap;

use reqwest::{Client, header};
use serde_json::Value;

use crate::error::{Error, Result};
use crate::types::*;

/// Builder for constructing an [`ImpossibleFX`] client.
pub struct ImpossibleFXBuilder {
    region: String,
    api_key: Option<String>,
    timeout: std::time::Duration,
}

impl ImpossibleFXBuilder {
    /// Create a new builder for the given region.
    pub fn new(region: impl Into<String>) -> Self {
        Self {
            region: region.into(),
            api_key: None,
            timeout: std::time::Duration::from_secs(120),
        }
    }

    /// Set the API key for authenticated requests.
    pub fn api_key(mut self, api_key: impl Into<String>) -> Self {
        self.api_key = Some(api_key.into());
        self
    }

    /// Set the request timeout. Defaults to 120 seconds.
    pub fn timeout(mut self, timeout: std::time::Duration) -> Self {
        self.timeout = timeout;
        self
    }

    /// Build the [`ImpossibleFX`] client.
    pub fn build(self) -> Result<ImpossibleFX> {
        let base_url = format!("https://render-{}.impossible.io/v2", self.region);

        let mut headers = header::HeaderMap::new();
        headers.insert(
            header::CONTENT_TYPE,
            header::HeaderValue::from_static("application/json"),
        );
        if let Some(ref key) = self.api_key {
            let value = header::HeaderValue::from_str(&format!("Bearer {key}"))
                .map_err(Error::InvalidHeader)?;
            headers.insert(header::AUTHORIZATION, value);
        }

        let client = Client::builder()
            .default_headers(headers)
            .timeout(self.timeout)
            .build()?;

        Ok(ImpossibleFX {
            base_url,
            client,
        })
    }
}

/// Client for the Impossible FX render API.
///
/// # Examples
///
/// ```no_run
/// use impossible_fx_sdk::ImpossibleFX;
///
/// let client = ImpossibleFX::builder("us-east-1")
///     .api_key("my-key")
///     .build()
///     .unwrap();
/// ```
pub struct ImpossibleFX {
    base_url: String,
    client: Client,
}

impl ImpossibleFX {
    /// Create a new client builder for the given region.
    pub fn builder(region: impl Into<String>) -> ImpossibleFXBuilder {
        ImpossibleFXBuilder::new(region)
    }

    /// Create a client with default settings and no API key.
    pub fn new(region: impl Into<String>) -> ImpossibleFX {
        ImpossibleFXBuilder::new(region)
            .build()
            .expect("failed to build default client")
    }

    /// Send a request and parse the JSON response.
    async fn request<T: serde::de::DeserializeOwned>(
        &self,
        method: reqwest::Method,
        path: &str,
    ) -> Result<T> {
        let url = format!("{}{}", self.base_url, path);
        let response = self.client.request(method, &url).send().await?;
        self.handle_response(response).await
    }

    /// Send a request with a JSON body and parse the JSON response.
    async fn request_json<T: serde::de::DeserializeOwned>(
        &self,
        method: reqwest::Method,
        path: &str,
        body: &impl serde::Serialize,
    ) -> Result<T> {
        let url = format!("{}{}", self.base_url, path);
        let response = self.client.request(method, &url).json(body).send().await?;
        self.handle_response(response).await
    }

    /// Send a request with a JSON body, expecting no response body.
    async fn request_json_no_response(
        &self,
        method: reqwest::Method,
        path: &str,
        body: &impl serde::Serialize,
    ) -> Result<()> {
        let url = format!("{}{}", self.base_url, path);
        let response = self.client.request(method, &url).json(body).send().await?;
        self.handle_error(response).await
    }

    /// Send a request expecting no response body.
    async fn request_no_response(
        &self,
        method: reqwest::Method,
        path: &str,
    ) -> Result<()> {
        let url = format!("{}{}", self.base_url, path);
        let response = self.client.request(method, &url).send().await?;
        self.handle_error(response).await
    }

    /// Check the response status and deserialize the JSON body.
    async fn handle_response<T: serde::de::DeserializeOwned>(
        &self,
        response: reqwest::Response,
    ) -> Result<T> {
        let status = response.status();
        if !status.is_success() {
            let message = self.extract_error_message(response).await;
            return Err(Error::Api {
                status: status.as_u16(),
                message,
            });
        }
        let body = response.text().await?;
        let value: T = serde_json::from_str(&body)?;
        Ok(value)
    }

    /// Check the response status, discarding the body on success.
    async fn handle_error(&self, response: reqwest::Response) -> Result<()> {
        let status = response.status();
        if !status.is_success() {
            let message = self.extract_error_message(response).await;
            return Err(Error::Api {
                status: status.as_u16(),
                message,
            });
        }
        Ok(())
    }

    /// Try to extract an error message from a response body.
    async fn extract_error_message(&self, response: reqwest::Response) -> String {
        match response.text().await {
            Ok(text) => {
                if let Ok(json) = serde_json::from_str::<Value>(&text) {
                    json.get("message")
                        .or_else(|| json.get("error"))
                        .and_then(|v| v.as_str())
                        .unwrap_or(&text)
                        .to_string()
                } else {
                    text
                }
            }
            Err(_) => "unknown error".to_string(),
        }
    }

    // -- Core Render ---------------------------------------------------------

    /// Render a movie and return the result including the output URL.
    ///
    /// Set `async_` to `Some(true)` to return immediately and poll with
    /// [`get_progress`](Self::get_progress).
    ///
    /// # Arguments
    ///
    /// * `project_id` - The project identifier.
    /// * `movie` - The movie template name.
    /// * `params` - Key-value render parameters.
    /// * `options` - Optional render settings (format, async, routing key).
    pub async fn render(
        &self,
        project_id: &str,
        movie: &str,
        params: &HashMap<String, Value>,
        options: Option<RenderOptions>,
    ) -> Result<RenderResult> {
        let opts = options.unwrap_or_default();
        let mut body = serde_json::json!({
            "movie": movie,
            "params": params,
        });
        let map = body.as_object_mut().unwrap();
        if let Some(ref format) = opts.format {
            map.insert("format".to_string(), Value::String(format.clone()));
        }
        if let Some(true) = opts.async_ {
            map.insert("async".to_string(), Value::Bool(true));
        }
        if let Some(ref routing_key) = opts.routing_key {
            map.insert("routingKey".to_string(), Value::String(routing_key.clone()));
        }

        self.request_json(
            reqwest::Method::POST,
            &format!("/render/{project_id}"),
            &body,
        )
        .await
    }

    /// Create a render token without waiting for the render to complete.
    ///
    /// Use [`get_url`](Self::get_url) to construct the output URL from the
    /// returned token, and [`get_progress`](Self::get_progress) to poll for
    /// render progress.
    ///
    /// # Arguments
    ///
    /// * `project_id` - The project identifier.
    /// * `movie` - The movie template name.
    /// * `params` - Key-value render parameters.
    /// * `options` - Optional settings (routing key).
    pub async fn create_token(
        &self,
        project_id: &str,
        movie: &str,
        params: &HashMap<String, Value>,
        options: Option<TokenOptions>,
    ) -> Result<TokenResult> {
        let opts = options.unwrap_or_default();
        let mut body = serde_json::json!({
            "movie": movie,
            "params": params,
        });
        if let Some(ref routing_key) = opts.routing_key {
            body.as_object_mut()
                .unwrap()
                .insert("routingKey".to_string(), Value::String(routing_key.clone()));
        }

        self.request_json(
            reqwest::Method::POST,
            &format!("/token/{project_id}"),
            &body,
        )
        .await
    }

    /// Construct the output URL for a given render token.
    ///
    /// # Arguments
    ///
    /// * `token` - The render token.
    /// * `format` - Output format (e.g. "mp4", "gif", "png").
    pub fn get_url(&self, token: &str, format: &str) -> String {
        format!("{}/get/{}.{}", self.base_url, token, format)
    }

    /// Get the current render progress for a token.
    ///
    /// Only available for renders started with `async_: Some(true)`.
    ///
    /// # Arguments
    ///
    /// * `token` - The render token.
    pub async fn get_progress(&self, token: &str) -> Result<Progress> {
        self.request(reqwest::Method::GET, &format!("/progress/{token}"))
            .await
    }

    // -- Batch ---------------------------------------------------------------

    /// Create a new batch for grouping multiple render tasks.
    ///
    /// # Arguments
    ///
    /// * `routing` - Optional routing key for batch affinity.
    pub async fn create_batch(&self, routing: Option<&str>) -> Result<BatchInfo> {
        let body = serde_json::json!({
            "routing": routing.unwrap_or("default"),
        });
        self.request_json(reqwest::Method::POST, "/batch", &body)
            .await
    }

    /// Add tasks to an existing batch.
    ///
    /// # Arguments
    ///
    /// * `batch_id` - The batch identifier.
    /// * `tasks` - Slice of render tasks to add.
    pub async fn add_batch_tasks(
        &self,
        batch_id: &str,
        tasks: &[BatchTask],
    ) -> Result<()> {
        let body = serde_json::json!({ "tasks": tasks });
        self.request_json_no_response(
            reqwest::Method::POST,
            &format!("/batch/{batch_id}/tasks"),
            &body,
        )
        .await
    }

    /// Start execution of a batch.
    ///
    /// # Arguments
    ///
    /// * `batch_id` - The batch identifier.
    pub async fn run_batch(&self, batch_id: &str) -> Result<()> {
        self.request_no_response(
            reqwest::Method::POST,
            &format!("/batch/{batch_id}/run"),
        )
        .await
    }

    /// Get the current status of a batch.
    ///
    /// # Arguments
    ///
    /// * `batch_id` - The batch identifier.
    pub async fn get_batch_status(&self, batch_id: &str) -> Result<BatchStatus> {
        self.request(reqwest::Method::GET, &format!("/batch/{batch_id}"))
            .await
    }

    /// Retrieve results for a completed batch run.
    ///
    /// # Arguments
    ///
    /// * `run_id` - The batch run identifier.
    pub async fn get_batch_results(&self, run_id: &str) -> Result<Vec<BatchResult>> {
        self.request(reqwest::Method::GET, &format!("/batch/{run_id}/results"))
            .await
    }

    /// Cancel a running batch.
    ///
    /// # Arguments
    ///
    /// * `batch_id` - The batch identifier.
    pub async fn cancel_batch(&self, batch_id: &str) -> Result<()> {
        self.request_no_response(
            reqwest::Method::POST,
            &format!("/batch/{batch_id}/cancel"),
        )
        .await
    }
}
