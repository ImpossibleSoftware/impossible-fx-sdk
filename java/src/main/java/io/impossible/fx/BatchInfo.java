package io.impossible.fx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about a created batch.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchInfo {

    @JsonProperty("id")
    private String id;

    public BatchInfo() {
    }

    /** The batch identifier. */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "BatchInfo{id='" + id + "'}";
    }
}
