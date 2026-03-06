package io.impossible.fx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Client for the Impossible FX render API.
 *
 * <pre>{@code
 * ImpossibleFX client = new ImpossibleFX.Builder()
 *     .region("us-east-1")
 *     .apiKey("optional-key")
 *     .build();
 *
 * RenderResult result = client.render("my-project", "intro",
 *     Map.of("title", "Hello World"));
 * System.out.println(result.getUrl());
 * }</pre>
 */
public class ImpossibleFX {

    private final String baseUrl;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ImpossibleFX(Builder builder) {
        this.baseUrl = "https://render-" + builder.region + ".impossible.io/v2";
        this.apiKey = builder.apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // ---- Core Render ----

    /**
     * Renders a movie and returns the result including a direct URL.
     *
     * @param projectId the project identifier
     * @param movie     the movie template name
     * @param params    render parameters
     * @return the render result
     */
    public RenderResult render(String projectId, String movie, Map<String, Object> params) {
        return render(projectId, movie, params, null);
    }

    /**
     * Renders a movie with options and returns the result including a direct URL.
     *
     * @param projectId the project identifier
     * @param movie     the movie template name
     * @param params    render parameters
     * @param options   render options (format, parallel, routingKey)
     * @return the render result
     */
    public RenderResult render(String projectId, String movie, Map<String, Object> params,
                               RenderOptions options) {
        Map<String, Object> body = new HashMap<>();
        body.put("movie", movie);
        body.put("params", params);
        if (options != null) {
            if (options.getFormat() != null) body.put("format", options.getFormat());
            if (options.getParallel() != null) body.put("parallel", options.getParallel());
            if (options.getRoutingKey() != null) body.put("routingKey", options.getRoutingKey());
        }
        return post("/render/" + encode(projectId), body, RenderResult.class);
    }

    /**
     * Creates a render token without waiting for the render to complete.
     *
     * @param projectId the project identifier
     * @param movie     the movie template name
     * @param params    render parameters
     * @return the token result
     */
    public TokenResult createToken(String projectId, String movie, Map<String, Object> params) {
        return createToken(projectId, movie, params, null);
    }

    /**
     * Creates a render token with options.
     *
     * @param projectId the project identifier
     * @param movie     the movie template name
     * @param params    render parameters
     * @param options   token options (routingKey)
     * @return the token result
     */
    public TokenResult createToken(String projectId, String movie, Map<String, Object> params,
                                   TokenOptions options) {
        Map<String, Object> body = new HashMap<>();
        body.put("movie", movie);
        body.put("params", params);
        if (options != null && options.getRoutingKey() != null) {
            body.put("routingKey", options.getRoutingKey());
        }
        return post("/token/" + encode(projectId), body, TokenResult.class);
    }

    /**
     * Returns a direct URL for a rendered token in the given format.
     *
     * @param token  the render token
     * @param format the output format (e.g. "mp4", "gif", "png")
     * @return the URL string
     */
    public String getUrl(String token, String format) {
        Objects.requireNonNull(token, "token must not be null");
        Objects.requireNonNull(format, "format must not be null");
        return baseUrl + "/get/" + encode(token) + "." + encode(format);
    }

    /**
     * Returns a direct URL for a rendered token in mp4 format.
     *
     * @param token the render token
     * @return the URL string
     */
    public String getUrl(String token) {
        return getUrl(token, "mp4");
    }

    /**
     * Returns progress information for a render in progress.
     *
     * @param token the render token
     * @return progress information
     */
    public Progress getProgress(String token) {
        Objects.requireNonNull(token, "token must not be null");
        return get("/progress/" + encode(token), Progress.class);
    }

    // ---- Batch ----

    /**
     * Creates a new batch.
     *
     * @return batch information including the batch id
     */
    public BatchInfo createBatch() {
        return createBatch(null);
    }

    /**
     * Creates a new batch with a routing key.
     *
     * @param routing routing key for batch affinity
     * @return batch information including the batch id
     */
    public BatchInfo createBatch(String routing) {
        Map<String, Object> body = new HashMap<>();
        if (routing != null) {
            body.put("routing", routing);
        }
        return post("/batch", body, BatchInfo.class);
    }

    /**
     * Adds tasks to an existing batch.
     *
     * @param batchId the batch identifier
     * @param tasks   list of tasks to add
     */
    public void addBatchTasks(String batchId, List<BatchTask> tasks) {
        Objects.requireNonNull(batchId, "batchId must not be null");
        Objects.requireNonNull(tasks, "tasks must not be null");

        List<Map<String, Object>> taskList = new java.util.ArrayList<>();
        for (BatchTask task : tasks) {
            Map<String, Object> t = new HashMap<>();
            t.put("movie", task.getMovie());
            t.put("params", task.getParams());
            if (task.getFormat() != null) {
                t.put("format", task.getFormat());
            }
            taskList.add(t);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("tasks", taskList);
        postVoid("/batch/" + encode(batchId) + "/tasks", body);
    }

    /**
     * Starts a batch run.
     *
     * @param batchId the batch identifier
     */
    public void runBatch(String batchId) {
        Objects.requireNonNull(batchId, "batchId must not be null");
        postVoid("/batch/" + encode(batchId) + "/run", Map.of());
    }

    /**
     * Returns the current status of a batch.
     *
     * @param batchId the batch identifier
     * @return the batch status
     */
    public BatchStatus getBatchStatus(String batchId) {
        Objects.requireNonNull(batchId, "batchId must not be null");
        return get("/batch/" + encode(batchId), BatchStatus.class);
    }

    /**
     * Returns the results of a completed batch run.
     *
     * @param runId the batch run identifier
     * @return list of batch results
     */
    public List<BatchResult> getBatchResults(String runId) {
        Objects.requireNonNull(runId, "runId must not be null");
        return getList("/batch/" + encode(runId) + "/results", new TypeReference<List<BatchResult>>() {});
    }

    /**
     * Cancels a batch run.
     *
     * @param batchId the batch identifier
     */
    public void cancelBatch(String batchId) {
        Objects.requireNonNull(batchId, "batchId must not be null");
        postVoid("/batch/" + encode(batchId) + "/cancel", Map.of());
    }

    // ---- HTTP helpers ----

    private <T> T get(String path, Class<T> responseType) {
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", "application/json")
                .GET();
        addAuth(reqBuilder);
        return send(reqBuilder.build(), responseType);
    }

    private <T> T getList(String path, TypeReference<T> typeRef) {
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Accept", "application/json")
                .GET();
        addAuth(reqBuilder);
        return sendTyped(reqBuilder.build(), typeRef);
    }

    private <T> T post(String path, Map<String, Object> body, Class<T> responseType) {
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(bodyPublisher(body));
        addAuth(reqBuilder);
        return send(reqBuilder.build(), responseType);
    }

    private void postVoid(String path, Map<String, Object> body) {
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(bodyPublisher(body));
        addAuth(reqBuilder);
        sendVoid(reqBuilder.build());
    }

    private void addAuth(HttpRequest.Builder reqBuilder) {
        if (apiKey != null && !apiKey.isEmpty()) {
            reqBuilder.header("Authorization", "Bearer " + apiKey);
        }
    }

    private HttpRequest.BodyPublisher bodyPublisher(Map<String, Object> body) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(body);
            return HttpRequest.BodyPublishers.ofByteArray(json);
        } catch (IOException e) {
            throw new ImpossibleFXException("Failed to serialize request body", e);
        }
    }

