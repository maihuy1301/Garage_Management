class TechnicianRepairOrder {
  const TechnicianRepairOrder({
    required this.id,
    required this.status,
    this.licensePlate = '',
    this.brand = '',
    this.model = '',
    this.customerName = '',
    this.customerPhone = '',
    this.branchName = '',
    this.note,
    this.startedAt,
    this.completedAt,
  });

  factory TechnicianRepairOrder.fromJson(Map<String, dynamic> json) {
    return TechnicianRepairOrder(
      id: (json['maPhieuSuaChua'] as num).toInt(),
      status: json['trangThai']?.toString() ?? 'DA_PHAN_CONG',
      licensePlate: json['bienSoXe']?.toString() ?? '',
      brand: json['tenHangXe']?.toString() ?? json['hangXe']?.toString() ?? '',
      model: json['tenModel']?.toString() ?? json['modelXe']?.toString() ?? '',
      customerName: json['tenKhachHang']?.toString() ?? '',
      customerPhone: json['soDienThoaiKhachHang']?.toString() ?? '',
      branchName: json['tenChiNhanh']?.toString() ?? '',
      note: json['ghiChu']?.toString(),
      startedAt: _dateTime(json['thoiGianBatDau']),
      completedAt: _dateTime(json['thoiGianHoanTat']),
    );
  }

  final int id;
  final String status;
  final String licensePlate;
  final String brand;
  final String model;
  final String customerName;
  final String customerPhone;
  final String branchName;
  final String? note;
  final DateTime? startedAt;
  final DateTime? completedAt;

  String get vehicleDescription =>
      [brand, model].where((value) => value.trim().isNotEmpty).join(' ');

  bool get isLocked => status == 'HOAN_TAT' || status == 'HUY';
}

class TechnicianRepairItem {
  const TechnicianRepairItem({
    required this.id,
    required this.repairOrderId,
    required this.name,
    required this.status,
    this.category = '',
    this.quantity = 1,
    this.unitPrice = 0,
    this.total = 0,
  });

  factory TechnicianRepairItem.fromJson(Map<String, dynamic> json) {
    return TechnicianRepairItem(
      id: (json['maChiTiet'] as num).toInt(),
      repairOrderId: (json['maPhieuSuaChua'] as num).toInt(),
      name: json['tenDichVu']?.toString() ?? 'Dịch vụ',
      category: json['tenLoaiDichVu']?.toString() ?? '',
      quantity: (json['soLuong'] as num?)?.toInt() ?? 1,
      unitPrice: (json['donGia'] as num?)?.toDouble() ?? 0,
      total: (json['thanhTien'] as num?)?.toDouble() ?? 0,
      status: json['trangThai']?.toString() ?? 'CHO_XU_LY',
    );
  }

  final int id;
  final int repairOrderId;
  final String name;
  final String category;
  final int quantity;
  final double unitPrice;
  final double total;
  final String status;
}

class TechnicianRepairProgress {
  const TechnicianRepairProgress({
    required this.status,
    required this.percent,
    this.description,
    this.technicianName = '',
    this.occurredAt,
  });

  factory TechnicianRepairProgress.fromJson(Map<String, dynamic> json) {
    return TechnicianRepairProgress(
      status: json['trangThai']?.toString() ?? '',
      percent: (json['phanTramHoanThanh'] as num?)?.toInt() ?? 0,
      description: json['moTa']?.toString(),
      technicianName: json['tenNhanVien']?.toString() ?? '',
      occurredAt: _dateTime(json['thoiGian']),
    );
  }

  final String status;
  final int percent;
  final String? description;
  final String technicianName;
  final DateTime? occurredAt;
}

class TechnicianRepairDetail {
  const TechnicianRepairDetail({
    required this.order,
    required this.items,
    required this.progressHistory,
  });

  final TechnicianRepairOrder order;
  final List<TechnicianRepairItem> items;
  final List<TechnicianRepairProgress> progressHistory;
}

DateTime? _dateTime(Object? value) {
  if (value == null) return null;
  return DateTime.tryParse(value.toString());
}
