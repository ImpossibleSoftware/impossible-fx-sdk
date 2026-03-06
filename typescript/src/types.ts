/** Configuration options for the ImpossibleFX client. */
export interface ImpossibleFXConfig {
  /** API key for authentication. Optional for public projects. */
  apiKey?: string;
  /** Render region (e.g. "us-east-1", "eu-central-1"). */
  region: string;
}

/** Options for a render request. */
export interface RenderOptions {
  /** Output format (e.g. "mp4", "gif", "png"). */
  format?: string;
  /** Number of parallel render workers. */
  parallel?: number;
  /** Routing key for render affinity. */
  routingKey?: string;
}

/** Result of a render request. */
export interface RenderResult {
  /** The render token. */
  token: string;
  /** Direct URL to the rendered output. */
  url: string;
  /** Expiration timestamp (Unix seconds). */
  expires: number;
  /** HTTP status code. */
  status: number;
}

/** Options for creating a render token. */
export interface CreateTokenOptions {
  /** Routing key for render affinity. */
  routingKey?: string;
}

/** Result of a token creation request. */
export interface TokenResult {
  /** The render token. */
  token: string;
  /** Expiration timestamp (Unix seconds). */
  expires: number;
  /** HTTP status code. */
  status: number;
}

/** Render progress information. */
export interface Progress {
  /** The render token. */
  token: string;
  /** Number of completed frames/steps. */
  done: number;
  /** Total number of frames/steps. */
  total: number;
}

/** Options for creating a batch. */
export interface CreateBatchOptions {
  /** Routing key for batch affinity. */
  routing?: string;
}

/** A single task within a batch. */
export interface BatchTask {
  /** Movie template name. */
  movie: string;
  /** Render parameters. */
  params: Record<string, any>;
  /** Output format (e.g. "mp4", "gif", "png"). */
  format?: string;
}

/** Status of a batch run. */
export interface BatchStatus {
  /** Batch identifier. */
  id: string;
  /** Current batch status (e.g. "pending", "running", "completed"). */
  status: string;
  /** Total number of tasks in the batch. */
  tasks: number;
}

/** Result of a single task within a completed batch. */
export interface BatchResult {
  /** The render token. */
  token: string;
  /** Direct URL to the rendered output. */
  url: string;
  /** Task status (e.g. "completed", "failed"). */
  status: string;
}
