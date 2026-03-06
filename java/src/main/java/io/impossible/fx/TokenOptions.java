package io.impossible.fx;

/**
 * Options for creating a render token.
 */
public class TokenOptions {

    private final String routingKey;

    private TokenOptions(Builder builder) {
        this.routingKey = builder.routingKey;
    }

    /** Routing key for render affinity. */
    public String getRoutingKey() {
        return routingKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String routingKey;

        public Builder routingKey(String routingKey) {
            this.routingKey = routingKey;
            return this;
        }

        public TokenOptions build() {
            return new TokenOptions(this);
        }
    }
}
