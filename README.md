# Impossible FX SDK v3

Official SDK for the Impossible FX Render API. Available for TypeScript/Node.js, Python, Java, Go, and Rust.

## Quick Start

### TypeScript / Node.js

```typescript
import { ImpossibleFX } from '@impossible/fx-sdk';

const client = new ImpossibleFX({ region: 'us-east-1', apiKey: 'your-api-key' });
const result = await client.render('your-project-id', 'movie-name', {
  title: 'Hello World',
  color: '#ff0000',
});

console.log(result.url);
```

### Python

```python
from impossible_fx import ImpossibleFX

client = ImpossibleFX(region="us-east-1", api_key="your-api-key")
result = client.render("your-project-id", "movie-name", {
    "title": "Hello World",
    "color": "#ff0000",
})

print(result.url)
```

### Java

```java
import io.impossible.fx.ImpossibleFX;
import java.util.Map;

ImpossibleFX client = new ImpossibleFX.Builder()
    .region("us-east-1")
    .apiKey("your-api-key")
    .build();

RenderResult result = client.render("your-project-id", "movie-name",
    Map.of("title", "Hello World", "color", "#ff0000"));

System.out.println(result.getUrl());
```

### Go

```go
import impossiblefx "github.com/impossiblesoftware/impossible-fx-sdk/v3/go"

client := impossiblefx.NewClient("us-east-1", impossiblefx.WithAPIKey("your-api-key"))
result, err := client.Render(ctx, "your-project-id", "movie-name",
    map[string]any{"title": "Hello World", "color": "#ff0000"})

fmt.Println(result.URL)
```

### Rust

```rust
use impossible_fx_sdk::ImpossibleFX;
use std::collections::HashMap;

let client = ImpossibleFX::builder("us-east-1")
    .api_key("your-api-key")
    .build()?;

let mut params = HashMap::new();
params.insert("title".into(), serde_json::json!("Hello World"));
params.insert("color".into(), serde_json::json!("#ff0000"));

let result = client.render("your-project-id", "movie-name", &params, None).await?;
println!("{}", result.url);
```

## API Reference

All SDKs implement the same API surface:

### Core Render

| Method | Description |
|--------|-------------|
| `render(projectId, movie, params, options?)` | Render a movie and return the media URL |
| `createToken(projectId, movie, params, options?)` | Create a render token without starting the render |
| `getUrl(token, format?)` | Construct the media URL for a token |
| `getProgress(token)` | Poll render progress |

### Batch Operations

| Method | Description |
|--------|-------------|
| `createBatch(routing?)` | Create a new batch queue |
| `addBatchTasks(batchId, tasks)` | Add render tasks to a batch |
| `runBatch(batchId)` | Start batch execution |
| `getBatchStatus(batchId)` | Get batch status |
| `getBatchResults(runId)` | Get results for a batch run |
| `cancelBatch(batchId)` | Cancel a batch |

### Authentication

All clients accept an optional API key. When provided, it is sent as a `Bearer` token in the `Authorization` header.

```
Authorization: Bearer <apiKey>
```

### Base URL

The SDK connects to region-specific endpoints:

```
https://render-{region}.impossible.io/v2
```

## Language-Specific Documentation

Each SDK directory contains its own README with language-specific setup instructions:

- [TypeScript/Node.js](./typescript/)
- [Python](./python/)
- [Java](./java/)
- [Go](./go/)
- [Rust](./rust/)

## License

Copyright Impossible Software.
