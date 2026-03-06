package io.impossible.fx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of a token creation request.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResult {

    @JsonProperty("token")
    private String token;

    @JsonProperty("expires")
    private long expires;

    @JsonProperty("status")
    private int status;

    public TokenResult() {
    }

    /** The render token. */
    public String getToken() {
        return token;
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
        return "TokenResult{token='" + token + "', expires=" + expires
                + ", status=" + status + "}";
    }
}
