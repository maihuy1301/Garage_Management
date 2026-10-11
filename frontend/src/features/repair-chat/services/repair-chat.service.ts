import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import { ApiResponse } from '@/types/api.types';

export interface RepairChatRoom {
  receptionId: number;
  repairOrderId: number;
  vehicleId: number;
  plate: string;
  vehicleName: string;
  branchName: string;
  state: 'OPEN' | 'WAITING_ASSIGNMENT' | 'ARCHIVED' | 'CLOSED' | 'DELETED';
  canSend: boolean;
  canDelete: boolean;
}
export interface RepairChatMessage {
  id: number;
  senderId: number;
  senderName: string;
  senderRole: string;
  content: string;
  sentAt: string;
  mine: boolean;
}
export interface RepairChatHistory {
  room: RepairChatRoom;
  messages: RepairChatMessage[];
  hasMore: boolean;
}
const base = API_ENDPOINTS.REPAIR_CHAT;
const order = (id: number) => `${base}/orders/${id}`;
export const repairChatService = {
  async enabled() {
    return (await apiClient.get<ApiResponse<{ enabled: boolean }>>(`${base}/capabilities`)).data.data.enabled;
  },
  async history(id: number, params: { before?: number; after?: number } = {}) {
    return (await apiClient.get<ApiResponse<RepairChatHistory>>(`${order(id)}/messages`, { params })).data.data;
  },
  async send(id: number, content: string, clientId: string) {
    return (await apiClient.post<ApiResponse<RepairChatMessage>>(`${order(id)}/messages`, { content, clientId })).data.data;
  },
  async remove(id: number) { await apiClient.delete(order(id)); },
  async image(id: number) {
    return (await apiClient.get<Blob>(`${order(id)}/vehicle-image`, { responseType: 'blob' })).data;
  },
};
