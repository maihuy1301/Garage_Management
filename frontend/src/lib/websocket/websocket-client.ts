import { ENV } from '@/app/config/env';
import { tokenStorage } from '@/lib/auth/token-storage';

export type WebSocketStatus = 'DISCONNECTED' | 'CONNECTING' | 'CONNECTED' | 'ERROR';
export type MessageHandler = (payload: unknown) => void;

class WebSocketClient {
  private ws: WebSocket | null = null;
  private status: WebSocketStatus = 'DISCONNECTED';
  private subscriptions: Map<string, Set<MessageHandler>> = new Map();
  private reconnectTimer: number | null = null;
  private subscriptionIds: Map<string, string> = new Map();
  private nextSubscriptionId = 1;

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
      const wsUrl = new URL(ENV.WS_BASE_URL.replace(/^http/, 'ws'));
      this.ws = new WebSocket(wsUrl.toString());

      this.ws.onopen = () => {
        this.sendFrame('CONNECT', {
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
          'accept-version': '1.2',
          'heart-beat': '10000,10000',
        });
      };

      this.ws.onmessage = (event: MessageEvent) => {
        const frame = this.parseFrame(event.data);
        if (!frame) return;

        if (frame.command === 'CONNECTED') {
          this.status = 'CONNECTED';
          this.subscriptions.forEach((_handlers, destination) => {
            this.sendSubscribe(destination);
          });
          console.info('[WebSocket] Connected successfully to', ENV.WS_BASE_URL);
          return;
        }

        if (frame.command === 'MESSAGE') {
          const destination = frame.headers.destination;
          if (!destination) return;
          const payload = this.parseJson(frame.body);
          this.subscriptions.get(destination)?.forEach((handler) => handler(payload));
        }

        if (frame.command === 'ERROR') {
          this.status = 'ERROR';
          console.warn('[WebSocket] STOMP error:', frame.body || frame.headers.message);
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
    this.subscriptionIds.clear();
  }

  public subscribe(destination: string, callback: MessageHandler): () => void {
    const alreadySubscribed = this.subscriptions.has(destination);
    if (!this.subscriptions.has(destination)) {
      this.subscriptions.set(destination, new Set());
    }
    this.subscriptions.get(destination)!.add(callback);

    if (!alreadySubscribed && this.status === 'CONNECTED') {
      this.sendSubscribe(destination);
    }

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
        this.sendUnsubscribe(destination);
      }
    } else {
      this.subscriptions.delete(destination);
      this.sendUnsubscribe(destination);
    }
  }

  public send(destination: string, payload: unknown): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.sendFrame('SEND', {
        destination,
        'content-type': 'application/json',
      }, JSON.stringify(payload));
    } else {
      console.warn('[WebSocket] Cannot send message, socket is not connected');
    }
  }

  private sendSubscribe(destination: string): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
    if (!this.subscriptionIds.has(destination)) {
      this.subscriptionIds.set(destination, `sub-${this.nextSubscriptionId++}`);
    }
    this.sendFrame('SUBSCRIBE', {
      id: this.subscriptionIds.get(destination)!,
      destination,
    });
  }

  private sendUnsubscribe(destination: string): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
    const id = this.subscriptionIds.get(destination);
    if (!id) return;
    this.sendFrame('UNSUBSCRIBE', { id });
    this.subscriptionIds.delete(destination);
  }

  private sendFrame(command: string, headers: Record<string, string>, body = ''): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
    const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${value}`);
    this.ws.send(`${command}\n${headerLines.join('\n')}\n\n${body}\0`);
  }

  private parseFrame(raw: string): { command: string; headers: Record<string, string>; body: string } | null {
    const normalized = raw.replace(/\0+$/, '');
    const [head = '', body = ''] = normalized.split('\n\n');
    const lines = head.split('\n').filter(Boolean);
    const command = lines.shift();
    if (!command) return null;
    const headers = lines.reduce<Record<string, string>>((acc, line) => {
      const separator = line.indexOf(':');
      if (separator > -1) {
        acc[line.slice(0, separator)] = line.slice(separator + 1);
      }
      return acc;
    }, {});
    return { command, headers, body };
  }

  private parseJson(body: string): unknown {
    try {
      return JSON.parse(body);
    } catch {
      return body;
    }
  }
}

export const wsClient = new WebSocketClient();
