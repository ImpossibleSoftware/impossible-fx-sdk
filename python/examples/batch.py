"""Batch render example using the Impossible FX SDK."""

import time

from impossible_fx import BatchTask, ImpossibleFX

client = ImpossibleFX(region="us-east-1", api_key="your-api-key")

# 1. Create a batch
batch = client.create_batch(routing="default")
print(f"Created batch: {batch.id}")

# 2. Add tasks to the batch
tasks = [
    BatchTask(
        movie="intro",
        params={"title": "Video 1", "color": "#ff0000"},
        format="mp4",
    ),
    BatchTask(
        movie="intro",
        params={"title": "Video 2", "color": "#00ff00"},
        format="mp4",
    ),
    BatchTask(
        movie="intro",
        params={"title": "Video 3", "color": "#0000ff"},
        format="gif",
    ),
]

client.add_batch_tasks(batch.id, tasks)
print(f"Added {len(tasks)} tasks")

# 3. Run the batch
client.run_batch(batch.id)
print("Batch started")

# 4. Poll for completion
while True:
    status = client.get_batch_status(batch.id)
    print(f"Status: {status.status} ({status.tasks} tasks)")

    if status.status in ("completed", "failed", "cancelled"):
        break

    time.sleep(2)

# 5. Get results
if status.status == "completed":
    results = client.get_batch_results(batch.id)
    for i, result in enumerate(results):
        print(f"  Task {i + 1}: {result.status} -> {result.url}")
else:
    print(f"Batch ended with status: {status.status}")

client.close()
