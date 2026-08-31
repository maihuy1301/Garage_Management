import 'package:dio/dio.dart';

import '../api/api_client.dart';
import '../storage/secure_session_storage.dart';
import 'auth_exception.dart';
import 'auth_session.dart';
import 'jwt_decoder.dart';

class AuthRepository {
  const AuthRepository(this._apiClient, this._sessionStorage);

  final ApiClient _apiClient;
  final SecureSessionStorage _sessionStorage;

  Future<AuthSession?> restoreSession() async {
    final token = await _sessionStorage.readToken();
    if (token == null || token.isEmpty) return null;

    try {
      final payload = JwtDecoder.decode(token);
      if (payload.isExpired) {
        await _sessionStorage.clear();
        return null;
      }
      await _apiClient.dio.get<Map<String, dynamic>>('/auth/me');
      return _sessionFromToken(token);
    } on DioException catch (error) {
      if (error.response?.statusCode == 401 ||
          error.response?.statusCode == 403) {
        await _sessionStorage.clear();
        return null;
      }
      return _sessionFromToken(token);
    } on FormatException {
      await _sessionStorage.clear();
      return null;
    }
  }

  Future<AuthSession> login({
    required String username,
    required String password,
  }) async {
    try {
      final response = await _apiClient.dio.post<Map<String, dynamic>>(
        '/auth/login',
        data: {'tenDangNhap': username.trim(), 'matKhau': password},
      );
      final envelope = response.data;
      final data = envelope?['data'];
      if (envelope?['success'] != true || data is! Map<String, dynamic>) {
        throw AuthException(
          envelope?['message']?.toString() ??
              'Phản hồi đăng nhập không hợp lệ.',
        );
      }

      final token = data['accessToken']?.toString();
      if (token == null || token.isEmpty) {
        throw const AuthException('Máy chủ không trả về access token.');
      }
      final payload = JwtDecoder.decode(token);
      final session = AuthSession(
        accessToken: token,
        userId: (data['maNguoiDung'] as num?)?.toInt(),
        username: data['tenDangNhap']?.toString() ?? payload.subject,
        fullName: data['hoTen']?.toString(),
        roles: payload.roles,
      );
      await _sessionStorage.writeToken(token);
      return session;
    } on AuthException {
      rethrow;
    } on DioException catch (error) {
      final body = error.response?.data;
      final serverMessage = body is Map<String, dynamic>
          ? body['message']?.toString()
          : null;
      if (error.response?.statusCode == 401) {
        throw const AuthException('Tên đăng nhập hoặc mật khẩu không đúng.');
      }
      throw AuthException(
        serverMessage ??
            'Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại kết nối.',
      );
    } on FormatException {
      throw const AuthException('Access token từ máy chủ không hợp lệ.');
    }
  }

  Future<void> logout() => _sessionStorage.clear();

  AuthSession _sessionFromToken(String token) {
    final payload = JwtDecoder.decode(token);
    return AuthSession(
      accessToken: token,
      username: payload.subject,
      roles: payload.roles,
    );
  }
}
