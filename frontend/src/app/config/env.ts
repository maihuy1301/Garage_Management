export const ENV = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  WS_BASE_URL: import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080/ws',
  IS_DEV: import.meta.env.DEV,
  IS_PROD: import.meta.env.PROD,
} as const;
