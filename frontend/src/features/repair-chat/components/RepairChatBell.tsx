import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useWaitingChatCount } from '@/hooks/useWaitingChatCount';
import { watchSupportChat } from '@/features/chat/services/support-chat-events';
import { repairChatNotifications as api, RepairChatNotification } from '../services/repair-chat-notifications.service';
import { ApiError } from '@/types/api.types';
import './repair-chat-notifications.css';

export function RepairChatBell({ token }: { token: string }) {
  const waitingCount = useWaitingChatCount();
  const navigate = useNavigate();
  const [items, setItems] = useState<RepairChatNotification[]>([]);
  const [open, setOpen] = useState(false);
  const [toast, setToast] = useState<RepairChatNotification | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [opening, setOpening] = useState(false);
  const root = useRef<HTMLDivElement>(null);
  const refreshRef = useRef<() => void>(() => {});
  const alive = useRef(false);

  useEffect(() => {
    alive.current = true;
    let stopped = false, fetching = false, queued = false, baseline = false, lastId = 0;
    let toastTimer: ReturnType<typeof setTimeout>;
    const refresh = async () => {
      if (stopped) return;
      if (fetching) { queued = true; return; }
      fetching = true;
      try {
        const next = await api.list();
        if (stopped) return;
        const newMessage = next.find(n => !n.daDoc && n.maThongBao > lastId);
        if (baseline && newMessage) {
          setToast(newMessage); clearTimeout(toastTimer);
          toastTimer = setTimeout(() => setToast(null), 10000);
        }
        lastId = Math.max(lastId, next[0]?.maThongBao ?? 0); baseline = true;
        setItems(next); setError('');
      } catch (e) {
        if (!stopped) {
          setError('Chưa cập nhật được thông báo. Bấm thử lại.');
          if ([401, 403].includes((e as ApiError)?.status ?? 0)) { setItems([]); setToast(null); }
        }
      } finally {
        fetching = false;
        if (!stopped) { setLoading(false); if (queued) { queued = false; void refresh(); } }
      }
    };
    refreshRef.current = () => void refresh();
    void refresh();
    const stop = watchSupportChat(token, () => void refresh(), () => {}, '/user/queue/notifications');
    const timer = setInterval(() => { if (document.visibilityState === 'visible') void refresh(); }, 10000);
    const focus = () => { if (document.visibilityState === 'visible') void refresh(); };
    document.addEventListener('visibilitychange', focus);
    const outside = (e: MouseEvent) => { if (root.current && !root.current.contains(e.target as Node)) setOpen(false); };
    const escape = (e: KeyboardEvent) => { if (e.key === 'Escape') { setOpen(false); setToast(null); } };
    document.addEventListener('mousedown', outside); document.addEventListener('keydown', escape);
    return () => {
      stopped = true; alive.current = false; stop(); clearInterval(timer); clearTimeout(toastTimer);
      document.removeEventListener('visibilitychange', focus);
      document.removeEventListener('mousedown', outside); document.removeEventListener('keydown', escape);
    };
  }, [token]);

  const enter = async (item: RepairChatNotification) => {
    if (opening) return;
    setOpening(true);
    try {
      if (!item.daDoc) await api.read(item.maThongBao);
      if (!alive.current) return;
      setItems(old => old.map(n => n.maThongBao === item.maThongBao ? { ...n, daDoc: true } : n));
      setToast(null); setOpen(false);
      navigate(`/app/repair-orders/${item.maThamChieu}/chat`);
    } catch {
      if (alive.current) { setOpen(true); setError('Không thể mở thông báo. Vui lòng thử lại.'); }
    } finally { if (alive.current) setOpening(false); }
  };
  const unread = items.filter(n => !n.daDoc).length;
  const total = unread + waitingCount;
  // Put unread items first so a long history never hides an unread alert.
  const visible = [...items].sort((a, b) => Number(a.daDoc) - Number(b.daDoc) || b.maThongBao - a.maThongBao);
  return <div className="repair-alert" ref={root}>
    <button type="button" className="header-icon-btn" aria-label={`Thông báo, ${total} chưa đọc hoặc đang chờ`}
      aria-expanded={open} aria-controls="repair-alert-list" title="Thông báo chat" onClick={() => { setOpen(!open); refreshRef.current(); }}>
      <span className="material-symbols-outlined">notifications</span>
      {total > 0 && <span className="repair-alert__count">{total > 99 ? '99+' : total}</span>}
    </button>
    {open && <div className="repair-alert__menu" id="repair-alert-list" aria-label="Thông báo chat">
      <strong>Thông báo chat</strong>
      <button type="button" className="repair-alert__support" onClick={() => { setOpen(false); navigate('/app/chat'); }}>
        Hỗ trợ khách hàng{waitingCount > 0 ? ` · ${waitingCount} đang chờ` : ''}
      </button>
      <h3>Trao đổi sửa chữa {unread > 0 ? `(${unread} mới)` : ''}</h3>
      {error && <p role="alert">{error} <button type="button" onClick={() => refreshRef.current()}>Thử lại</button></p>}
      {loading ? <p>Đang tải thông báo…</p> : !items.length && <p>Chưa có thông báo từ kỹ thuật viên.</p>}
      <div className="repair-alert__items">{visible.map(n => <button type="button" key={n.maThongBao}
        className={`repair-alert__item ${n.daDoc ? '' : 'is-unread'}`} disabled={opening} onClick={() => void enter(n)}>
        <strong>{n.tieuDe}</strong><span>{n.noiDung}</span>
        <small>{new Date(n.ngayTao).toLocaleString('vi-VN')} · {n.daDoc ? 'Đã đọc' : 'Chưa đọc'}</small>
      </button>)}</div>
    </div>}
    {toast && <div className="repair-alert__toast" role="status">
      <strong>{toast.tieuDe}</strong><p>{toast.noiDung}</p>
      <button type="button" disabled={opening} onClick={() => void enter(toast)}>Mở trao đổi</button>
      <button type="button" aria-label="Ẩn thông báo" onClick={() => setToast(null)}>Đóng</button>
    </div>}
  </div>;
}
