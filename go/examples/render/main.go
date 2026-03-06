package main

import (
	"context"
	"fmt"
	"log"
	"os"

	impossiblefx "github.com/impossiblesoftware/impossible-fx-sdk/v3/go"
)

func main() {
	apiKey := os.Getenv("IMPOSSIBLE_FX_API_KEY")

	client := impossiblefx.NewClient("us-east-1", impossiblefx.WithAPIKey(apiKey))

	ctx := context.Background()

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

	// Alternatively, build the URL from a token manually.
	url := client.GetURL(result.Token, "mp4")
	fmt.Printf("Built URL: %s\n", url)
}
