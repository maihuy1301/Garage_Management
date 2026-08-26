/**
 * Resolves Backend API base URL dynamically:
 * - If VITE_API_BASE_URL is explicitly set to a custom remote host (non-localhost), use it.
 * - In browser context, automatically resolves to current hostname (e.g. LAN IP / host name)
 *   so devices across LAN communicate directly with the Docker host.
 */
function resolveApiBaseUrl(): string {
  const envUrl = import.meta.env.VITE_API_BASE_URL;
  if (envUrl && !envUrl.includes('localhost') && !envUrl.includes('127.0.0.1')) {
    return envUrl;
  }

  if (typeof window !== 'undefined' && window.location?.hostname) {
    const protocol = window.location.protocol;
    const hostname = window.location.hostname;
    // Extract port from envUrl if custom port was provided, default to 8080
    const portMatch = envUrl?.match(/:(\d+)/);
    const port = portMatch ? portMatch[1] : '8080';
    return `${protocol}//${hostname}:${port}/api`;
  }

  return envUrl || 'http://localhost:8080/api';
}

/**
 * Resolves WebSocket STOMP base URL dynamically for LAN / local support.
 */
function resolveWsBaseUrl(): string {
  const envUrl = import.meta.env.VITE_WS_BASE_URL;
  if (envUrl && !envUrl.includes('localhost') && !envUrl.includes('127.0.0.1')) {
    return envUrl;
  }

  if (typeof window !== 'undefined' && window.location?.hostname) {
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const hostname = window.location.hostname;
    const portMatch = envUrl?.match(/:(\d+)/);
    const port = portMatch ? portMatch[1] : '8080';
    return `${wsProtocol}//${hostname}:${port}/ws`;
  }

  return envUrl || 'http://localhost:8080/ws';
}

export const ENV = {
  API_BASE_URL: resolveApiBaseUrl(),
  WS_BASE_URL: resolveWsBaseUrl(),
  IS_DEV: import.meta.env.DEV,
  IS_PROD: import.meta.env.PROD,
} as const;

