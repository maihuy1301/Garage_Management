class VehicleOption {
  const VehicleOption({
    required this.id,
    required this.licensePlate,
    required this.brand,
    required this.model,
    this.isActive = true,
  });

  factory VehicleOption.fromJson(Map<String, dynamic> json) {
    return VehicleOption(
      id: (json['maXe'] as num).toInt(),
      licensePlate: json['bienSo']?.toString() ?? '',
      brand: json['hangXe']?.toString() ?? '',
      model: json['model']?.toString() ?? '',
      isActive: json['trangThai'] != false,
    );
  }

  final int id;
  final String licensePlate;
  final String brand;
  final String model;
  final bool isActive;

  String get description =>
      [brand, model].where((value) => value.trim().isNotEmpty).join(' ');
}

class BranchOption {
  const BranchOption({
    required this.id,
    required this.name,
    required this.address,
  });

  factory BranchOption.fromJson(Map<String, dynamic> json) {
    return BranchOption(
      id: (json['maChiNhanh'] as num).toInt(),
      name: json['tenChiNhanh']?.toString() ?? '',
      address: json['diaChi']?.toString() ?? '',
    );
  }

  final int id;
  final String name;
  final String address;
}

class ServicePartOption {
  const ServicePartOption({
    required this.id,
    required this.name,
    required this.quantity,
    required this.unitPrice,
    required this.totalPrice,
    this.unit,
  });

  factory ServicePartOption.fromJson(Map<String, dynamic> json) {
    return ServicePartOption(
      id: (json['maPhuTung'] as num).toInt(),
      name: json['tenPhuTung']?.toString() ?? '',
      quantity: json['soLuong'] != null ? (json['soLuong'] as num).toInt() : 1,
      unitPrice: json['donGia'] != null ? (json['donGia'] as num).toDouble() : 0.0,
      totalPrice: json['thanhTien'] != null ? (json['thanhTien'] as num).toDouble() : 0.0,
      unit: json['donViTinh']?.toString(),
    );
  }

  final int id;
  final String name;
  final int quantity;
  final double unitPrice;
  final double totalPrice;
  final String? unit;
}

class ServiceOption {
  const ServiceOption({
    required this.id,
    required this.name,
    this.categoryName,
    this.description,
    this.price,
    this.estimatedDurationMinutes,
    this.isActive = true,
    this.parts = const [],
    this.estimatedPartsCost,
    this.estimatedTotalCost,
  });

  factory ServiceOption.fromJson(Map<String, dynamic> json) {
    final rawParts = json['parts'] as List<dynamic>?;
    final parts = rawParts != null
        ? rawParts
            .whereType<Map<String, dynamic>>()
            .map(ServicePartOption.fromJson)
            .toList(growable: false)
        : const <ServicePartOption>[];

    final laborPrice = json['donGia'] != null ? (json['donGia'] as num).toDouble() : null;
    final partsCost = json['tienPhuTungDuKien'] != null ? (json['tienPhuTungDuKien'] as num).toDouble() : null;
    final totalCost = json['tongGiaDuKien'] != null ? (json['tongGiaDuKien'] as num).toDouble() : null;

    return ServiceOption(
      id: (json['maDichVu'] as num).toInt(),
      name: json['tenDichVu']?.toString() ?? '',
      categoryName: json['tenLoaiDichVu']?.toString(),
      description: json['moTa']?.toString(),
      price: laborPrice,
      estimatedDurationMinutes: json['thoiGianDuKien'] != null
          ? (json['thoiGianDuKien'] as num).toInt()
          : null,
      isActive: json['trangThai'] != false,
      parts: parts,
      estimatedPartsCost: partsCost,
      estimatedTotalCost: totalCost ??
          (laborPrice != null && partsCost != null ? laborPrice + partsCost : laborPrice),
    );
  }

  final int id;
  final String name;
  final String? categoryName;
  final String? description;
  final double? price;
  final int? estimatedDurationMinutes;
  final bool isActive;
  final List<ServicePartOption> parts;
  final double? estimatedPartsCost;
  final double? estimatedTotalCost;
}

class AppointmentServiceItem {
  const AppointmentServiceItem({
    required this.id,
    required this.name,
    this.description,
    this.price,
    this.estimatedDurationMinutes,
    this.quantity = 1,
    this.note,
    this.parts = const [],
    this.estimatedPartsCost,
    this.estimatedTotalCost,
  });

