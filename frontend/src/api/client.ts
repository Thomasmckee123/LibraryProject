import type { ApiError } from "../types";

/**
 * Every fetch in the app goes through here. Components never call fetch
 * directly - see frontend/CLAUDE.md.
 */
export class ApiClientError extends Error {
  readonly status: number;
  readonly body: ApiError;

  constructor(status: number, body: ApiError) {
    super(body.message ?? `Request failed with ${status}`);
    this.name = "ApiClientError";
    this.status = status;
    this.body = body;
  }

  /** True when checkout failed because someone else took the last copy. */
  get isOutOfStock(): boolean {
    return this.status === 409;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });

  if (!response.ok) {
    let body: ApiError;
    try {
      body = await response.json();
    } catch {
      body = { status: response.status, message: response.statusText };
    }
    throw new ApiClientError(response.status, body);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, { method: "POST", body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};
