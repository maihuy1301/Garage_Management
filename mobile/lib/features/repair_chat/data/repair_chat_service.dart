import 'dart:typed_data';
import 'package:dio/dio.dart';
import '../../../core/api/api_client.dart';

class RepairChatRoom {
  RepairChatRoom.fromJson(Map<String, dynamic> json)
    : plate = json['plate'] as String,
      vehicleName = json['vehicleName'] as String,
      branchName = json['branchName'] as String,
      orderId = (json['repairOrderId'] as num).toInt(),
      state = json['state'] as String,
      canSend = json['canSend'] == true;
  final String plate, vehicleName, branchName, state;
  final int orderId;
  final bool canSend;
  String get label => switch (state) {
    'OPEN' => 'Đang trao đổi',
    'ARCHIVED' => 'Đã bàn giao · Lưu trữ',
    'DELETED' => 'Đã xóa vĩnh viễn',
    'CLOSED' => 'Lần sửa đã hủy · Chỉ đọc',
    _ => 'Chờ duyệt phân công kỹ thuật viên',
  };
}

class RepairChatMessage {
  RepairChatMessage.fromJson(Map<String, dynamic> json)
    : id = (json['id'] as num).toInt(),
      name = json['senderName'] as String? ?? 'Nhân viên',
      role = json['senderRole'] as String,
      content = json['content'] as String,
      sentAt = DateTime.parse(json['sentAt'] as String),
      mine = json['mine'] == true;
  final int id;
  final String name, role, content;
  final DateTime sentAt;
  final bool mine;
  String get roleLabel => switch (role) {
    'FRONT_DESK' => 'Tiếp tân',
    'TECHNICIAN' => 'Kỹ thuật viên',
    'MANAGER' => 'Quản lý',
    'ADMIN' => 'Quản trị',
    _ => 'Nhân viên',
  };
}

class RepairChatHistory {
  RepairChatHistory.fromJson(Map<String, dynamic> json)
    : room = RepairChatRoom.fromJson(json['room'] as Map<String, dynamic>),
      messages = (json['messages'] as List)
          .map((m) => RepairChatMessage.fromJson(m as Map<String, dynamic>))
          .toList(),
      hasMore = json['hasMore'] == true;
  final RepairChatRoom room;
  final List<RepairChatMessage> messages;
  final bool hasMore;
}

class RepairChatException implements Exception {
  const RepairChatException(this.message, [this.status]);
  final String message;
  final int? status;
  bool get denied => [401, 403, 404].contains(status);
}

abstract interface class RepairChatGateway {
  Future<bool> enabled(String token);
  Future<RepairChatHistory> history(
    String token,
    int orderId, {
    int? before,
    int? after,
  });
  Future<void> send(String token, int orderId, String content, String clientId);
  Future<Uint8List?> image(String token, int orderId);
}

class RepairChatService implements RepairChatGateway {
  RepairChatService(this.api);
  final ApiClient api;
  Options _options(String token) =>
      Options(headers: {'Authorization': 'Bearer $token'});
  String _path(int id) => '/repair-chat/orders/$id';

  Future<T> _guard<T>(Future<T> Function() work) async {
    try {
      return await work();
    } on DioException catch (e) {
      final body = e.response?.data;
      throw RepairChatException(
        body is Map && body['message'] is String
            ? body['message'] as String
            : 'Không thể kết nối. Vui lòng thử lại.',
        e.response?.statusCode,
      );
    } on RepairChatException {
      rethrow;
    } catch (_) {
      throw const RepairChatException('Phản hồi chat từ máy chủ không hợp lệ.');
    }
  }

  Map<String, dynamic> _data(Map<String, dynamic>? envelope) {
    if (envelope?['success'] != true ||
        envelope?['data'] is! Map<String, dynamic>) {
      throw const RepairChatException('Phản hồi chat từ máy chủ không hợp lệ.');
    }
    return envelope!['data'] as Map<String, dynamic>;
  }

  @override
  Future<bool> enabled(String token) => _guard(() async {
    final response = await api.dio.get<Map<String, dynamic>>(
      '/repair-chat/capabilities',
      options: _options(token),
    );
    return _data(response.data)['enabled'] == true;
  });

  @override
  Future<RepairChatHistory> history(
    String token,
    int orderId, {
    int? before,
    int? after,
  }) => _guard(() async {
    final response = await api.dio.get<Map<String, dynamic>>(
      '${_path(orderId)}/messages',
      options: _options(token),
      queryParameters: {'before': ?before, 'after': ?after},
    );
    return RepairChatHistory.fromJson(_data(response.data));
  });

  @override
  Future<void> send(
    String token,
    int orderId,
    String content,
    String clientId,
  ) => _guard(() async {
    final response = await api.dio.post<Map<String, dynamic>>(
      '${_path(orderId)}/messages',
      options: _options(token),
      data: {'content': content, 'clientId': clientId},
    );
    _data(response.data);
  });

  @override
  Future<Uint8List?> image(String token, int orderId) async {
    try {
      final response = await api.dio.get<List<int>>(
        '${_path(orderId)}/vehicle-image',
        options: _options(token)..responseType = ResponseType.bytes,
      );
      return Uint8List.fromList(response.data!);
    } catch (_) {
      return null; // Profile pictures are optional; never fabricate a car picture.
    }
  }
}
