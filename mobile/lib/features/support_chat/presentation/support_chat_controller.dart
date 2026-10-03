import 'dart:async';
import 'dart:math';
import 'package:flutter/widgets.dart';
import '../../appointments/domain/appointment_models.dart';
import '../data/support_chat_service.dart';

String supportRequestId() => List.generate(
  16,
  (_) => Random.secure().nextInt(256).toRadixString(16).padLeft(2, '0'),
).join();

class SupportChatController extends ChangeNotifier with WidgetsBindingObserver {
  SupportChatController(this.gateway, this.events) {
    WidgetsBinding.instance.addObserver(this);
  }
  final SupportGateway gateway;
  final SupportEvents events;
  String? _token;
  String? get token => _token;
  int _epoch = 0, _selection = 0;
  bool _disposed = false, _refreshing = false, _refreshAgain = false;
  bool _foreground = true;
  StreamSubscription<bool>? _subscription;
  Timer? _timer;
  bool? enabled;
  bool connected = false,
      busy = false,
      loading = false,
      visible = false,
      hasMore = false;
  String? error;
  List<SupportRoom> rooms = [];
  List<BranchOption> branches = [];
  List<SupportMessage> messages = [];
  int? selectedId;
  ({int room, String text, String key})? _pending;
  SupportRoom? get selected =>
      rooms.where((r) => r.id == selectedId).firstOrNull;
  int get unread => rooms.fold(0, (sum, room) => sum + room.unread);
  bool _valid(int epoch) => !_disposed && epoch == _epoch;
  void _notify() {
    if (!_disposed) notifyListeners();
  }

  void bind(String? token) {
    if (_token == token) return;
    _token = token;
    _epoch++;
    _selection++;
    _subscription?.cancel();
    _timer?.cancel();
    rooms = [];
    messages = [];
    branches = [];
    selectedId = null;
    enabled = null;
    error = null;
    connected = false;
    busy = false;
    loading = false;
    hasMore = false;
    _pending = null;
    _refreshing = false;
    _refreshAgain = false;
    visible = false;
    final epoch = _epoch;
    Future.microtask(() {
      if (!_valid(epoch)) return;
      _notify();
      if (token != null) initialize();
    });
  }

  Future<void> initialize() async {
    final token = _token;
    if (token == null || !_foreground || loading) return;
    final epoch = _epoch;
    error = null;
    loading = true;
    _notify();
    try {
      final available = await gateway.enabled(token);
      if (!_valid(epoch)) return;
      enabled = available;
      if (!available) return;
      final options = await gateway.branches(token);
      if (!_valid(epoch)) return;
      branches = options;
      await _subscription?.cancel();
      if (!_valid(epoch) || !_foreground) return;
      _subscription = events.watch(token).listen((online) {
        if (!_valid(epoch) || !_foreground) return;
        connected = online;
        _notify();
        if (online) refresh();
      });
      _timer?.cancel();
      _timer = Timer.periodic(const Duration(seconds: 20), (_) => refresh());
      await refresh();
    } catch (e) {
      if (_valid(epoch)) _failure(e);
    } finally {
      if (_valid(epoch)) {
        loading = false;
        _notify();
      }
    }
  }

  void _failure(Object e) {
    error = e is SupportException
        ? e.message
        : 'Không thể tải chat. Vui lòng thử lại.';
    if (e is SupportException && [401, 403, 404].contains(e.status)) {
      rooms = [];
      messages = [];
      selectedId = null;
      _selection++;
    }
  }

