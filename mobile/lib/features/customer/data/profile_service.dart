import 'package:dio/dio.dart';
import '../../../core/api/api_client.dart';

class CustomerProfile {
  CustomerProfile.fromJson(Map<String, dynamic> json)
    : username = json['tenDangNhap'] as String,
      name = json['hoTen'] as String? ?? '',
      email = json['email'] as String? ?? '',
      phone = json['soDienThoai'] as String? ?? '',
      address = json['diaChi'] as String? ?? '',
      birthday = DateTime.tryParse(json['ngaySinh'] as String? ?? '');
  final String username, name, email, phone, address;
  final DateTime? birthday;
}

abstract interface class ProfileGateway {
  Future<CustomerProfile> load();
  Future<CustomerProfile> save(Map<String, dynamic> fields);
}

class ProfileException implements Exception {
  const ProfileException(this.message);
  final String message;
}

class ProfileService implements ProfileGateway {
  const ProfileService(this.api);
  final ApiClient api;
  @override
  Future<CustomerProfile> load() => _request();
  @override
  Future<CustomerProfile> save(Map<String, dynamic> fields) => _request(fields);

  Future<CustomerProfile> _request([Map<String, dynamic>? fields]) async {
    try {
      final response = fields == null
          ? await api.dio.get<Map<String, dynamic>>('/customers/me')
          : await api.dio.put<Map<String, dynamic>>(
              '/customers/me',
              data: {
                for (final key in [
                  'hoTen',
                  'email',
                  'soDienThoai',
                  'diaChi',
                  'ngaySinh',
                ])
                  if (fields.containsKey(key)) key: fields[key],
              },
            );
      if (response.data?['success'] != true) {
        throw const ProfileException('Chưa thể tải hoặc lưu hồ sơ.');
      }
      return CustomerProfile.fromJson(
        response.data!['data'] as Map<String, dynamic>,
      );
    } on DioException catch (error) {
      final status = error.response?.statusCode;
      final data = error.response?.data;
      throw ProfileException(switch (status) {
        401 => 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
        403 => 'Bạn không có quyền truy cập hồ sơ này.',
        400 || 409 =>
          data is Map && data['message'] is String
              ? data['message'] as String
              : 'Thông tin không hợp lệ hoặc đã được sử dụng.',
        _ => 'Không thể kết nối. Vui lòng thử lại.',
      });
    } on ProfileException {
      rethrow;
    } on Object {
      throw const ProfileException('Dữ liệu hồ sơ không hợp lệ.');
    }
  }
}
