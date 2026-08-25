/**
 * Token Storage Abstraction
 * Default strategy uses localStorage for development.
 * Can be switched to sessionStorage or secure memory/cookie adapter as needed.
 */

export interface ITokenStorage {
  getToken(): string | null;
  setToken(token: string): void;
  removeToken(): void;
}

const TOKEN_KEY = 'garage_access_token';

class LocalStorageTokenStorage implements ITokenStorage {
  getToken(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  }

  setToken(token: string): void {
    try {
      localStorage.setItem(TOKEN_KEY, token);
    } catch (error) {
      console.error('Failed to save token to localStorage:', error);
    }
  }

  removeToken(): void {
    try {
      localStorage.removeItem(TOKEN_KEY);
    } catch (error) {
      console.error('Failed to remove token from localStorage:', error);
    }
  }
}

export const tokenStorage: ITokenStorage = new LocalStorageTokenStorage();

/**
 * Parses JWT payload safely without external dependencies
 */
export function parseJwtClaims(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}
