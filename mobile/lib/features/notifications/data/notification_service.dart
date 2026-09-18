import 'package:dio/dio.dart';

import '../../../core/api/api_client.dart';
import '../domain/notification_models.dart';

abstract interface class NotificationGateway {
  Future<NotificationSnapshot> load();
  Future<void> markRead(int id);
  Future<void> markAllRead();
}

class NotificationException implements Exception {
  const NotificationException(this.message);
  final String message;
}

class NotificationService implements NotificationGateway {
  const NotificationService(this._api);
  final ApiClient _api;

  @override
  Future<NotificationSnapshot> load() async {
    final results = await Future.wait([
      _request('/notifications'),
      _request('/notifications/unread-count'),
    ]);
    try {
      final items = (results[0] as List)
          .map(
            (item) => GarageNotification.fromJson(item as Map<String, dynamic>),
          )
          .toList();
      final count = (results[1] as Map<String, dynamic>)['unreadCount'] as int;
      if (count < 0) throw const FormatException();
      return NotificationSnapshot(items, count);
    } on Object {
      throw const NotificationException('Dữ liệu thông báo không hợp lệ.');
    }
  }

  @override
  Future<void> markRead(int id) async {
    await _request('/notifications/$id/read', patch: true);
  }

  @override
  Future<void> markAllRead() async {
    await _request('/notifications/read-all', patch: true);
  }

  Future<Object?> _request(String path, {bool patch = false}) async {
    try {
      final response = await _api.dio.request<Map<String, dynamic>>(
        path,
        options: Options(method: patch ? 'PATCH' : 'GET'),
      );
      final body = response.data;
      if (body?['success'] != true) {
        throw NotificationException(
          body?['message']?.toString() ??
              'Không thể xử lý thông báo. Vui lòng thử lại.',
        );
      }
      return body?['data'];
    } on NotificationException {
      rethrow;
    } on DioException catch (error) {
      if (error.response?.statusCode == 401) {
        throw const NotificationException(
          'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
        );
      }
      if (error.response?.statusCode == 403) {
        throw const NotificationException(
          'Bạn không có quyền truy cập thông báo này.',
        );
      }
      throw const NotificationException(
        'Không thể kết nối đến máy chủ. Vui lòng thử lại.',
      );
    } on Object {
      throw const NotificationException('Dữ liệu thông báo không hợp lệ.');
    }
  }
}
