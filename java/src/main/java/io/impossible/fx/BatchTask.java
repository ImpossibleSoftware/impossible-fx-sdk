package io.impossible.fx;

import java.util.Map;

/**
 * A single task within a batch render.
 */
public class BatchTask {

    private final String movie;
    private final Map<String, Object> params;
    private final String format;

    public BatchTask(String movie, Map<String, Object> params) {
        this(movie, params, "mp4");
    }

    public BatchTask(String movie, Map<String, Object> params, String format) {
        this.movie = movie;
        this.params = params;
        this.format = format;
    }

    /** Movie template name. */
    public String getMovie() {
        return movie;
    }

    /** Render parameters. */
    public Map<String, Object> getParams() {
        return params;
    }

    /** Output format (e.g. "mp4", "gif", "png"). */
    public String getFormat() {
        return format;
    }

    @Override
    public String toString() {
        return "BatchTask{movie='" + movie + "', format='" + format + "'}";
    }
}
