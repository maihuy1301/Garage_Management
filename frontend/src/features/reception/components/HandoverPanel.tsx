import { useCallback, useEffect, useState } from 'react';
import { Button } from '@/components/common/Button';
import { HandoverResponse } from '@/types/reception.types';
import { receptionService } from '../services/reception.service';

export function HandoverPanel({ receptionId, onCompleted }: {
  receptionId: number;
  onCompleted?: () => void;
}) {
  const [status, setStatus] = useState<HandoverResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [confirmed, setConfirmed] = useState(false);
  const [note, setNote] = useState('');
  const [revision, setRevision] = useState(0);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError(null);
    receptionService.getHandover(receptionId).then(result => {
      if (active) setStatus(result);
    }).catch(() => {
      if (active) setError('Không thể tải thông tin bàn giao. Vui lòng thử lại.');
    }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [receptionId, revision]);

  const submit = useCallback(async () => {
    if (!confirmed || !status?.eligible || saving) return;
    setSaving(true);
    setError(null);
    try {
      const result = await receptionService.handover(receptionId, note.trim());
      setStatus(result);
      onCompleted?.();
    } catch (err: unknown) {
      setError(typeof err === 'object' && err !== null && 'message' in err && typeof err.message === 'string'
        ? err.message : 'Chưa thể bàn giao xe. Vui lòng thử lại.');
    } finally {
      setSaving(false);
    }
  }, [confirmed, status, saving, receptionId, note, onCompleted]);

  return (
    <section aria-label="Bàn giao xe" style={{ borderTop: '1px solid var(--color-border)', paddingTop: 16 }}>
      <h4 style={{ margin: '0 0 12px' }}>Bàn giao xe</h4>
      {loading && <p role="status">Đang kiểm tra điều kiện bàn giao...</p>}
      {error && <p className="alert alert-error" role="alert">{error}</p>}
      {!loading && status?.thoiGianBanGiao && <>
        <p><strong>Đã bàn giao:</strong> {new Date(status.thoiGianBanGiao).toLocaleString('vi-VN')}</p>
        <p><strong>Người bàn giao:</strong> {status.tenNguoiBanGiao}</p>
        {status.ghiChu && <p style={{ overflowWrap: 'anywhere' }}>{status.ghiChu}</p>}
      </>}
      {!loading && !status?.thoiGianBanGiao && <>
        {status?.reason && <p>{status.reason}</p>}
        {status?.eligible && <>
          <label className="form-label" htmlFor="handover-note">Ghi chú bàn giao</label>
          <textarea id="handover-note" className="form-input" rows={3} maxLength={500}
            value={note} disabled={saving} onChange={event => setNote(event.target.value)}
            style={{ width: '100%', boxSizing: 'border-box', resize: 'vertical' }} />
          <label style={{ display: 'flex', alignItems: 'flex-start', gap: 8, margin: '12px 0' }}>
            <input type="checkbox" checked={confirmed} disabled={saving}
              onChange={event => setConfirmed(event.target.checked)} />
            <span>Tôi đã đối chiếu và giao xe cho khách hàng.</span>
          </label>
          <Button type="button" disabled={!confirmed} isLoading={saving} onClick={submit}
            style={{ marginRight: 8 }}
            leftIcon={<span className="material-symbols-outlined" aria-hidden="true">car_rental</span>}>
            Xác nhận đã bàn giao
          </Button>
        </>}
        <Button type="button" variant="secondary" disabled={saving} onClick={() => setRevision(value => value + 1)}
          style={{ marginTop: 12 }} leftIcon={<span className="material-symbols-outlined" aria-hidden="true">refresh</span>}>
          Kiểm tra lại
        </Button>
      </>}
    </section>
  );
}
