//! Rust SDK for the Impossible FX v3 render API.
//!
//! # Example
//!
//! ```no_run
//! use std::collections::HashMap;
//! use impossible_fx_sdk::ImpossibleFX;
//!
//! #[tokio::main]
//! async fn main() -> impossible_fx_sdk::Result<()> {
//!     let client = ImpossibleFX::builder("us-east-1")
//!         .api_key("my-key")
//!         .build()?;
//!
//!     let params = HashMap::new();
//!     let result = client.render("my-project", "intro", &params, None).await?;
//!     println!("Rendered: {}", result.url);
//!     Ok(())
//! }
//! ```

mod client;
pub mod error;
pub mod types;

pub use client::{ImpossibleFX, ImpossibleFXBuilder};
pub use error::{Error, Result};
pub use types::*;
