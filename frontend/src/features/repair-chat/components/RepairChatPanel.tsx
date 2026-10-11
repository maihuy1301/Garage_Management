import { useEffect, useRef, useState } from 'react';
import { ApiError } from '@/types/api.types';
import { repairChatService as api, RepairChatHistory, RepairChatMessage } from '../services/repair-chat.service';
import './repair-chat.css';

const labels: Record<string, string> = {
  OPEN: 'Đang trao đổi', WAITING_ASSIGNMENT: 'Chờ duyệt phân công kỹ thuật viên',
  ARCHIVED: 'Đã bàn giao · Lưu trữ', CLOSED: 'Lần sửa đã hủy · Chỉ đọc', DELETED: 'Đã xóa vĩnh viễn',
};
const roles: Record<string, string> = { FRONT_DESK: 'Tiếp tân', TECHNICIAN: 'Kỹ thuật viên', MANAGER: 'Quản lý', ADMIN: 'Quản trị' };
function message(error: unknown): string {
  return typeof error === 'object' && error !== null && 'message' in error && typeof error.message === 'string'
    ? error.message : 'Không thể tải cuộc trò chuyện.';
}
function merge(old: RepairChatMessage[], incoming: RepairChatMessage[]) {
  return [...new Map([...old, ...incoming].map(m => [m.id, m])).values()].sort((a, b) => a.id - b.id);
}

