package io.impossible.fx;

/**
 * Options for a render request.
 */
public class RenderOptions {

    private final String format;
    private final Integer parallel;
    private final String routingKey;

    private RenderOptions(Builder builder) {
        this.format = builder.format;
        this.parallel = builder.parallel;
        this.routingKey = builder.routingKey;
    }

    /** Output format (e.g. "mp4", "gif", "png"). */
    public String getFormat() {
        return format;
    }

    /** Number of parallel render workers. */
    public Integer getParallel() {
        return parallel;
    }

    /** Routing key for render affinity. */
    public String getRoutingKey() {
        return routingKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String format;
        private Integer parallel;
        private String routingKey;

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder parallel(int parallel) {
            this.parallel = parallel;
            return this;
        }

        public Builder routingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        public RenderOptions build() {
            return new RenderOptions(this);
        }
    }
}
