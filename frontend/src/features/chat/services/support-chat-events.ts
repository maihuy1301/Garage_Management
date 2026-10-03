import { ENV } from '@/app/config/env';

// Isolated authenticated subscription: reconnects and reconciles missed messages
// without interfering with the existing notification/appointment connection.
export function watchSupportChat(token: string, refresh: () => void, status: (connected: boolean) => void): () => void {
  let socket: WebSocket | null = null;
  let stopped = false;
  let retry: number | undefined;
  let buffer = '';
  let delay = 1000;
  const connect = () => {
    if (stopped) return;
    buffer = '';
    const ws = new WebSocket(ENV.WS_BASE_URL.replace(/^http/, 'ws'));
    socket = ws;
    ws.onopen = () => ws.send(`CONNECT\naccept-version:1.2\nAuthorization:Bearer ${token}\nheart-beat:0,0\n\n\0`);
    ws.onmessage = (event) => {
      if (stopped || socket !== ws || typeof event.data !== 'string') return;
      buffer += event.data;
      let boundary: number;
      while ((boundary = buffer.indexOf('\0')) >= 0) {
        const frame = buffer.slice(0, boundary).replace(/^[\r\n]+/, '');
        buffer = buffer.slice(boundary + 1);
        if (frame.startsWith('CONNECTED\n') || frame.startsWith('CONNECTED\r\n')) {
          delay = 1000;
          ws.send('SUBSCRIBE\nid:support\ndestination:/user/queue/support-chat\n\n\0');
          status(true); refresh();
        } else if (frame.startsWith('MESSAGE\n') || frame.startsWith('MESSAGE\r\n')) refresh();
        else if (frame.startsWith('ERROR')) ws.close();
      }
    };
    ws.onerror = () => ws.close();
    ws.onclose = () => {
      if (stopped || socket !== ws) return;
      status(false);
      retry = window.setTimeout(connect, delay);
      delay = Math.min(delay * 2, 15000);
    };
  };
  connect();
  return () => { stopped = true; window.clearTimeout(retry); socket?.close(); };
}
