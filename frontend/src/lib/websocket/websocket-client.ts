import { ENV } from '@/app/config/env';
import { tokenStorage } from '@/lib/auth/token-storage';

export type WebSocketStatus = 'DISCONNECTED' | 'CONNECTING' | 'CONNECTED' | 'ERROR';
export type MessageHandler = (payload: unknown) => void;

class WebSocketClient {
  private ws: WebSocket | null = null;
  private status: WebSocketStatus = 'DISCONNECTED';
  private subscriptions: Map<string, Set<MessageHandler>> = new Map();
  private reconnectTimer: number | null = null;

  public getStatus(): WebSocketStatus {
    return this.status;
  }

  public connect(authToken?: string): void {
    const token = authToken || tokenStorage.getToken();
    if (this.ws && (this.status === 'CONNECTED' || this.status === 'CONNECTING')) {
      return;
    }

    this.status = 'CONNECTING';

    try {
      // WebSocket endpoint supporting query or subprotocol token
      const wsUrl = new URL(ENV.WS_BASE_URL.replace(/^http/, 'ws'));
      if (token) {
        wsUrl.searchParams.set('token', token);
      }

      this.ws = new WebSocket(wsUrl.toString());

      this.ws.onopen = () => {
        this.status = 'CONNECTED';
        console.info('[WebSocket] Connected successfully to', ENV.WS_BASE_URL);
      };

      this.ws.onmessage = (event: MessageEvent) => {
        try {
          const data = JSON.parse(event.data);
          const destination = data.destination || data.topic;
          if (destination && this.subscriptions.has(destination)) {
            this.subscriptions.get(destination)?.forEach((handler) => handler(data.payload || data));
          }
        } catch {
          // Non-JSON message handler
        }
      };

      this.ws.onerror = (error) => {
        this.status = 'ERROR';
        console.warn('[WebSocket] Connection error:', error);
      };

      this.ws.onclose = () => {
        this.status = 'DISCONNECTED';
        this.ws = null;
      };
    } catch (err) {
      this.status = 'ERROR';
      console.warn('[WebSocket] Setup exception:', err);
    }
  }

  public disconnect(): void {
    if (this.reconnectTimer) {
      window.clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
    this.status = 'DISCONNECTED';
    this.subscriptions.clear();
  }

  public subscribe(destination: string, callback: MessageHandler): () => void {
    if (!this.subscriptions.has(destination)) {
      this.subscriptions.set(destination, new Set());
    }
    this.subscriptions.get(destination)!.add(callback);

    return () => {
      this.unsubscribe(destination, callback);
    };
  }

  public unsubscribe(destination: string, callback?: MessageHandler): void {
    if (!this.subscriptions.has(destination)) return;
    if (callback) {
      this.subscriptions.get(destination)!.delete(callback);
      if (this.subscriptions.get(destination)!.size === 0) {
        this.subscriptions.delete(destination);
      }
    } else {
      this.subscriptions.delete(destination);
    }
  }

  public send(destination: string, payload: unknown): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message = JSON.stringify({ destination, payload });
      this.ws.send(message);
    } else {
      console.warn('[WebSocket] Cannot send message, socket is not connected');
    }
  }
}

export const wsClient = new WebSocketClient();
