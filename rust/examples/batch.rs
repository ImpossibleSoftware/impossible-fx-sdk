use std::collections::HashMap;

use impossible_fx_sdk::{BatchTask, ImpossibleFX};

#[tokio::main]
async fn main() -> impossible_fx_sdk::Result<()> {
    let client = ImpossibleFX::builder("us-east-1")
        .api_key("your-api-key")
        .build()?;

    // Create a batch.
    let batch = client.create_batch(None).await?;
    println!("Created batch: {}", batch.id);

    // Add tasks to the batch.
    let tasks = vec![
        BatchTask {
            movie: "intro".to_string(),
            params: HashMap::from([
                ("title".to_string(), serde_json::json!("Video 1")),
            ]),
            format: Some("mp4".to_string()),
        },
        BatchTask {
            movie: "intro".to_string(),
            params: HashMap::from([
                ("title".to_string(), serde_json::json!("Video 2")),
            ]),
            format: Some("mp4".to_string()),
        },
        BatchTask {
            movie: "outro".to_string(),
            params: HashMap::from([
                ("message".to_string(), serde_json::json!("Thanks!")),
            ]),
            format: None,
        },
    ];

    client.add_batch_tasks(&batch.id, &tasks).await?;
    println!("Added {} tasks", tasks.len());

    // Start the batch.
    client.run_batch(&batch.id).await?;
    println!("Batch started");

    // Poll for status until complete.
    loop {
        let status = client.get_batch_status(&batch.id).await?;
        println!("Status: {} ({} tasks)", status.status, status.tasks);

        if status.status == "completed" || status.status == "failed" {
            break;
        }

        tokio::time::sleep(std::time::Duration::from_secs(2)).await;
    }

    // Get results.
    let results = client.get_batch_results(&batch.id).await?;
    for (i, result) in results.iter().enumerate() {
        println!("Task {}: status={}, url={}", i, result.status, result.url);
    }

    Ok(())
}
