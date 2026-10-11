import 'dart:math';
import 'package:flutter/foundation.dart';
import '../data/repair_chat_service.dart';

/// Polling is owned by the visible page. Bind invalidates all in-flight work on logout.
class RepairChatController extends ChangeNotifier {
  RepairChatController(this.gateway, this.orderId);
  final RepairChatGateway gateway;
  final int orderId;
  String? _token;
  int _generation = 0;
  bool _disposed = false;
  bool loading = false, busy = false, enabled = false, hasMore = false;
  bool _polling = false;
  String? error;
  RepairChatRoom? room;
  Uint8List? image;
  List<RepairChatMessage> messages = [];
  String? _pendingText, _pendingId;

  bool _valid(int generation) =>
      !_disposed && generation == _generation && _token != null;
  void _emit() {
    if (!_disposed) notifyListeners();
  }

  void bind(String? token) {
    if (_token == token) return;
    _token = token;
    _generation++;
    room = null;
    messages = [];
    image = null;
    error = null;
    loading = false;
    busy = false;
    enabled = false;
    hasMore = false;
    _polling = false;
    _pendingId = null;
    _pendingText = null;
    _emit();
    if (token != null) load();
  }

  void _fail(RepairChatException exception) {
    error = exception.message;
    if (exception.denied) {
      room = null;
      messages = [];
      image = null;
      hasMore = false;
    }
  }

  Future<void> load() async {
    final token = _token;
    if (token == null || loading || busy || _polling) return;
    final generation = _generation;
    loading = true;
    error = null;
    _emit();
    try {
      final available = await gateway.enabled(token);
      if (!_valid(generation)) return;
      enabled = available;
      if (!available) {
        room = null;
        messages = [];
        image = null;
        return;
      }
      final page = await gateway.history(token, orderId);
      if (!_valid(generation)) return;
      _apply(page, replace: true);
      final picture = await gateway.image(token, orderId);
      if (_valid(generation)) image = picture;
    } on RepairChatException catch (e) {
      if (_valid(generation)) _fail(e);
    } finally {
      if (_valid(generation)) {
        loading = false;
        _emit();
      }
    }
  }

  void _apply(
    RepairChatHistory page, {
    bool replace = false,
    bool older = false,
  }) {
    room = page.room;
    if (room!.state == 'DELETED') {
      messages = [];
      hasMore = false;
      return;
    }
    final byId = {
      if (!replace)
        for (final m in messages) m.id: m,
      for (final m in page.messages) m.id: m,
    };
    messages = byId.values.toList()..sort((a, b) => a.id.compareTo(b.id));
    if (replace || older) hasMore = page.hasMore;
  }

  Future<void> refresh({bool older = false}) async {
    final token = _token;
    if (token == null ||
        room == null ||
        room!.state == 'DELETED' ||
        loading ||
        busy ||
        _polling) {
      return;
    }
    final generation = _generation;
    _polling = true;
    if (older) {
      busy = true;
      _emit();
    }
    try {
      final page = await gateway.history(
        token,
        orderId,
        before: older && messages.isNotEmpty ? messages.first.id : null,
        after: older
            ? null
            : messages.isEmpty
            ? 0
            : messages.last.id,
      );
      if (_valid(generation)) {
        _apply(page, older: older);
        error = null;
      }
    } on RepairChatException catch (e) {
      if (_valid(generation)) _fail(e);
    } finally {
      if (_valid(generation)) {
        _polling = false;
        busy = false;
        _emit();
      }
    }
  }

  Future<bool> send(String value) async {
    final token = _token, content = value.trim();
    if (token == null ||
        content.isEmpty ||
        content.length > 2000 ||
        room?.canSend != true ||
        loading ||
        busy ||
        _polling) {
      return false;
    }
    if (_pendingText != content) {
      _pendingText = content;
      _pendingId = List.generate(
        16,
        (_) => Random.secure().nextInt(256).toRadixString(16).padLeft(2, '0'),
      ).join();
    }
    final generation = _generation;
    busy = true;
    error = null;
    _emit();
    bool sent = false;
    try {
      await gateway.send(token, orderId, content, _pendingId!);
      if (!_valid(generation)) return false;
      sent = true;
      _pendingText = null;
      _pendingId = null;
      final page = await gateway.history(
        token,
        orderId,
        after: messages.isEmpty ? 0 : messages.last.id,
      );
      if (_valid(generation)) _apply(page);
    } on RepairChatException catch (e) {
      if (_valid(generation)) _fail(e);
    } finally {
      if (_valid(generation)) {
        busy = false;
        _emit();
      }
    }
    return sent && _valid(generation);
  }

  @override
  void dispose() {
    _disposed = true;
    _generation++;
    _token = null;
    super.dispose();
  }
}
