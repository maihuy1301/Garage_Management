import 'dart:async';
import 'package:dio/dio.dart';
import 'package:stomp_dart_client/stomp_dart_client.dart';
import '../../../core/api/api_client.dart';
import '../../appointments/domain/appointment_models.dart';
import '../../notifications/data/notification_events.dart';

class SupportRoom {
  SupportRoom.fromJson(Map<String, dynamic> j)
    : id = (j['id'] as num).toInt(),
      branchId = (j['branchId'] as num).toInt(),
      branchName = j['branchName'] as String,
      status = j['status'] as String,
      agentName = j['agentName'] as String?,
      botPending = j['botPending'] == true,
      unread = (j['unreadCount'] as num).toInt();
  final int id, branchId, unread;
  final String branchName, status;
  final String? agentName;
  final bool botPending;
  String get label => switch (status) {
    'WAITING' => 'Đang chờ tiếp tân nhận hỗ trợ',
    'HUMAN' => '${agentName ?? 'Tiếp tân'} đang hỗ trợ',
    _ =>
      botPending
          ? 'Trợ lý đang trả lời…'
          : 'Trợ lý tự động · Tư vấn & đặt lịch',
  };
}

class SupportMessage {
  SupportMessage.fromJson(Map<String, dynamic> j)
    : id = (j['id'] as num).toInt(),
      type = j['senderType'] as String,
      name = j['senderName'] as String,
      content = j['content'] as String,
      time = DateTime.parse(j['createdAt'] as String);
  final int id;
  final String type, name, content;
  final DateTime time;
}

class SupportHistory {
  const SupportHistory(this.messages, this.hasMore);
  final List<SupportMessage> messages;
  final bool hasMore;
}

class SupportException implements Exception {
  const SupportException(this.message, [this.status]);
  final String message;
  final int? status;
  @override
  String toString() => message;
}

abstract interface class SupportGateway {
  Future<bool> enabled(String token);
  Future<List<BranchOption>> branches(String token);
  Future<List<SupportRoom>> rooms(String token);
  Future<SupportRoom> open(String token, int branch);
  Future<SupportHistory> history(String token, int room, {int? before});
  Future<void> send(String token, int room, String text, String clientId);
  Future<void> handoff(String token, int room);
  Future<void> read(String token, int room, int lastReadId);
  Future<AppointmentBookingResult> book(
    String token,
    int room,
    String key,
    Map<String, dynamic> payload,
  );
}

class BranchSuggestion {
  BranchSuggestion.fromJson(Map<String, dynamic> j)
    : id = (j['id'] as num).toInt(),
      name = j['name'] as String,
      address = j['address'] as String,
      reason = j['reason'] as String,
      distanceKm = (j['distanceKm'] as num?)?.toDouble();
  final int id;
  final String name, address, reason;
  final double? distanceKm;
}

class BranchSuggestions {
  BranchSuggestions.fromJson(Map<String, dynamic> j)
    : explanation = j['explanation'] as String,
      branches = (j['branches'] as List)
          .map((b) => BranchSuggestion.fromJson(Map<String, dynamic>.from(b)))
          .toList();
  final String explanation;
  final List<BranchSuggestion> branches;
}

abstract interface class SupportBranchGateway {
  Future<BranchSuggestions> suggestions(
    String token, {
    double? latitude,
    double? longitude,
  });
}

class SupportChatService implements SupportGateway, SupportBranchGateway {
  SupportChatService(this.api);
  final ApiClient api;
  static const base = '/support-chat';
  @override
  Future<BranchSuggestions> suggestions(
    String token, {
    double? latitude,
    double? longitude,
  }) async => BranchSuggestions.fromJson(
    Map<String, dynamic>.from(
      await _request(
        token,
        latitude == null ? 'GET' : 'POST',
        '$base/branch-suggestions',
        data: latitude == null
            ? null
            : {'latitude': latitude, 'longitude': longitude},
      ),
    ),
  );
  Future<dynamic> _request(
    String token,
    String method,
    String path, {
    dynamic data,
    String? key,
  }) async {
    try {
      final response = await api.dio.request<Map<String, dynamic>>(
        path,
        data: data,
        options: Options(
          method: method,
          headers: {'Authorization': 'Bearer $token', 'Idempotency-Key': ?key},
        ),
      );
      if (response.data?['success'] != true) {
        throw const SupportException('Không thể tải chat. Vui lòng thử lại.');
      }
      return response.data?['data'];
    } on DioException catch (e) {
      final body = e.response?.data;
      final fallback = switch (e.type) {
        DioExceptionType.connectionTimeout ||
        DioExceptionType.receiveTimeout ||
        DioExceptionType.sendTimeout =>
          'Kết nối chat quá thời gian chờ. Vui lòng thử lại.',
        DioExceptionType.connectionError =>
          'Không kết nối được máy chủ. Kiểm tra mạng rồi thử lại.',
        _ =>
          e.response?.statusCode == 401
              ? 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
              : 'Không thể tải chat${e.response?.statusCode == null ? '' : ' (HTTP ${e.response!.statusCode})'}. Vui lòng thử lại.',
      };
      throw SupportException(
        body is Map ? body['message']?.toString() ?? fallback : fallback,
        e.response?.statusCode,
      );
    }
  }

