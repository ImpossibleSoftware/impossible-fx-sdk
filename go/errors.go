package impossiblefx

import "fmt"

// APIError represents an error response from the Impossible FX API.
type APIError struct {
	StatusCode int    `json:"statusCode"`
	Message    string `json:"message"`
	RequestID  string `json:"requestId,omitempty"`
}

// Error implements the error interface.
func (e *APIError) Error() string {
	if e.RequestID != "" {
		return fmt.Sprintf("impossible-fx API error (status %d, request %s): %s", e.StatusCode, e.RequestID, e.Message)
	}
	return fmt.Sprintf("impossible-fx API error (status %d): %s", e.StatusCode, e.Message)
}
