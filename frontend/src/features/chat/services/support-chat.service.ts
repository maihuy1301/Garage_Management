import { apiClient } from '@/lib/api/client';
import { API_ENDPOINTS } from '@/lib/api/endpoints';
import type { ApiResponse } from '@/types/api.types';

export interface SupportConversation {
  id: number; branchId: number; branchName: string; customerId: number; customerName: string;
  agentId: number | null; agentName: string | null; status: 'BOT' | 'WAITING' | 'HUMAN';
  updatedAt: string; lastMessageId: number; unreadCount: number;
}
export interface SupportMessage {
  id: number; conversationId: number; senderId: number | null; senderName: string;
  senderType: 'BOT' | 'SYSTEM' | 'CUSTOMER' | 'STAFF'; content: string; createdAt: string;
}
const base = API_ENDPOINTS.SUPPORT_CHAT;
export const supportChat = {
  enabled: async () => (await apiClient.get<ApiResponse<{ enabled: boolean }>>(`${base}/capabilities`)).data.data.enabled,
  list: async () => (await apiClient.get<ApiResponse<SupportConversation[]>>(base)).data.data,
  history: async (id: number, before?: number) =>
    (await apiClient.get<ApiResponse<{ messages: SupportMessage[]; hasMore: boolean }>>(`${base}/${id}/messages`, { params: { before } })).data.data,
  send: async (id: number, content: string, clientId: string) =>
    (await apiClient.post<ApiResponse<SupportMessage>>(`${base}/${id}/messages`, { content, clientId })).data.data,
  action: async (id: number, action: 'claim' | 'resolve') =>
    (await apiClient.post<ApiResponse<SupportConversation>>(`${base}/${id}/${action}`)).data.data,
  read: async (id: number, lastReadId: number) => { await apiClient.patch(`${base}/${id}/read`, { lastReadId }); },
  clear: async (id: number) => (await apiClient.delete<ApiResponse<SupportConversation>>(`${base}/${id}/messages`)).data.data,
};