    private <T> T send(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ImpossibleFXException(response.statusCode(), response.body());
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (ImpossibleFXException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImpossibleFXException("Request interrupted", e);
        } catch (Exception e) {
            throw new ImpossibleFXException("Request failed", e);
        }
    }

    private <T> T sendTyped(HttpRequest request, TypeReference<T> typeRef) {
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ImpossibleFXException(response.statusCode(), response.body());
            }
            return objectMapper.readValue(response.body(), typeRef);
        } catch (ImpossibleFXException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImpossibleFXException("Request interrupted", e);
        } catch (Exception e) {
            throw new ImpossibleFXException("Request failed", e);
        }
    }

    private void sendVoid(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ImpossibleFXException(response.statusCode(), response.body());
            }
        } catch (ImpossibleFXException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ImpossibleFXException("Request interrupted", e);
        } catch (Exception e) {
            throw new ImpossibleFXException("Request failed", e);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    // ---- Builder ----

    /**
     * Builder for creating {@link ImpossibleFX} client instances.
     */
    public static class Builder {

        private String region;
        private String apiKey;

        /**
         * Sets the render region (e.g. "us-east-1", "eu-central-1"). Required.
         */
        public Builder region(String region) {
            this.region = region;
            return this;
        }

        /**
         * Sets the API key for authentication. Optional for public projects.
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Builds the client.
         *
         * @return a configured {@link ImpossibleFX} instance
         * @throws IllegalStateException if region is not set
         */
        public ImpossibleFX build() {
            if (region == null || region.isEmpty()) {
                throw new IllegalStateException("region is required");
            }
            return new ImpossibleFX(this);
        }
    }
}
