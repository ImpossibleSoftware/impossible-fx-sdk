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

    // Render a movie with custom options.
    let result = client
        .render(
            "my-project",
            "intro",
            &params,
            Some(RenderOptions {
                format: Some("mp4".to_string()),
                parallel: Some(2),
                ..Default::default()
            }),
        )
        .await?;

    println!("Token:   {}", result.token);
    println!("URL:     {}", result.url);
    println!("Expires: {}", result.expires);
    println!("Status:  {}", result.status);

    // You can also create a token and poll for progress.
    let token_result = client
        .create_token("my-project", "intro", &params, None)
        .await?;

    let url = client.get_url(&token_result.token, "mp4");
    println!("\nToken URL: {}", url);

    let progress = client.get_progress(&token_result.token).await?;
    println!("Progress: {}/{}", progress.done, progress.total);

    Ok(())
}
