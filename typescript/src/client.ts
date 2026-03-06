import type {
  ImpossibleFXConfig,
  RenderOptions,
  RenderResult,
  CreateTokenOptions,
  TokenResult,
  Progress,
  CreateBatchOptions,
  BatchTask,
  BatchStatus,
  BatchResult,
} from './types.js';

/** Error thrown when an API request fails. */
export class ImpossibleFXError extends Error {
  /** HTTP status code, if available. */
  public readonly status?: number;

  constructor(message: string, status?: number) {
    super(message);
    this.name = 'ImpossibleFXError';
    this.status = status;
  }
}

/**
 * Impossible FX v3 client.
 *
 * Provides methods for rendering movies, managing tokens, and running batch jobs
 * against the Impossible FX render API.
 *
 * @example
 * ```typescript
 * const client = new ImpossibleFX({ region: 'us-east-1', apiKey: 'my-key' });
 * const result = await client.render('proj-123', 'intro', { title: 'Hello' });
 * console.log(result.url);
 * ```
 */
export class ImpossibleFX {
  private readonly baseUrl: string;
  private readonly headers: Record<string, string>;

  constructor(config: ImpossibleFXConfig) {
    if (!config.region) {
      throw new Error('region is required');
    }
    this.baseUrl = `https://render-${config.region}.impossible.io/v2`;
    this.headers = {
      'Content-Type': 'application/json',
    };
    if (config.apiKey) {
      this.headers['Authorization'] = `Bearer ${config.apiKey}`;
    }
  }

  /**
   * Sends an HTTP request to the API and returns the parsed JSON response.
   * Throws {@link ImpossibleFXError} on non-2xx status codes.
   */
  private async request<T>(method: string, path: string, body?: unknown): Promise<T> {
    const url = `${this.baseUrl}${path}`;
    const init: RequestInit = {
      method,
      headers: this.headers,
    };
    if (body !== undefined) {
      init.body = JSON.stringify(body);
    }

    const response = await fetch(url, init);

    if (!response.ok) {
      let message: string;
      try {
        const err = await response.json() as Record<string, unknown>;
        message = (err.message as string) || (err.error as string) || response.statusText;
      } catch {
        message = response.statusText;
      }
      throw new ImpossibleFXError(message, response.status);
    }

    // Handle 204 No Content
    if (response.status === 204) {
      return undefined as T;
    }

    return response.json() as Promise<T>;
  }

  /**
   * Render a movie and return the result including the output URL.
   *
   * @param projectId - The project identifier.
   * @param movie - The movie template name.
   * @param params - Key-value render parameters.
   * @param options - Optional render settings (format, parallel workers, routing key).
   * @returns The render result with token, URL, expiration, and status.
   */
  async render(
    projectId: string,
    movie: string,
    params: Record<string, any>,
    options?: RenderOptions,
  ): Promise<RenderResult> {
    const body: Record<string, any> = {
      movie,
      params,
    };
    if (options?.format) body.format = options.format;
    if (options?.parallel) body.parallel = options.parallel;
    if (options?.routingKey) body.routingKey = options.routingKey;

    return this.request<RenderResult>('POST', `/render/${projectId}`, body);
  }

  /**
   * Create a render token without waiting for the render to complete.
   *
   * Use {@link getUrl} to construct the output URL from the returned token,
   * and {@link getProgress} to poll for render progress.
   *
   * @param projectId - The project identifier.
   * @param movie - The movie template name.
   * @param params - Key-value render parameters.
   * @param options - Optional settings (routing key).
   * @returns The token result with token, expiration, and status.
   */
  async createToken(
    projectId: string,
    movie: string,
    params: Record<string, any>,
    options?: CreateTokenOptions,
  ): Promise<TokenResult> {
    const body: Record<string, any> = {
      movie,
      params,
    };
    if (options?.routingKey) body.routingKey = options.routingKey;

    return this.request<TokenResult>('POST', `/token/${projectId}`, body);
  }

  /**
   * Construct the output URL for a given render token.
   *
   * @param token - The render token.
   * @param format - Output format (defaults to "mp4").
   * @returns The fully-qualified URL to the rendered output.
   */
  getUrl(token: string, format: string = 'mp4'): string {
    return `${this.baseUrl}/get/${token}.${format}`;
  }

  /**
   * Get the current render progress for a token.
   *
   * @param token - The render token.
   * @returns Progress information with done/total counts.
   */
  async getProgress(token: string): Promise<Progress> {
    return this.request<Progress>('GET', `/progress/${token}`);
  }

  /**
   * Create a new batch for grouping multiple render tasks.
   *
   * @param options - Optional batch settings (routing key).
   * @returns An object containing the batch ID.
   */
  async createBatch(options?: CreateBatchOptions): Promise<{ id: string }> {
    const body: Record<string, any> = {};
    if (options?.routing) body.routing = options.routing;

    return this.request<{ id: string }>('POST', '/batch', body);
  }

  /**
   * Add tasks to an existing batch.
   *
   * @param batchId - The batch identifier.
   * @param tasks - Array of render tasks to add.
   */
  async addBatchTasks(batchId: string, tasks: BatchTask[]): Promise<void> {
    await this.request<void>('POST', `/batch/${batchId}/tasks`, { tasks });
  }

  /**
   * Start execution of a batch.
   *
   * @param batchId - The batch identifier.
   */
  async runBatch(batchId: string): Promise<void> {
    await this.request<void>('POST', `/batch/${batchId}/run`);
  }

  /**
   * Get the current status of a batch.
   *
   * @param batchId - The batch identifier.
   * @returns Batch status including task count and overall state.
   */
  async getBatchStatus(batchId: string): Promise<BatchStatus> {
    return this.request<BatchStatus>('GET', `/batch/${batchId}`);
  }

  /**
   * Get the results of a completed batch run.
   *
   * @param runId - The batch run identifier.
   * @returns Array of results for each task in the batch.
   */
  async getBatchResults(runId: string): Promise<BatchResult[]> {
    return this.request<BatchResult[]>('GET', `/batch/${runId}/results`);
  }

  /**
   * Cancel a running batch.
   *
   * @param batchId - The batch identifier.
   */
  async cancelBatch(batchId: string): Promise<void> {
    await this.request<void>('POST', `/batch/${batchId}/cancel`);
  }
}
