package io.impossible.fx;

/**
 * Options for a render request.
 */
public class RenderOptions {

    private final String format;
    private final Boolean async;
    private final String routingKey;

    private RenderOptions(Builder builder) {
        this.format = builder.format;
        this.async = builder.async;
        this.routingKey = builder.routingKey;
    }

    /** Output format (e.g. "mp4", "gif", "png"). */
    public String getFormat() {
        return format;
    }

    /** If true, return immediately and use {@link ImpossibleFX#getProgress} to poll for status. */
    public Boolean getAsync() {
        return async;
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
        private Boolean async;
        private String routingKey;

        public Builder format(String format) {
            this.format = format;
            return this;
        }

        public Builder async(boolean async) {
            this.async = async;
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
