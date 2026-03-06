package impossiblefx

import "time"

// RenderResult holds the response from a Render call.
type RenderResult struct {
	Token   string    `json:"token"`
	URL     string    `json:"url"`
	Expires time.Time `json:"expires"`
	Status  string    `json:"status"`
}

// TokenResult holds the response from a CreateToken call.
type TokenResult struct {
	Token   string    `json:"token"`
	Expires time.Time `json:"expires"`
	Status  string    `json:"status"`
}

// Progress holds the response from a GetProgress call.
type Progress struct {
	Token string `json:"token"`
	Done  int    `json:"done"`
	Total int    `json:"total"`
}

// BatchTask represents a single task within a batch.
type BatchTask struct {
	Movie  string         `json:"movie"`
	Params map[string]any `json:"params,omitempty"`
	Format string         `json:"format,omitempty"`
}

// BatchInfo holds the response from a CreateBatch call.
type BatchInfo struct {
	BatchID string `json:"batchId"`
	Status  string `json:"status"`
}

// BatchStatus holds the response from a GetBatchStatus call.
type BatchStatus struct {
	BatchID string `json:"batchId"`
	RunID   string `json:"runId"`
	Status  string `json:"status"`
	Total   int    `json:"total"`
	Done    int    `json:"done"`
	Failed  int    `json:"failed"`
}

// BatchResult holds a single result from a completed batch run.
type BatchResult struct {
	Token  string `json:"token"`
	URL    string `json:"url"`
	Movie  string `json:"movie"`
	Status string `json:"status"`
	Error  string `json:"error,omitempty"`
}

// RenderOption configures a Render call.
type RenderOption func(*renderOptions)

type renderOptions struct {
	Format     string
	Parallel   int
	RoutingKey string
}

// WithFormat sets the output format (e.g. "mp4", "png").
func WithFormat(format string) RenderOption {
	return func(o *renderOptions) {
		o.Format = format
	}
}

// WithParallel sets the parallelism level for rendering.
func WithParallel(n int) RenderOption {
	return func(o *renderOptions) {
		o.Parallel = n
	}
}

// WithRoutingKey sets the routing key for rendering.
func WithRoutingKey(key string) RenderOption {
	return func(o *renderOptions) {
		o.RoutingKey = key
	}
}

// TokenOption configures a CreateToken call.
type TokenOption func(*tokenOptions)

type tokenOptions struct {
	Format     string
	Parallel   int
	RoutingKey string
}

// WithTokenFormat sets the output format for token creation.
func WithTokenFormat(format string) TokenOption {
	return func(o *tokenOptions) {
		o.Format = format
	}
}

// WithTokenParallel sets the parallelism level for token creation.
func WithTokenParallel(n int) TokenOption {
	return func(o *tokenOptions) {
		o.Parallel = n
	}
}

// WithTokenRoutingKey sets the routing key for token creation.
func WithTokenRoutingKey(key string) TokenOption {
	return func(o *tokenOptions) {
		o.RoutingKey = key
	}
}

// BatchOption configures a CreateBatch call.
type BatchOption func(*batchOptions)

type batchOptions struct {
	Routing string
}

// WithRouting sets the routing key for the batch.
func WithRouting(routing string) BatchOption {
	return func(o *batchOptions) {
		o.Routing = routing
	}
}
