package impossiblefx

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"time"
)

// Client communicates with the Impossible FX render API.
type Client struct {
	region     string
	baseURL    string
	apiKey     string
	httpClient *http.Client
}

// ClientOption configures the Client.
type ClientOption func(*Client)

// WithAPIKey sets the API key used for authentication.
func WithAPIKey(key string) ClientOption {
	return func(c *Client) {
		c.apiKey = key
	}
}

// WithHTTPClient sets a custom HTTP client.
func WithHTTPClient(hc *http.Client) ClientOption {
	return func(c *Client) {
		c.httpClient = hc
	}
}

// NewClient creates a new Impossible FX client for the given region.
func NewClient(region string, opts ...ClientOption) *Client {
	c := &Client{
		region:  region,
		baseURL: fmt.Sprintf("https://render-%s.impossible.io/v2", region),
		httpClient: &http.Client{
			Timeout: 30 * time.Second,
		},
	}
	for _, opt := range opts {
		opt(c)
	}
	return c
}

// Render creates a render token and returns the result including the asset URL.
func (c *Client) Render(ctx context.Context, projectID, movie string, params map[string]any, opts ...RenderOption) (*RenderResult, error) {
	o := &renderOptions{}
	for _, opt := range opts {
		opt(o)
	}

	body := map[string]any{
		"movie":  movie,
		"params": params,
	}
	if o.Format != "" {
		body["format"] = o.Format
	}
	if o.Parallel > 0 {
		body["parallel"] = o.Parallel
	}
	if o.RoutingKey != "" {
		body["routingKey"] = o.RoutingKey
	}

	var result RenderResult
	if err := c.doJSON(ctx, http.MethodPost, "/render/"+projectID, body, &result); err != nil {
		return nil, err
	}
	return &result, nil
}

// CreateToken creates a render token without waiting for the render to complete.
func (c *Client) CreateToken(ctx context.Context, projectID, movie string, params map[string]any, opts ...TokenOption) (*TokenResult, error) {
	o := &tokenOptions{}
	for _, opt := range opts {
		opt(o)
	}

	body := map[string]any{
		"movie":  movie,
		"params": params,
	}
	if o.Format != "" {
		body["format"] = o.Format
	}
	if o.Parallel > 0 {
		body["parallel"] = o.Parallel
	}
	if o.RoutingKey != "" {
		body["routingKey"] = o.RoutingKey
	}

	var result TokenResult
	if err := c.doJSON(ctx, http.MethodPost, "/token/"+projectID, body, &result); err != nil {
		return nil, err
	}
	return &result, nil
}

// GetURL returns the full URL for a rendered asset given its token and format.
func (c *Client) GetURL(token, format string) string {
	return fmt.Sprintf("%s/get/%s.%s", c.baseURL, token, format)
}

// GetProgress returns the rendering progress for the given token.
func (c *Client) GetProgress(ctx context.Context, token string) (*Progress, error) {
	var result Progress
	if err := c.doJSON(ctx, http.MethodGet, "/progress/"+token, nil, &result); err != nil {
		return nil, err
	}
	return &result, nil
}

// CreateBatch creates a new batch and returns its info.
func (c *Client) CreateBatch(ctx context.Context, opts ...BatchOption) (*BatchInfo, error) {
	o := &batchOptions{}
	for _, opt := range opts {
		opt(o)
	}

	body := map[string]any{}
	if o.Routing != "" {
		body["routing"] = o.Routing
	}

	var result BatchInfo
	if err := c.doJSON(ctx, http.MethodPost, "/batch", body, &result); err != nil {
		return nil, err
	}
	return &result, nil
}

// AddBatchTasks adds tasks to an existing batch.
func (c *Client) AddBatchTasks(ctx context.Context, batchID string, tasks []BatchTask) error {
	body := map[string]any{
		"tasks": tasks,
	}
	return c.doJSON(ctx, http.MethodPost, "/batch/"+batchID+"/tasks", body, nil)
}

// RunBatch starts execution of a batch.
func (c *Client) RunBatch(ctx context.Context, batchID string) error {
	return c.doJSON(ctx, http.MethodPost, "/batch/"+batchID+"/run", nil, nil)
}

// GetBatchStatus returns the current status of a batch.
func (c *Client) GetBatchStatus(ctx context.Context, batchID string) (*BatchStatus, error) {
	var result BatchStatus
	if err := c.doJSON(ctx, http.MethodGet, "/batch/"+batchID+"/status", nil, &result); err != nil {
		return nil, err
	}
	return &result, nil
}

// GetBatchResults returns the results of a completed batch run.
func (c *Client) GetBatchResults(ctx context.Context, runID string) ([]BatchResult, error) {
	var results []BatchResult
	if err := c.doJSON(ctx, http.MethodGet, "/batch/run/"+runID+"/results", nil, &results); err != nil {
		return nil, err
	}
	return results, nil
}

// CancelBatch cancels a running batch.
func (c *Client) CancelBatch(ctx context.Context, batchID string) error {
	return c.doJSON(ctx, http.MethodPost, "/batch/"+batchID+"/cancel", nil, nil)
}

// doJSON performs an HTTP request with JSON encoding/decoding and error handling.
func (c *Client) doJSON(ctx context.Context, method, path string, body any, result any) error {
	var reqBody io.Reader
	if body != nil {
		data, err := json.Marshal(body)
		if err != nil {
			return fmt.Errorf("encoding request body: %w", err)
		}
		reqBody = bytes.NewReader(data)
	}

	req, err := http.NewRequestWithContext(ctx, method, c.baseURL+path, reqBody)
	if err != nil {
		return fmt.Errorf("creating request: %w", err)
	}

	if body != nil {
		req.Header.Set("Content-Type", "application/json")
	}
	req.Header.Set("Accept", "application/json")
	if c.apiKey != "" {
		req.Header.Set("Authorization", "Bearer "+c.apiKey)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("executing request: %w", err)
	}
	defer resp.Body.Close()

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("reading response body: %w", err)
	}

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		apiErr := &APIError{StatusCode: resp.StatusCode}
		if err := json.Unmarshal(respBody, apiErr); err != nil {
			apiErr.Message = string(respBody)
		}
		if reqID := resp.Header.Get("X-Request-Id"); reqID != "" {
			apiErr.RequestID = reqID
		}
		return apiErr
	}

	if result != nil && len(respBody) > 0 {
		if err := json.Unmarshal(respBody, result); err != nil {
			return fmt.Errorf("decoding response: %w", err)
		}
	}

	return nil
}
