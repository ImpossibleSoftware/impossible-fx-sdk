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

	// Create a token for the project
	tokenResult, err := client.CreateToken(ctx, "my-project", "intro-video", map[string]any{
		"title": "Hello World",
	})
	if err != nil {
		log.Fatalf("CreateToken failed: %v", err)
	}
	fmt.Printf("Token: %s\n", tokenResult.Token)

	// Synchronous render — waits for completion
	result, err := client.Render(ctx, "my-project", "intro-video", map[string]any{
		"title":    "Hello World",
		"subtitle": "Built with Impossible FX",
	}, impossiblefx.WithFormat("mp4"))
	if err != nil {
		log.Fatalf("Render failed: %v", err)
	}

	fmt.Printf("Token:   %s\n", result.Token)
	fmt.Printf("URL:     %s\n", result.URL)
	fmt.Printf("Status:  %s\n", result.Status)
	fmt.Printf("Expires: %s\n", result.Expires)

	// Async render — returns immediately, poll for progress
	asyncResult, err := client.Render(ctx, "my-project", "intro-video", map[string]any{
		"title": "Async Render",
	}, impossiblefx.WithFormat("mp4"), impossiblefx.WithAsync())
	if err != nil {
		log.Fatalf("Async render failed: %v", err)
	}

	fmt.Printf("\nAsync render started: %s\n", asyncResult.Token)

	url := client.GetURL(asyncResult.Token, "mp4")
	fmt.Printf("Built URL: %s\n", url)

	// Poll for progress (only available for async renders)
	for {
		progress, err := client.GetProgress(ctx, asyncResult.Token)
		if err != nil {
			log.Fatalf("GetProgress failed: %v", err)
		}
		fmt.Printf("Progress: %d/%d\n", progress.Done, progress.Total)
		if progress.Done >= progress.Total {
			break
		}
		time.Sleep(1 * time.Second)
	}
	fmt.Println("Render finished!")
}
