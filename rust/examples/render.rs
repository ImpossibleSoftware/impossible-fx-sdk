use std::collections::HashMap;

use impossible_fx_sdk::{ImpossibleFX, RenderOptions};

#[tokio::main]
async fn main() -> impossible_fx_sdk::Result<()> {
    // Create a client with an API key.
    let client = ImpossibleFX::builder("us-east-1")
        .api_key("your-api-key")
        .build()?;

    // Build render parameters.
    let mut params = HashMap::new();
    params.insert("title".to_string(), serde_json::json!("Hello, World!"));
    params.insert("color".to_string(), serde_json::json!("#ff0000"));

    // Create a token for the project.
    let token_result = client
        .create_token("my-project", "intro", &params, None)
        .await?;
    println!("Token: {}", token_result.token);

    // Synchronous render — waits for completion.
    let result = client
        .render(
            "my-project",
            "intro",
            &params,
            Some(RenderOptions {
                format: Some("mp4".to_string()),
                ..Default::default()
            }),
        )
        .await?;

    println!("Token:   {}", result.token);
    println!("URL:     {}", result.url);
    println!("Expires: {}", result.expires);
    println!("Status:  {}", result.status);

    // Async render — returns immediately, poll for progress.
    let async_result = client
        .render(
            "my-project",
            "intro",
            &params,
            Some(RenderOptions {
                format: Some("mp4".to_string()),
                async_: Some(true),
                ..Default::default()
            }),
        )
        .await?;

    let url = client.get_url(&async_result.token, "mp4");
    println!("\nAsync URL: {}", url);

    // Poll for progress (only available for async renders).
    let progress = client.get_progress(&async_result.token).await?;
    println!("Progress: {}/{}", progress.done, progress.total);

    Ok(())
}