  Future<void> refresh() async {
    final token = _token;
    if (token == null || enabled != true || !_foreground) return;
    if (_refreshing) {
      _refreshAgain = true;
      return;
    }
    final epoch = _epoch;
    _refreshing = true;
    try {
      final result = await gateway.rooms(token);
      if (!_valid(epoch)) return;
      rooms = result;
      final id = selectedId;
      final selection = _selection;
      if (id != null && visible) {
        final page = await gateway.history(token, id);
        if (!_valid(epoch) || selection != _selection) return;
        if (messages.isEmpty) hasMore = page.hasMore;
        final map = {
          for (final m in messages) m.id: m,
          for (final m in page.messages) m.id: m,
        };
        messages = map.values.toList()..sort((a, b) => a.id.compareTo(b.id));
        if (messages.isNotEmpty && visible && _foreground) {
          await gateway.read(token, id, messages.last.id);
        }
      }
      error = null;
    } catch (e) {
      if (_valid(epoch)) _failure(e);
    } finally {
      if (_valid(epoch)) {
        _refreshing = false;
        _notify();
        if (_refreshAgain) {
          _refreshAgain = false;
          unawaited(refresh());
        }
      }
    }
  }

  void setVisible(bool value) {
    visible = value;
    if (value) {
      if (_timer == null || error != null) {
        initialize();
      } else {
        refresh();
      }
    }
  }

  Future<void> open(int branch) async {
    final token = _token;
    if (token == null || busy) return;
    final epoch = _epoch;
    busy = true;
    error = null;
    _notify();
    try {
      final room = await gateway.open(token, branch);
      if (!_valid(epoch)) return;
      rooms = [room, ...rooms.where((r) => r.id != room.id)];
      select(room.id);
    } catch (e) {
      if (_valid(epoch)) _failure(e);
    } finally {
      if (_valid(epoch)) {
        busy = false;
        _notify();
      }
    }
  }

  void select(int id) {
    if (selectedId == id) {
      refresh();
      return;
    }
    selectedId = id;
    _selection++;
    messages = [];
    hasMore = false;
    _pending = null;
    error = null;
    _notify();
    refresh();
  }

  Future<bool> send(String text) async {
    final token = _token;
    final id = selectedId;
    if (token == null || id == null || busy || text.trim().isEmpty) {
      return false;
    }
    final epoch = _epoch;
    final selection = _selection;
    final content = text.trim();
    if (_pending?.room != id || _pending?.text != content) {
      _pending = (room: id, text: content, key: supportRequestId());
    }
    busy = true;
    error = null;
    _notify();
    try {
      await gateway.send(token, id, content, _pending!.key);
      if (!_valid(epoch) || selection != _selection) return false;
      _pending = null;
      await refresh();
      return true;
    } catch (e) {
      if (_valid(epoch)) _failure(e);
      return false;
    } finally {
      if (_valid(epoch)) {
        busy = false;
        _notify();
      }
    }
  }

  Future<void> handoff() async {
    final token = _token;
    final id = selectedId;
    if (token == null || id == null || busy) return;
    final epoch = _epoch;
    busy = true;
    error = null;
    _notify();
    try {
      await gateway.handoff(token, id);
      if (_valid(epoch)) await refresh();
    } catch (e) {
      if (_valid(epoch)) _failure(e);
    } finally {
      if (_valid(epoch)) {
        busy = false;
        _notify();
      }
    }
  }

  Future<void> older() async {
    final token = _token;
    final id = selectedId;
    if (token == null || id == null || busy || messages.isEmpty || !hasMore) {
      return;
    }
    final epoch = _epoch;
    final selection = _selection;
    busy = true;
    _notify();
    try {
      final page = await gateway.history(token, id, before: messages.first.id);
      if (!_valid(epoch) || selection != _selection) return;
      messages = {
        for (final m in page.messages) m.id: m,
        for (final m in messages) m.id: m,
      }.values.toList()..sort((a, b) => a.id.compareTo(b.id));
      hasMore = page.hasMore;
    } catch (e) {
      if (_valid(epoch)) _failure(e);
    } finally {
      if (_valid(epoch)) {
        busy = false;
        _notify();
      }
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    final active = state == AppLifecycleState.resumed;
    if (_foreground == active) return;
    _foreground = active;
    if (active) {
      initialize();
    } else {
      _subscription?.cancel();
      _timer?.cancel();
      connected = false;
      _notify();
    }
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _disposed = true;
    _epoch++;
    _subscription?.cancel();
    _timer?.cancel();
    super.dispose();
  }
}
