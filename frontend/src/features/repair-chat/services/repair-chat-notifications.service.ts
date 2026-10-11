import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';

export interface RepairChatNotification {
  maThongBao: number;
  tieuDe: string;
  noiDung: string;
  loaiThongBao: string;
  maThamChieu: number;
  daDoc: boolean;
  ngayTao: string;
}
export const repairChatNotifications = {
  async list() {
    const response = await apiClient.get<ApiResponse<RepairChatNotification[]>>(API_ENDPOINTS.NOTIFICATIONS);
    return response.data.data.filter(n => n.loaiThongBao === 'REPAIR_CHAT_MESSAGE')
      .sort((a, b) => b.maThongBao - a.maThongBao);
  },
  async read(id: number) { await apiClient.patch(`${API_ENDPOINTS.NOTIFICATIONS}/${id}/read`); },
};
