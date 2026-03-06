import io.impossible.fx.ImpossibleFX;
import io.impossible.fx.RenderOptions;
import io.impossible.fx.RenderResult;
import io.impossible.fx.Progress;

import java.util.Map;

/**
 * Simple render example: create a client, render a movie, and print the URL.
 */
public class RenderExample {

    public static void main(String[] args) {
        // Create the client
        ImpossibleFX client = new ImpossibleFX.Builder()
                .region("us-east-1")
                .apiKey("your-api-key")
                .build();

        // Render a movie with default options
        RenderResult result = client.render("my-project", "intro", Map.of(
                "title", "Hello World",
                "subtitle", "Rendered with Impossible FX"
        ));

        System.out.println("Token:   " + result.getToken());
        System.out.println("URL:     " + result.getUrl());
        System.out.println("Expires: " + result.getExpires());
        System.out.println("Status:  " + result.getStatus());

        // Render with options
        RenderOptions options = RenderOptions.builder()
                .format("gif")
                .parallel(4)
                .build();

        RenderResult gifResult = client.render("my-project", "intro", Map.of(
                "title", "Animated"
        ), options);

        System.out.println("\nGIF URL: " + gifResult.getUrl());

        // Build a URL manually from a token
        String url = client.getUrl(result.getToken(), "mp4");
        System.out.println("\nManual URL: " + url);

        // Check render progress
        Progress progress = client.getProgress(result.getToken());
        System.out.println("\nProgress: " + progress.getDone() + "/" + progress.getTotal());
    }
}
