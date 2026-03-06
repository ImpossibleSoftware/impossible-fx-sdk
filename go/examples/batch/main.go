package main

import (
	"context"
	"fmt"
	"log"
	"os"
	"time"

	impossiblefx "github.com/impossiblesoftware/impossible-fx-sdk/v3/go"
)

func main() {
	apiKey := os.Getenv("IMPOSSIBLE_FX_API_KEY")

	client := impossiblefx.NewClient("us-east-1", impossiblefx.WithAPIKey(apiKey))

	ctx := context.Background()

	// Create a batch.
	batch, err := client.CreateBatch(ctx, impossiblefx.WithRouting("default"))
	if err != nil {
		log.Fatalf("CreateBatch failed: %v", err)
	}
	fmt.Printf("Created batch: %s\n", batch.BatchID)

	// Add tasks to the batch.
	tasks := []impossiblefx.BatchTask{
		{Movie: "intro-video", Params: map[string]any{"title": "Clip 1"}, Format: "mp4"},
		{Movie: "intro-video", Params: map[string]any{"title": "Clip 2"}, Format: "mp4"},
		{Movie: "intro-video", Params: map[string]any{"title": "Clip 3"}, Format: "mp4"},
	}
	if err := client.AddBatchTasks(ctx, batch.BatchID, tasks); err != nil {
		log.Fatalf("AddBatchTasks failed: %v", err)
	}
	fmt.Printf("Added %d tasks\n", len(tasks))

	// Run the batch.
	if err := client.RunBatch(ctx, batch.BatchID); err != nil {
		log.Fatalf("RunBatch failed: %v", err)
	}
	fmt.Println("Batch started")

	// Poll for completion.
	for {
		status, err := client.GetBatchStatus(ctx, batch.BatchID)
		if err != nil {
			log.Fatalf("GetBatchStatus failed: %v", err)
		}
		fmt.Printf("Progress: %d/%d (failed: %d, status: %s)\n", status.Done, status.Total, status.Failed, status.Status)

		if status.Status == "completed" || status.Status == "failed" || status.Status == "cancelled" {
			// Fetch results.
			if status.RunID != "" {
				results, err := client.GetBatchResults(ctx, status.RunID)
				if err != nil {
					log.Fatalf("GetBatchResults failed: %v", err)
				}
				for i, r := range results {
					fmt.Printf("Result %d: token=%s status=%s url=%s\n", i, r.Token, r.Status, r.URL)
				}
			}
			break
		}

		time.Sleep(2 * time.Second)
	}

	_ = os.Stdout.Sync()
}
