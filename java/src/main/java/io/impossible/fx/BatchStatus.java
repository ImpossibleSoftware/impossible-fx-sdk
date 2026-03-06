package io.impossible.fx;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Status of a batch run.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchStatus {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("tasks")
    private int tasks;

    public BatchStatus() {
    }

    /** Batch identifier. */
    public String getId() {
        return id;
    }

    /** Current batch status (e.g. "pending", "running", "completed"). */
    public String getStatus() {
        return status;
    }

    /** Total number of tasks in the batch. */
    public int getTasks() {
        return tasks;
    }

    @Override
    public String toString() {
        return "BatchStatus{id='" + id + "', status='" + status
                + "', tasks=" + tasks + "}";
    }
}
