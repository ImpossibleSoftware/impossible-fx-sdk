package io.impossible.fx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of a single task within a completed batch.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchResult {

    @JsonProperty("token")
    private String token;

    @JsonProperty("url")
    private String url;

    @JsonProperty("status")
    private String status;

    public BatchResult() {
    }

    /** The render token. */
    public String getToken() {
        return token;
    }

    /** Direct URL to the rendered output. */
    public String getUrl() {
        return url;
    }

    /** Task status (e.g. "completed", "failed"). */
    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "BatchResult{token='" + token + "', url='" + url
                + "', status='" + status + "'}";
    }
}
