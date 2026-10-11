import { Link, useParams } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { RepairChatPanel } from './RepairChatPanel';

export function RepairChatPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const orderId = Number(id);
  return <div style={{ padding: '24px', maxWidth: '1000px', margin: '0 auto' }}>
    <Link to="/app/repair-orders">← Lệnh sửa chữa</Link>
    <h1>Trao đổi sửa chữa</h1>
    {Number.isSafeInteger(orderId) && orderId > 0
      ? <RepairChatPanel key={`${user?.maNguoiDung}-${orderId}`} orderId={orderId} />
      : <p role="alert">Mã phiếu không hợp lệ.</p>}
  </div>;
}
