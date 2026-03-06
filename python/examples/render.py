"""Simple render example using the Impossible FX SDK."""

import time

from impossible_fx import ImpossibleFX

# Create a client (use api_key for authenticated requests)
client = ImpossibleFX(region="us-east-1", api_key="your-api-key")

# Create a token for the project
token_result = client.create_token(
    project_id="my-project",
    movie="intro",
    params={"title": "Hello World"},
)
print(f"Token: {token_result.token}")

# Synchronous render — waits for completion and returns the URL
result = client.render(
    project_id="my-project",
    movie="intro",
    params={
        "title": "Hello World",
        "subtitle": "Generated with Impossible FX",
    },
    format="mp4",
)

print(f"Token:   {result.token}")
print(f"URL:     {result.url}")
print(f"Expires: {result.expires}")
print(f"Status:  {result.status}")

# Async render — returns immediately, poll for progress
async_result = client.render(
    project_id="my-project",
    movie="intro",
    params={"title": "Async render"},
    format="mp4",
    async_=True,
)

url = client.get_url(async_result.token, format="mp4")
print(f"\nAsync URL: {url}")

# Poll for progress (only available for async renders)
progress = client.get_progress(async_result.token)
while progress.done < progress.total:
    print(f"Progress: {progress.done}/{progress.total}")
    time.sleep(1)
    progress = client.get_progress(async_result.token)
print("Render finished!")

client.close()
