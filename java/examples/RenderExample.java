import io.impossible.fx.ImpossibleFX;
import io.impossible.fx.RenderOptions;
import io.impossible.fx.RenderResult;
import io.impossible.fx.TokenResult;
import io.impossible.fx.Progress;

import java.util.Map;

/**
 * Simple render example: create a client, render a movie, and print the URL.
 */
public class RenderExample {

    public static void main(String[] args) throws InterruptedException {
        // Create the client
        ImpossibleFX client = new ImpossibleFX.Builder()
                .region("us-east-1")
                .apiKey("your-api-key")
                .build();

        // Create a token for the project
        TokenResult tokenResult = client.createToken("my-project", "intro", Map.of(
                "title", "Hello World"
        ));
        System.out.println("Token: " + tokenResult.getToken());

        // Synchronous render — waits for completion
        RenderResult result = client.render("my-project", "intro", Map.of(
                "title", "Hello World",
                "subtitle", "Rendered with Impossible FX"
        ), RenderOptions.builder().format("mp4").build());

        System.out.println("Token:   " + result.getToken());
        System.out.println("URL:     " + result.getUrl());
        System.out.println("Expires: " + result.getExpires());
        System.out.println("Status:  " + result.getStatus());

        // Async render — returns immediately, poll for progress
        RenderOptions asyncOptions = RenderOptions.builder()
                .format("mp4")
                .async(true)
                .build();

        RenderResult asyncResult = client.render("my-project", "intro", Map.of(
                "title", "Async Render"
        ), asyncOptions);

        System.out.println("\nAsync render started: " + asyncResult.getToken());

        // Build a URL manually from a token
        String url = client.getUrl(asyncResult.getToken(), "mp4");
        System.out.println("Manual URL: " + url);

        // Poll for progress (only available for async renders)
        Progress progress = client.getProgress(asyncResult.getToken());
        while (progress.getDone() < progress.getTotal()) {
            System.out.println("Progress: " + progress.getDone() + "/" + progress.getTotal());
            Thread.sleep(1000);
            progress = client.getProgress(asyncResult.getToken());
        }
        System.out.println("Render finished!");
    }
}
