import io.impossible.fx.ImpossibleFX;
import io.impossible.fx.BatchInfo;
import io.impossible.fx.BatchTask;
import io.impossible.fx.BatchStatus;
import io.impossible.fx.BatchResult;

import java.util.List;
import java.util.Map;

/**
 * Batch render example: create a batch, add tasks, run it, poll for
 * completion, and retrieve results.
 */
public class BatchExample {

    public static void main(String[] args) throws InterruptedException {
        // Create the client
        ImpossibleFX client = new ImpossibleFX.Builder()
                .region("us-east-1")
                .apiKey("your-api-key")
                .build();

        // Create a new batch
        BatchInfo batch = client.createBatch();
        String batchId = batch.getId();
        System.out.println("Created batch: " + batchId);

        // Add tasks to the batch
        List<BatchTask> tasks = List.of(
                new BatchTask("intro", Map.of("title", "Video 1")),
                new BatchTask("intro", Map.of("title", "Video 2")),
                new BatchTask("outro", Map.of("message", "Thanks!"), "gif")
        );
        client.addBatchTasks(batchId, tasks);
        System.out.println("Added " + tasks.size() + " tasks");

        // Start the batch
        client.runBatch(batchId);
        System.out.println("Batch started");

        // Poll for completion
        BatchStatus status;
        do {
            Thread.sleep(2000);
            status = client.getBatchStatus(batchId);
            System.out.println("Status: " + status.getStatus()
                    + " (" + status.getTasks() + " tasks)");
        } while (!"completed".equals(status.getStatus())
                && !"failed".equals(status.getStatus()));

        // Get results
        if ("completed".equals(status.getStatus())) {
            List<BatchResult> results = client.getBatchResults(batchId);
            for (BatchResult result : results) {
                System.out.println("  " + result.getStatus()
                        + " - " + result.getUrl());
            }
        } else {
            System.err.println("Batch failed");
            // Optionally cancel
            // client.cancelBatch(batchId);
        }
    }
}
