import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:garage_mobile/features/appointments/domain/appointment_models.dart';
import 'package:garage_mobile/features/support_chat/data/support_chat_service.dart';
import 'package:garage_mobile/features/support_chat/presentation/support_chat_controller.dart';
import 'package:garage_mobile/features/support_chat/presentation/support_chat_page.dart';

SupportRoom room([String status = 'BOT']) => SupportRoom.fromJson({
  'id': 1,
  'branchId': 1,
  'branchName': 'AutoCare Central',
  'status': status,
  'agentName': status == 'HUMAN' ? 'Tiếp tân An' : null,
  'unreadCount': 2,
});
SupportMessage message(int id, {String type = 'BOT'}) =>
    SupportMessage.fromJson({
      'id': id,
      'senderType': type,
      'senderName': type == 'BOT' ? 'Trợ lý tự động' : 'Tiếp tân An',
      'content': 'Nội dung $id',
      'createdAt': '2026-10-02T15:00:00',
    });

class FakeGateway implements SupportGateway {
  bool available = true, failSend = false;
  String status = 'BOT';
  int readCount = 0;
  List<String> sendKeys = [];
  List<SupportMessage> data = [message(1)];
  Completer<List<SupportRoom>>? delayedRooms;
  SupportException? historyFailure;
  bool failBranches = false;
  @override
  Future<bool> enabled(String token) async => available;
  @override
  Future<List<BranchOption>> branches(String token) async {
    if (failBranches) throw const SupportException('Không thể kết nối chat');
    return [
      const BranchOption(
        id: 1,
        name: 'AutoCare Central',
        address: 'Chi nhánh 1',
      ),
    ];
  }

  @override
  Future<List<SupportRoom>> rooms(String token) async =>
      delayedRooms != null ? delayedRooms!.future : [room(status)];
  @override
  Future<SupportRoom> open(String token, int branch) async => room(status);
  @override
  Future<SupportHistory> history(String token, int room, {int? before}) async {
    if (historyFailure != null) throw historyFailure!;
    return SupportHistory(data, false);
  }

  @override
  Future<void> send(
    String token,
    int room,
    String text,
    String clientId,
  ) async {
    sendKeys.add(clientId);
    if (failSend) throw const SupportException('Mất mạng');
  }

  @override
  Future<void> handoff(String token, int room) async {
    status = 'WAITING';
  }

  @override
  Future<void> read(String token, int room, int lastReadId) async {
    readCount++;
  }

  @override
  Future<AppointmentBookingResult> book(
    String token,
    int room,
    String key,
    Map<String, dynamic> payload,
  ) => throw UnimplementedError();
}

class FakeEvents implements SupportEvents {
  final stream = StreamController<bool>.broadcast();
  @override
  Stream<bool> watch(String token) => stream.stream;
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  late FakeGateway api;
  late FakeEvents events;
  late SupportChatController chat;
  setUp(() {
    api = FakeGateway();
    events = FakeEvents();
    chat = SupportChatController(api, events);
  });
  tearDown(() async {
    chat.dispose();
    await events.stream.close();
  });
  Future<void> start(WidgetTester tester) async {
    chat.bind('session-a');
    await tester.pump();
    await tester.pump();
  }

  testWidgets('opening chat recovers a failed initialization after login', (
    tester,
  ) async {
    api.failBranches = true;
    await start(tester);
    expect(chat.error, isNotNull);
    expect(chat.branches, isEmpty);
    api.failBranches = false;
    chat.setVisible(true);
    await tester.pump();
    expect(chat.error, isNull);
    expect(chat.branches, hasLength(1));
    expect(chat.rooms, hasLength(1));
    chat.bind(null);
    await tester.pump();
  });

  testWidgets('successful refresh clears a prior connection error', (
    tester,
  ) async {
    await start(tester);
    chat.setVisible(true);
    chat.select(1);
    await tester.pump();
    api.historyFailure = const SupportException('Mất mạng');
    await chat.refresh();
    expect(chat.error, isNotNull);
    api.historyFailure = null;
    await chat.refresh();
    expect(chat.error, isNull);
    chat.bind(null);
    await tester.pump();
  });

