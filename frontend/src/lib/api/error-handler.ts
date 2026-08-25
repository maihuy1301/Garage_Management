import { AxiosError } from 'axios';
import { ApiError, ApiResponse } from '@/types/api.types';

type UnauthorizedHandler = () => void;
let onUnauthorizedCallback: UnauthorizedHandler | null = null;

export function registerUnauthorizedHandler(handler: UnauthorizedHandler): void {
  onUnauthorizedCallback = handler;
}

export function triggerUnauthorized(): void {
  if (onUnauthorizedCallback) {
    onUnauthorizedCallback();
  }
}

export function normalizeApiError(error: unknown): ApiError {
  if (!error) {
    return { message: 'Đã xảy ra lỗi không xác định.' };
  }

  if (typeof error === 'object' && 'isAxiosError' in error) {
    const axiosError = error as AxiosError<ApiResponse<unknown> | string>;

    if (!axiosError.response) {
      return {
        status: 0,
        message: 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng hoặc máy chủ Backend.',
      };
    }

    const status = axiosError.response.status;
    const data = axiosError.response.data;

    let serverMessage = '';
    if (typeof data === 'object' && data !== null && 'message' in data && typeof data.message === 'string') {
      serverMessage = data.message;
    } else if (typeof data === 'string') {
      serverMessage = data;
    }

    switch (status) {
      case 400:
        return {
          status,
          message: serverMessage || 'Dữ liệu yêu cầu không hợp lệ.',
          data,
        };
      case 401:
        return {
          status,
          message: serverMessage || 'Tài khoản hoặc mật khẩu không chính xác.',
          data,
        };
      case 403:
        return {
          status,
          message: 'Bạn không có quyền truy cập tính năng hoặc tài nguyên này.',
          data,
        };
      case 404:
        return {
          status,
          message: serverMessage || 'Không tìm thấy dữ liệu yêu cầu.',
          data,
        };
      case 409:
        return {
          status,
          message: serverMessage || 'Dữ liệu bị xung đột hoặc đã tồn tại.',
          data,
        };
      case 500:
      default:
        return {
          status,
          message: 'Đã xảy ra lỗi trên hệ thống máy chủ. Vui lòng thử lại sau.',
          data,
        };
    }
  }

  if (error instanceof Error) {
    return { message: error.message };
  }

  return { message: 'Đã xảy ra lỗi không xác định.' };
}