/** Mount with an account/order key so in-flight work cannot cross conversations. */
export function RepairChatPanel({ orderId }: { orderId: number }) {
  const [data, setData] = useState<RepairChatHistory | null>(null);
  const [disabled, setDisabled] = useState(false);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [text, setText] = useState('');
  const [image, setImage] = useState<string>();
  const [attempt, setAttempt] = useState(0);
  const alive = useRef(false);
  const current = useRef<RepairChatHistory | null>(null);
  const pending = useRef<{ text: string; id: string }>();
  const locked = useRef(false);
  const end = useRef<HTMLDivElement>(null);
  const scroller = useRef<HTMLDivElement>(null);
  const apply = (page: RepairChatHistory, mode: 'initial' | 'older' | 'newer') => {
    if (!alive.current) return;
    const previous = current.current;
    const next = { ...page,
      messages: page.room.state === 'DELETED' ? [] : mode === 'initial' ? page.messages : merge(previous?.messages ?? [], page.messages),
      hasMore: mode === 'newer' ? previous?.hasMore ?? false : page.hasMore,
    };
    current.current = next; setData(next);
    if (page.room.state === 'DELETED') { setText(''); pending.current = undefined; }
    const nearBottom = !scroller.current || scroller.current.scrollHeight - scroller.current.scrollTop - scroller.current.clientHeight < 100;
    if (mode === 'initial' || (mode === 'newer' && nearBottom)) requestAnimationFrame(() => end.current?.scrollIntoView({ block: 'nearest' }));
  };
  const fail = (e: unknown) => {
    if (!alive.current) return;
    setError(message(e));
    // Never leave old private messages visible after access is revoked.
    if (typeof e === 'object' && e !== null && [401, 403, 404].includes((e as ApiError).status ?? 0)) {
      current.current = null; setData(null); setImage(undefined); setText('');
    }
  };

  useEffect(() => {
    alive.current = true;
    let cancelled = false;
    let timer: ReturnType<typeof setTimeout>;
    let objectUrl: string | undefined;
    const poll = async () => {
      if (cancelled) return;
      if (!locked.current && current.current && document.visibilityState === 'visible' && current.current.room.state !== 'DELETED') {
        locked.current = true;
        try {
          const rows = current.current.messages;
          const page = await api.history(orderId, { after: rows[rows.length - 1]?.id ?? 0 });
          if (!cancelled) { apply(page, 'newer'); setError(''); }
        } catch (e) { if (!cancelled) fail(e); }
        finally { locked.current = false; }
      }
      if (!cancelled) timer = setTimeout(poll, 5000);
    };
    const start = async () => {
      locked.current = true;
      setLoading(true); setError('');
      try {
        const enabled = await api.enabled();
        if (cancelled) return;
        setDisabled(!enabled);
        if (!enabled) return;
        const page = await api.history(orderId);
        if (cancelled) return;
        apply(page, 'initial');
        void api.image(orderId).then(blob => {
          if (cancelled || !current.current) return;
          objectUrl = URL.createObjectURL(blob); setImage(objectUrl);
        }).catch(() => { /* A vehicle without a profile image uses the car icon. */ });
        timer = setTimeout(poll, 5000);
      } catch (e) { if (!cancelled) fail(e); }
      finally { locked.current = false; if (!cancelled) setLoading(false); }
    };
    void start();
    return () => { cancelled = true; alive.current = false; clearTimeout(timer); if (objectUrl) URL.revokeObjectURL(objectUrl); };
  }, [orderId, attempt]);

  const older = async () => {
    if (!data || locked.current) return;
    locked.current = true; setBusy(true);
    try { apply(await api.history(orderId, { before: data.messages[0]?.id }), 'older'); setError(''); }
    catch (e) { fail(e); }
    finally { locked.current = false; if (alive.current) setBusy(false); }
  };
  const send = async () => {
    const content = text.trim();
    if (!content || !data?.room.canSend || loading || locked.current || content.length > 2000) return;
    if (pending.current?.text !== content) pending.current = { text: content, id: crypto.randomUUID() };
    locked.current = true; setBusy(true); setError('');
    try {
      await api.send(orderId, content, pending.current.id);
      if (!alive.current) return;
      setText(''); pending.current = undefined;
      // Fetch all missing messages after the previous cursor (including concurrent senders).
      const rows = current.current?.messages ?? [];
      apply(await api.history(orderId, { after: rows[rows.length - 1]?.id ?? 0 }), 'newer');
    } catch (e) { fail(e); }
    finally { locked.current = false; if (alive.current) setBusy(false); }
  };
  const remove = async () => {
    if (!data?.room.canDelete || locked.current || !window.confirm('Xóa vĩnh viễn toàn bộ tin nhắn của lần sửa này cho tất cả người tham gia? Không thể khôi phục. Hồ sơ xe và chứng từ vẫn được giữ.')) return;
    locked.current = true; setBusy(true);
    try {
      await api.remove(orderId);
      if (!alive.current) return;
      const next = { ...data, room: { ...data.room, state: 'DELETED' as const, canSend: false, canDelete: false }, messages: [], hasMore: false };
      current.current = next; setData(next); setText(''); setError('');
    } catch (e) { fail(e); }
    finally { locked.current = false; if (alive.current) setBusy(false); }
  };

  return <section className="repair-chat" aria-label="Trao đổi sửa chữa">
    <header className="repair-chat__header">
      <div className="repair-chat__vehicle">{image ? <img src={image} alt="Ảnh hồ sơ xe" /> : <span className="material-symbols-outlined">directions_car</span>}</div>
      <div><strong>{data?.room.plate || `Phiếu #${orderId}`}</strong>
        <div>{data ? `${data.room.vehicleName} · Phiếu #${data.room.repairOrderId}` : 'Trao đổi với kỹ thuật viên'}</div>
        {data && <small>{data.room.branchName} · {labels[data.room.state]}</small>}
      </div>
      {data?.room.canDelete && <button className="btn-ghost repair-chat__delete" disabled={busy} onClick={() => void remove()}>Xóa vĩnh viễn</button>}
    </header>
    {error && <div role="alert" className="repair-chat__notice">{error} <button type="button" disabled={busy || loading} onClick={() => { if (!locked.current) setAttempt(a => a + 1); }}>Thử lại</button></div>}
    <div className="repair-chat__messages" ref={scroller} aria-label="Tin nhắn">
      {loading && <p className="repair-chat__empty">Đang tải cuộc trò chuyện…</p>}
      {disabled && <p className="repair-chat__empty">Chat sửa chữa chưa được bật. Bạn có thể quay lại sau.</p>}
      {!loading && data && <>
        {data.hasMore && <button type="button" disabled={busy} onClick={() => void older()}>Xem tin nhắn trước</button>}
        {!data.messages.length && <p className="repair-chat__empty">{data.room.state === 'DELETED' ? 'Nội dung cuộc trò chuyện đã được xóa vĩnh viễn.' : data.room.canSend ? 'Chưa có tin nhắn. Bắt đầu trao đổi về tình trạng xe và công việc sửa chữa.' : labels[data.room.state]}</p>}
        {data.messages.map(m => <article key={m.id} className={`repair-chat__bubble ${m.mine ? 'is-mine' : ''}`}>
          <small>{m.senderName} · {roles[m.senderRole] || m.senderRole}</small>
          <p>{m.content}</p><time>{new Date(m.sentAt).toLocaleString('vi-VN')}</time>
        </article>)}
      </>}
      <div ref={end} />
    </div>
    <form className="repair-chat__composer" onSubmit={e => { e.preventDefault(); void send(); }}>
      <textarea aria-label="Nội dung tin nhắn" placeholder="Trao đổi về xe đang sửa…" rows={2} maxLength={2000}
        value={text} disabled={!data?.room.canSend || busy || loading} onChange={e => setText(e.target.value)} />
      <button className="btn-primary" type="submit" disabled={!data?.room.canSend || busy || loading || !text.trim()}>{busy ? 'Đang xử lý…' : 'Gửi'}</button>
    </form>
    <small className="repair-chat__footnote">Trao đổi nội bộ giữa tiếp tân và kỹ thuật viên. Khách hàng không xem được.</small>
  </section>;
}