  @override
  Future<bool> enabled(String token) async =>
      (await _request(token, 'GET', '$base/capabilities'))['enabled'] == true;
  @override
  Future<List<BranchOption>> branches(String token) async =>
      (await _request(token, 'GET', '/branches') as List)
          .where((j) => j['trangThai'] != false)
          .map((j) => BranchOption.fromJson(Map<String, dynamic>.from(j)))
          .toList();
  @override
  Future<List<SupportRoom>> rooms(String token) async =>
      (await _request(token, 'GET', base) as List)
          .map((j) => SupportRoom.fromJson(Map<String, dynamic>.from(j)))
          .toList();
  @override
  Future<SupportRoom> open(String token, int branch) async =>
      SupportRoom.fromJson(
        Map<String, dynamic>.from(
          await _request(token, 'POST', base, data: {'branchId': branch}),
        ),
      );
  @override
  Future<SupportHistory> history(String token, int room, {int? before}) async {
    final j = await _request(
      token,
      'GET',
      '$base/$room/messages${before == null ? '' : '?before=$before'}',
    );
    return SupportHistory(
      (j['messages'] as List)
          .map((m) => SupportMessage.fromJson(Map<String, dynamic>.from(m)))
          .toList(),
      j['hasMore'] == true,
    );
  }

  @override
  Future<void> send(
    String token,
    int room,
    String text,
    String clientId,
  ) async {
    await _request(
      token,
      'POST',
      '$base/$room/messages',
      data: {'content': text, 'clientId': clientId},
    );
  }

  @override
  Future<void> handoff(String token, int room) async {
    await _request(token, 'POST', '$base/$room/handoff');
  }

  @override
  Future<void> read(String token, int room, int lastReadId) async {
    await _request(
      token,
      'PATCH',
      '$base/$room/read',
      data: {'lastReadId': lastReadId},
    );
  }

  @override
  Future<AppointmentBookingResult> book(
    String token,
    int room,
    String key,
    Map<String, dynamic> payload,
  ) async => AppointmentBookingResult.fromJson(
    Map<String, dynamic>.from(
      await _request(
        token,
        'POST',
        '$base/$room/appointments',
        data: payload,
        key: key,
      ),
    ),
  );
}

abstract interface class SupportEvents {
  Stream<bool> watch(String token);
}

class StompSupportEvents implements SupportEvents {
  StompSupportEvents(this.apiBaseUrl);
  final String apiBaseUrl;
  @override
  Stream<bool> watch(String token) {
    late StreamController<bool> stream;
    StompClient? client;
    var cancelled = false;
    void emit(bool connected) {
      if (!cancelled) stream.add(connected);
    }

    stream = StreamController<bool>(
      onListen: () {
        client = StompClient(
          config: StompConfig(
            url: StompNotificationEvents.socketUrl(apiBaseUrl),
            stompConnectHeaders: {'Authorization': 'Bearer $token'},
            connectionTimeout: const Duration(seconds: 10),
            reconnectDelay: const Duration(seconds: 4),
            onConnect: (_) {
              if (cancelled) return;
              client!.subscribe(
                destination: '/user/queue/support-chat',
                callback: (_) => emit(true),
              );
              emit(true); // Reconcile messages missed offline.
            },
            onWebSocketDone: () => emit(false),
            onWebSocketError: (_) => emit(false),
            onStompError: (_) => emit(false),
          ),
        );
        client!.activate();
      },
      onCancel: () {
        cancelled = true;
        client?.deactivate();
      },
    );
    return stream.stream;
  }
}
