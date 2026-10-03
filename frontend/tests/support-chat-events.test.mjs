import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';
import { transformSync } from 'esbuild';

function fixture() {
  const sockets = [], timers = [], statuses = [];
  let refreshes = 0;
  class Socket {
    sent = [];
    constructor(url) { this.url = url; sockets.push(this); }
    send(frame) { this.sent.push(frame); }
    close() { this.onclose?.(); }
    receive(data) { this.onmessage?.({ data }); }
  }
  const source = readFileSync(new URL('../src/features/chat/services/support-chat-events.ts', import.meta.url), 'utf8')
    .replace("import { ENV } from '@/app/config/env';", 'const ENV = { WS_BASE_URL: "http://localhost:8080/ws" };');
  const context = { exports: {}, module: { exports: {} }, WebSocket: Socket,
    window: { setTimeout: (fn, delay) => { timers.push({ fn, delay }); return timers.length; }, clearTimeout: id => { if (timers[id - 1]) timers[id - 1].cancelled = true; } } };
  vm.runInNewContext(transformSync(source, { loader: 'ts', format: 'cjs' }).code, context);
  const stop = context.module.exports.watchSupportChat('test-session', () => refreshes++, s => statuses.push(s));
  return { sockets, timers, statuses, stop, get refreshes() { return refreshes; } };
}
test('authenticates, subscribes privately, handles fragmented and batched STOMP frames', () => {
  const f = fixture(); const ws = f.sockets[0]; ws.onopen();
  assert.match(ws.sent[0], /Authorization:Bearer test-session/);
  ws.receive('\nCONNE'); ws.receive('CTED\nversion:1.2\n\n\0');
  assert.match(ws.sent[1], /destination:\/user\/queue\/support-chat/);
  assert.equal(f.refreshes, 1);
  ws.receive('MESSAGE\nsubscription:support\n\n{}\0MESSAGE\nsubscription:support\n\n{}\0');
  assert.equal(f.refreshes, 3); f.stop();
});
test('reconnect reconciles missed events and logout cancels reconnect', () => {
  const f = fixture(); f.sockets[0].close(); assert.equal(f.timers[0].delay, 1000);
  f.timers[0].fn(); assert.equal(f.sockets.length, 2);
  f.sockets[1].receive('CONNECTED\n\n\0'); assert.equal(f.refreshes, 1);
  f.sockets[1].close(); f.stop(); assert.equal(f.timers.at(-1).cancelled, true);
  const count = f.refreshes; f.sockets[1].receive('MESSAGE\n\n{}\0'); assert.equal(f.refreshes, count);
});
