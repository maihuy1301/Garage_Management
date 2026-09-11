import { normalizeApiError } from '@/lib/api/error-handler';

export const getAppointmentErrorMessage = (error: unknown): string => {
  if (error && typeof error === 'object' && 'message' in error && typeof error.message === 'string') {
    return error.message;
  }
  return normalizeApiError(error).message;
};
