export interface BrandResponse {
  maHangXe: number;
  tenHangXe: string;
  trangThai?: boolean;
}

export interface CreateBrandRequest {
  tenHangXe: string;
}

export interface ModelResponse {
  maModel: number;
  maHangXe: number;
  tenHangXe?: string;
  tenModel: string;
  trangThai?: boolean;
}

export interface CreateModelRequest {
  tenModel: string;
}
