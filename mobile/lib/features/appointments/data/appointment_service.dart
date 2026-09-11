import 'package:dio/dio.dart';

import '../../../core/api/api_client.dart';
import '../domain/appointment_models.dart';

abstract interface class AppointmentGateway {
  Future<AppointmentBookingOptions> loadBookingOptions();

  Future<List<AppointmentBookingResult>> loadMyAppointments();

  Future<AppointmentBookingResult> loadAppointment(int appointmentId);

  Future<AppointmentBookingResult> createAppointment({
    required int vehicleId,
    required int branchId,
    required DateTime appointmentTime,
    String? note,
  });

  Future<AppointmentBookingResult> cancelAppointment(int appointmentId);
}

class AppointmentException implements Exception {
  const AppointmentException(this.message);

  final String message;

  @override
  String toString() => message;
}

class AppointmentService implements AppointmentGateway {
  const AppointmentService(this._apiClient);

  final ApiClient _apiClient;

  @override
  Future<AppointmentBookingOptions> loadBookingOptions() async {
    try {
      final responses = await Future.wait([
        _apiClient.dio.get<Map<String, dynamic>>('/vehicles'),
        _apiClient.dio.get<Map<String, dynamic>>('/branches'),
      ]);
      final vehicles = _readList(responses[0].data, 'danh sách xe')
          .map(VehicleOption.fromJson)
          .where((vehicle) => vehicle.isActive)
          .toList(growable: false);
      final branches = _readList(
        responses[1].data,
        'danh sách chi nhánh',
      ).map(BranchOption.fromJson).toList(growable: false);
      return AppointmentBookingOptions(vehicles: vehicles, branches: branches);
    } on AppointmentException {
      rethrow;
    } on DioException catch (error) {
      throw AppointmentException(_messageFor(error));
    } on Object {
      throw const AppointmentException(
        'Dữ liệu đặt lịch từ máy chủ không hợp lệ.',
      );
    }
  }

  @override
  Future<List<AppointmentBookingResult>> loadMyAppointments() async {
    try {
      final response = await _apiClient.dio.get<Map<String, dynamic>>(
        '/appointments',
      );
      final appointments = _readList(
        response.data,
        'lịch hẹn của bạn',
      ).map(AppointmentBookingResult.fromJson).toList(growable: false);
      appointments.sort((left, right) {
        final leftActive = left.canCancel;
        final rightActive = right.canCancel;
        if (leftActive != rightActive) return leftActive ? -1 : 1;
        return leftActive
            ? left.appointmentTime.compareTo(right.appointmentTime)
            : right.appointmentTime.compareTo(left.appointmentTime);
      });
      return appointments;
    } on AppointmentException {
      rethrow;
    } on DioException catch (error) {
      throw AppointmentException(_messageFor(error));
    } on Object {
      throw const AppointmentException(
        'Dữ liệu lịch hẹn từ máy chủ không hợp lệ.',
      );
    }
  }

  @override
  Future<AppointmentBookingResult> loadAppointment(int appointmentId) async {
    try {
      final response = await _apiClient.dio.get<Map<String, dynamic>>(
        '/appointments/$appointmentId',
      );
      final data = _readObject(response.data, 'chi tiết lịch hẹn');
      return AppointmentBookingResult.fromJson(data);
    } on AppointmentException {
      rethrow;
    } on DioException catch (error) {
      throw AppointmentException(_messageFor(error));
    } on Object {
      throw const AppointmentException(
        'Dữ liệu chi tiết lịch hẹn từ máy chủ không hợp lệ.',
      );
    }
  }

  @override
  Future<AppointmentBookingResult> createAppointment({
    required int vehicleId,
    required int branchId,
    required DateTime appointmentTime,
    String? note,
  }) async {
    try {
      final normalizedNote = note?.trim();
      final response = await _apiClient.dio.post<Map<String, dynamic>>(
        '/appointments',
        data: {
          'maXe': vehicleId,
          'maChiNhanh': branchId,
          'thoiGianHen': appointmentTime.toIso8601String(),
          'ghiChu': normalizedNote == null || normalizedNote.isEmpty
              ? null
              : normalizedNote,
        },
      );
      final data = _readObject(response.data, 'lịch hẹn');
      return AppointmentBookingResult.fromJson(data);
    } on AppointmentException {
      rethrow;
    } on DioException catch (error) {
      throw AppointmentException(_messageFor(error));
    } on Object {
      throw const AppointmentException(
        'Phản hồi đặt lịch từ máy chủ không hợp lệ.',
      );
    }
  }

  @override
  Future<AppointmentBookingResult> cancelAppointment(int appointmentId) async {
    try {
      final response = await _apiClient.dio.patch<Map<String, dynamic>>(
        '/appointments/$appointmentId/cancel',
      );
      final data = _readObject(response.data, 'hủy lịch hẹn');
      return AppointmentBookingResult.fromJson(data);
    } on AppointmentException {
      rethrow;
    } on DioException catch (error) {
      throw AppointmentException(_messageFor(error));
    } on Object {
      throw const AppointmentException(
        'Phản hồi hủy lịch từ máy chủ không hợp lệ.',
      );
    }
  }

  List<Map<String, dynamic>> _readList(
    Map<String, dynamic>? envelope,
    String label,
  ) {
    final data = envelope?['data'];
    if (envelope?['success'] != true || data is! List) {
      throw AppointmentException(
        envelope?['message']?.toString() ?? 'Không thể tải $label.',
      );
    }
    return data.whereType<Map<String, dynamic>>().toList(growable: false);
  }

  Map<String, dynamic> _readObject(
    Map<String, dynamic>? envelope,
    String label,
  ) {
    final data = envelope?['data'];
    if (envelope?['success'] != true || data is! Map<String, dynamic>) {
      throw AppointmentException(
        envelope?['message']?.toString() ?? 'Không thể xử lý $label.',
      );
    }
    return data;
  }

  String _messageFor(DioException error) {
    final body = error.response?.data;
    final serverMessage = body is Map<String, dynamic>
        ? body['message']?.toString()
        : null;
    if (serverMessage != null && serverMessage.trim().isNotEmpty) {
      return serverMessage.replaceFirst(RegExp(r'^Forbidden:\s*'), '');
    }
    if (error.response?.statusCode == 401) {
      return 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
    }
    if (error.response?.statusCode == 403) {
      return 'Bạn không có quyền thực hiện thao tác này.';
    }
    return 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.';
  }
}
