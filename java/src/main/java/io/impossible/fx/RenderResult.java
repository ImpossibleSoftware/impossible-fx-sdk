package io.impossible.fx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of a render request.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RenderResult {

    @JsonProperty("token")
    private String token;

    @JsonProperty("url")
    private String url;

    @JsonProperty("expires")
    private long expires;

    @JsonProperty("status")
    private int status;

    public RenderResult() {
    }

    /** The render token. */
    public String getToken() {
        return token;
    }

    /** Direct URL to the rendered output. */
    public String getUrl() {
        return url;
    }

    /** Expiration timestamp (Unix seconds). */
    public long getExpires() {
        return expires;
    }

    /** HTTP status code. */
    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "RenderResult{token='" + token + "', url='" + url
                + "', expires=" + expires + ", status=" + status + "}";
    }
}