  testWidgets(
    'retry keeps the same message key and new content gets a new key',
    (tester) async {
      await start(tester);
      chat.select(1);
      await tester.pump();
      api.failSend = true;
      expect(await chat.send('Xin chào'), false);
      api.failSend = false;
      expect(await chat.send('Xin chào'), true);
      expect(api.sendKeys[0], api.sendKeys[1]);
      await chat.send('Tin khác');
      expect(api.sendKeys[2], isNot(api.sendKeys[1]));
      chat.bind(null);
      await tester.pump();
    },
  );
  testWidgets('old response cannot restore data after logout', (tester) async {
    await start(tester);
    api.delayedRooms = Completer();
    final refresh = chat.refresh();
    chat.bind(null);
    api.delayedRooms!.complete([room()]);
    await refresh;
    expect(chat.rooms, isEmpty);
    expect(chat.messages, isEmpty);
    expect(chat.token, isNull);
    chat.bind(null);
    await tester.pump();
  });
  testWidgets('reconnect reloads missed messages and deduplicates history', (
    tester,
  ) async {
    await start(tester);
    chat.setVisible(true);
    chat.select(1);
    await tester.pump();
    api.data = [message(1), message(2, type: 'STAFF')];
    events.stream.add(true);
    await tester.pump();
    await tester.pump();
    events.stream.add(true);
    await tester.pump();
    await tester.pump();
    expect(chat.messages.map((m) => m.id), [1, 2]);
    expect(chat.connected, true);
    expect(api.readCount, greaterThan(0));
    chat.bind(null);
    await tester.pump();
  });
  testWidgets('background conversation is not marked read', (tester) async {
    await start(tester);
    chat.select(1);
    await tester.pump();
    await chat.refresh();
    expect(api.readCount, 0);
    expect(chat.unread, 2);
    chat.bind(null);
    await tester.pump();
  });
  testWidgets('handoff retains history and enters receptionist queue', (
    tester,
  ) async {
    await start(tester);
    chat.setVisible(true);
    chat.select(1);
    await tester.pump();
    await chat.handoff();
    expect(chat.selected?.status, 'WAITING');
    expect(chat.messages, isNotEmpty);
    chat.bind(null);
    await tester.pump();
  });
  testWidgets('lost access clears previously visible messages', (tester) async {
    await start(tester);
    chat.setVisible(true);
    chat.select(1);
    await tester.pump();
    api.historyFailure = const SupportException('Không có quyền', 403);
    await chat.refresh();
    expect(chat.messages, isEmpty);
    expect(chat.rooms, isEmpty);
    expect(chat.selectedId, isNull);
    chat.bind(null);
    await tester.pump();
  });
  testWidgets('disabled feature never loads chat rooms', (tester) async {
    api.available = false;
    await start(tester);
    expect(chat.enabled, false);
    expect(chat.rooms, isEmpty);
    expect(chat.connected, false);
    chat.bind(null);
    await tester.pump();
  });
  testWidgets(
    'customer sees sender labels, booking and handoff at narrow width',
    (tester) async {
      tester.view.physicalSize = const Size(360, 800);
      tester.view.devicePixelRatio = 1;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);
      await start(tester);
      chat.select(1);
      await tester.pumpWidget(
        ChangeNotifierProvider.value(
          value: chat,
          child: const MaterialApp(home: SupportChatPage()),
        ),
      );
      await tester.pumpAndSettle();
      expect(find.text('Trợ lý tự động'), findsOneWidget);
      expect(find.text('Đặt lịch'), findsOneWidget);
      expect(find.text('Gặp tiếp tân'), findsOneWidget);
      expect(tester.takeException(), isNull);
      await tester.tap(find.text('Gặp tiếp tân'));
      await tester.pumpAndSettle();
      expect(find.text('Đang chờ tiếp tân nhận hỗ trợ'), findsOneWidget);
      expect(find.text('Gặp tiếp tân'), findsNothing);
      await tester.pumpWidget(const SizedBox.shrink());
      chat.bind(null);
      await tester.pump();
    },
  );
}
