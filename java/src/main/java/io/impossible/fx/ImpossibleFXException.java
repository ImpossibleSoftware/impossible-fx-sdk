package io.impossible.fx;

/**
 * Exception thrown when an Impossible FX API request fails.
 */
public class ImpossibleFXException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public ImpossibleFXException(String message) {
        super(message);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public ImpossibleFXException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public ImpossibleFXException(int statusCode, String responseBody) {
        super("API request failed with status " + statusCode + ": " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    /** Returns the HTTP status code, or 0 if not applicable. */
    public int getStatusCode() {
        return statusCode;
    }

    /** Returns the raw response body, or null if not applicable. */
    public String getResponseBody() {
        return responseBody;
    }
}