  factory AppointmentServiceItem.fromJson(Map<String, dynamic> json) {
    final rawParts = json['parts'] as List<dynamic>?;
    final parts = rawParts != null
        ? rawParts
            .whereType<Map<String, dynamic>>()
            .map(ServicePartOption.fromJson)
            .toList(growable: false)
        : const <ServicePartOption>[];

    final laborPrice = json['donGia'] != null ? (json['donGia'] as num).toDouble() : null;
    final partsCost = json['tienPhuTungDuKien'] != null ? (json['tienPhuTungDuKien'] as num).toDouble() : null;
    final totalCost = json['tongGiaDuKien'] != null ? (json['tongGiaDuKien'] as num).toDouble() : null;

    return AppointmentServiceItem(
      id: (json['maDichVu'] as num).toInt(),
      name: json['tenDichVu']?.toString() ?? '',
      description: json['moTa']?.toString(),
      price: laborPrice,
      estimatedDurationMinutes: json['thoiGianDuKien'] != null
          ? (json['thoiGianDuKien'] as num).toInt()
          : null,
      quantity: json['soLuong'] != null ? (json['soLuong'] as num).toInt() : 1,
      note: json['ghiChu']?.toString(),
      parts: parts,
      estimatedPartsCost: partsCost,
      estimatedTotalCost: totalCost ??
          (laborPrice != null && partsCost != null ? laborPrice + partsCost : laborPrice),
    );
  }

  final int id;
  final String name;
  final String? description;
  final double? price;
  final int? estimatedDurationMinutes;
  final int quantity;
  final String? note;
  final List<ServicePartOption> parts;
  final double? estimatedPartsCost;
  final double? estimatedTotalCost;
}

class AppointmentBookingOptions {
  const AppointmentBookingOptions({
    required this.vehicles,
    required this.branches,
    this.services = const [],
  });

  final List<VehicleOption> vehicles;
  final List<BranchOption> branches;
  final List<ServiceOption> services;
}

class AppointmentBookingResult {
  const AppointmentBookingResult({
    required this.id,
    required this.status,
    required this.appointmentTime,
    required this.vehicleLicensePlate,
    required this.branchName,
    this.vehicleBrand = '',
    this.vehicleModel = '',
    this.note,
    this.createdAt,
    this.services = const [],
  });

  factory AppointmentBookingResult.fromJson(Map<String, dynamic> json) {
    return AppointmentBookingResult(
      id: (json['maDatLich'] as num).toInt(),
      status: json['trangThai']?.toString() ?? 'CHO_XAC_NHAN',
      appointmentTime: DateTime.parse(json['thoiGianHen'].toString()),
      vehicleLicensePlate: json['bienSoXe']?.toString() ?? '',
      branchName: json['tenChiNhanh']?.toString() ?? '',
      vehicleBrand: json['hangXe']?.toString() ?? '',
      vehicleModel: json['modelXe']?.toString() ?? '',
      note: json['ghiChu']?.toString(),
      createdAt: json['ngayDat'] == null
          ? null
          : DateTime.tryParse(json['ngayDat'].toString()),
      services: (json['dichVu'] as List<dynamic>?)
              ?.whereType<Map<String, dynamic>>()
              .map(AppointmentServiceItem.fromJson)
              .toList(growable: false) ??
          const [],
    );
  }

  final int id;
  final String status;
  final DateTime appointmentTime;
  final String vehicleLicensePlate;
  final String branchName;
  final String vehicleBrand;
  final String vehicleModel;
  final String? note;
  final DateTime? createdAt;
  final List<AppointmentServiceItem> services;

  bool get canCancel => status == 'CHO_XAC_NHAN' || status == 'DA_XAC_NHAN';

  bool get isCancelled =>
      status == 'HUY' || status == 'DA_HUY' || status == 'KHONG_DEN';

  int get progressStep => switch (status) {
    'CHO_XAC_NHAN' => 0,
    'DA_XAC_NHAN' => 1,
    'DA_TIEP_NHAN' => 2,
    'HOAN_TAT' => 3,
    _ => 0,
  };

  String get vehicleDescription => [
    vehicleBrand,
    vehicleModel,
  ].where((value) => value.trim().isNotEmpty).join(' ');
}
