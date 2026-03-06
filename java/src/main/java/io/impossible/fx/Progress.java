package io.impossible.fx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Render progress information.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Progress {

    @JsonProperty("token")
    private String token;

    @JsonProperty("done")
    private int done;

    @JsonProperty("total")
    private int total;

    public Progress() {
    }

    /** The render token. */
    public String getToken() {
        return token;
    }

    /** Number of completed frames/steps. */
    public int getDone() {
        return done;
    }

    /** Total number of frames/steps. */
    public int getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Progress{token='" + token + "', done=" + done
                + ", total=" + total + "}";
    }
}
