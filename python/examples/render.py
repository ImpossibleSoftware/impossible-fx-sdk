"""Simple render example using the Impossible FX SDK."""

from impossible_fx import ImpossibleFX

# Create a client (use api_key for authenticated requests)
client = ImpossibleFX(region="us-east-1", api_key="your-api-key")

# Render a movie with template parameters
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

# Alternatively, create a token first and build the URL yourself
token_result = client.create_token(
    project_id="my-project",
    movie="intro",
    params={"title": "Async render"},
)

url = client.get_url(token_result.token, format="mp4")
print(f"\nAsync URL: {url}")

# Check render progress
progress = client.get_progress(token_result.token)
print(f"Progress: {progress.done}/{progress.total}")

client.close()
