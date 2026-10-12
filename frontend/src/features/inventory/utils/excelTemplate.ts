import * as XLSX from 'xlsx';
import { PartResponse } from '@/types/part.types';

/**
 * Tải file Excel mẫu để thêm hàng loạt phụ tùng vào danh mục (Admin)
 */
export const downloadPartCatalogTemplate = () => {
  const data = [
    {
      'Mã phụ tùng (*)': 'BUG-NGK-01',
      'Tên phụ tùng (*)': 'Bugi NGK Laser Iridium chân dài',
      'Đơn vị tính': 'Cái',
      'Giá nhập (VNĐ)': 150000,
      'Giá bán (VNĐ)': 220000,
    },
    {
      'Mã phụ tùng (*)': 'LOC-NHOT-TOY-01',
      'Tên phụ tùng (*)': 'Lọc nhớt Toyota Vios/Altis chính hãng',
      'Đơn vị tính': 'Cái',
      'Giá nhập (VNĐ)': 85000,
      'Giá bán (VNĐ)': 130000,
    },
    {
      'Mã phụ tùng (*)': 'DAU-CASTROL-5W30',
      'Tên phụ tùng (*)': 'Dầu nhớt Castrol Magnatec 5W-30 (Can 4L)',
      'Đơn vị tính': 'Can',
      'Giá nhập (VNĐ)': 450000,
      'Giá bán (VNĐ)': 650000,
    },
  ];

  const ws = XLSX.utils.json_to_sheet(data);

  // Điều chỉnh độ rộng cột
  ws['!cols'] = [
    { wch: 22 }, // Mã phụ tùng
    { wch: 42 }, // Tên phụ tùng
    { wch: 14 }, // ĐVT
    { wch: 16 }, // Giá nhập
    { wch: 16 }, // Giá bán
  ];

  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Mau_Phu_Tung');

  XLSX.writeFile(wb, 'Mau_Nhap_Phu_Tung_AutoCare.xlsx');
};

/**
 * Tải file Excel mẫu để nhập kho hàng loạt (Admin & Manager)
 * Tự động điền danh sách tất cả các phụ tùng hiện có với số lượng = 0
 * để người dùng chỉ cần sửa số lượng món cần nhập thay vì phải gõ từng mã.
 */
export const downloadStockImportTemplate = (availableParts?: PartResponse[]) => {
  const activeParts = (availableParts || []).filter((p) => p.trangThai !== false);

  let importData: any[] = [];

  if (activeParts.length > 0) {
    importData = activeParts.map((p) => ({
      'Mã phụ tùng (*)': p.maPhuTungCode,
      'Tên phụ tùng (Tham khảo)': p.tenPhuTung,
      'Đơn vị tính': p.donViTinh || 'Cái',
      'Số lượng nhập (*)': 0,
      'Ghi chú': '',
    }));
  } else {
    // Dự phòng nếu hệ thống chưa có phụ tùng nào
    importData = [
      {
        'Mã phụ tùng (*)': 'BUG-NGK-01',
        'Tên phụ tùng (Tham khảo)': 'Bugi NGK Laser Iridium chân dài',
        'Đơn vị tính': 'Cái',
        'Số lượng nhập (*)': 0,
        'Ghi chú': 'Ví dụ nhập kho',
      },
      {
        'Mã phụ tùng (*)': 'LOC-NHOT-TOY-01',
        'Tên phụ tùng (Tham khảo)': 'Lọc nhớt Toyota Vios/Altis chính hãng',
        'Đơn vị tính': 'Cái',
        'Số lượng nhập (*)': 0,
        'Ghi chú': '',
      },
    ];
  }

  const ws = XLSX.utils.json_to_sheet(importData);
  ws['!cols'] = [
    { wch: 22 }, // Mã phụ tùng
    { wch: 42 }, // Tên phụ tùng (Tham khảo)
    { wch: 14 }, // Đơn vị tính
    { wch: 20 }, // Số lượng nhập
    { wch: 30 }, // Ghi chú
  ];

  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, 'Nhap_Kho');

  XLSX.writeFile(wb, 'Mau_Nhap_Kho_Hang_Loat_AutoCare.xlsx');
};
