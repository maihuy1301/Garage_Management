import 'dart:async';
import 'dart:typed_data';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/features/repair_chat/data/repair_chat_service.dart';
import 'package:garage_mobile/features/repair_chat/presentation/repair_chat_controller.dart';

RepairChatHistory page({
  String state = 'OPEN',
  List<int> ids = const [1],
  bool more = false,
}) => RepairChatHistory.fromJson({
  'room': {
    'plate': '51A-12345',
    'vehicleName': 'Toyota',
    'branchName': 'Central',
    'repairOrderId': 20,
    'state': state,
    'canSend': state == 'OPEN',
  },
  'messages': [
    for (final id in ids)
      {
        'id': id,
        'senderName': 'Tiếp tân An',
        'senderRole': 'FRONT_DESK',
        'content': 'Tin $id',
        'sentAt': '2026-10-07T09:00:00',
        'mine': false,
      },
  ],
  'hasMore': more,
});

class FakeGateway implements RepairChatGateway {
  bool available = true, failSend = false;
  int historyCalls = 0;
  RepairChatHistory result = page();
  RepairChatException? failure;
  Completer<RepairChatHistory>? delayed;
  final keys = <String>[];
  final tokens = <String>[];
  int? lastBefore, lastAfter;
  @override
  Future<bool> enabled(String token) async => available;
  @override
  Future<Uint8List?> image(String token, int orderId) async => null;
  @override
  Future<RepairChatHistory> history(
    String token,
    int orderId, {
    int? before,
    int? after,
  }) async {
    historyCalls++;
    tokens.add(token);
    lastBefore = before;
    lastAfter = after;
    if (failure != null) throw failure!;
    return delayed != null ? delayed!.future : result;
  }

  @override
  Future<void> send(
    String token,
    int orderId,
    String content,
    String clientId,
  ) async {
    keys.add(clientId);
    if (failSend) throw const RepairChatException('Mất kết nối');
  }
}

Future<void> flush() => Future<void>.delayed(Duration.zero);

void main() {
  late FakeGateway api;
  late RepairChatController chat;
  setUp(() {
    api = FakeGateway();
    chat = RepairChatController(api, 20);
  });
  tearDown(() => chat.dispose());

  test('disabled capability never reads new tables through history', () async {
    api.available = false;
    chat.bind('tech');
    await flush();
    expect(chat.enabled, false);
    expect(chat.loading, false);
    expect(api.historyCalls, 0);
  });
  test('logout discards late private history', () async {
    api.delayed = Completer();
    chat.bind('tech');
    await flush();
    chat.bind(null);
    api.delayed!.complete(page());
    await flush();
    expect(chat.messages, isEmpty);
    expect(chat.room, isNull);
  });
  test('account switch ignores old response', () async {
    final previous = Completer<RepairChatHistory>();
    api.delayed = previous;
    chat.bind('old');
    await flush();
    api.delayed = null;
    api.result = page(ids: [5]);
    chat.bind('new');
    await flush();
    previous.complete(page(ids: [1]));
    await flush();
    expect(chat.messages.map((m) => m.id), [5]);
    expect(api.tokens, ['old', 'new']);
  });
  test('failed send reuses idempotency key until successful retry', () async {
    chat.bind('tech');
    await flush();
    api.failSend = true;
    expect(await chat.send('Kết quả kiểm tra'), false);
    api.failSend = false;
    expect(await chat.send('Kết quả kiểm tra'), true);
    expect(api.keys[0], api.keys[1]);
    await chat.send('Kết quả kiểm tra');
    expect(api.keys[2], isNot(api.keys[1]));
  });
  test('handover makes chat read-only', () async {
    chat.bind('tech');
    await flush();
    api.result = page(state: 'ARCHIVED');
    await chat.refresh();
    expect(chat.room!.canSend, false);
    expect(await chat.send('Tin mới'), false);
    expect(api.keys, isEmpty);
  });
  test('deletion clears all loaded pages', () async {
    chat.bind('tech');
    await flush();
    api.result = page(state: 'DELETED', ids: []);
    await chat.refresh();
    expect(chat.messages, isEmpty);
    expect(chat.room!.state, 'DELETED');
    expect(await chat.send('Tin mới'), false);
  });
  test('permission loss erases cached messages', () async {
    chat.bind('tech');
    await flush();
    api.failure = const RepairChatException('Mất quyền', 403);
    await chat.refresh();
    expect(chat.messages, isEmpty);
    expect(chat.room, isNull);
    expect(chat.error, 'Mất quyền');
  });
  test(
    'pagination merges without duplicates and poll keeps older cursor',
    () async {
      api.result = page(ids: [5, 6], more: true);
      chat.bind('tech');
      await flush();
      api.result = page(ids: [6, 7]);
      await chat.refresh();
      expect(api.lastAfter, 6);
      expect(chat.hasMore, true);
      api.result = page(ids: [3, 4]);
      await chat.refresh(older: true);
      expect(api.lastBefore, 5);
      expect(chat.messages.map((m) => m.id), [3, 4, 5, 6, 7]);
      expect(chat.hasMore, false);
    },
  );
}
