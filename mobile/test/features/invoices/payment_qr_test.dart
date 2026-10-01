import 'dart:async';
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/features/invoices/data/payment_qr_actions.dart';
import 'package:garage_mobile/features/invoices/data/payment_service.dart';
import 'package:garage_mobile/features/invoices/domain/payment_models.dart';
import 'package:garage_mobile/features/invoices/presentation/invoice_payment_page.dart';
import 'payment_test.dart' as fixture;

PaymentSession qrSession({String status = 'PENDING', bool expired = false}) =>
    PaymentSession.fromJson({
      ...fixture.session(status: status),
      'qrImageUrl': 'https://vietqr.app/img?test=1',
      if (expired)
        'expiresAt': DateTime.now()
            .subtract(const Duration(minutes: 1))
            .toIso8601String(),
    });

class QrGateway extends fixture.FakePayments {
  bool expired = false;
  @override
  Future<PaymentSession> create(int id) async => qrSession();
  @override
  Future<PaymentSession> status(int id, String sessionId) async {
    checks++;
    if (errorStatus != null) {
      throw PaymentException(
        'Không còn quyền truy cập.',
        statusCode: errorStatus,
      );
    }
    return qrSession(status: nextStatus, expired: expired);
  }
}

class FakeQrActions implements PaymentQrActions {
  final events = <String>[];
  bool openResult = true;
  bool failSave = false;
  Completer<void>? pendingSave;
  @override
  bool get supported => true;
  @override
  Future<void> save(
    PaymentSession session, {
    required bool Function() isCurrent,
  }) async {
    expect(isCurrent(), isTrue);
    events.add('save');
    if (failSave) throw const PaymentException('Chưa lưu được ảnh QR.');
    if (pendingSave != null) await pendingSave!.future;
  }

  @override
  Future<bool> openMbBank() async {
    events.add('open');
    return openResult;
  }
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  Future<void> show(
    WidgetTester tester,
    QrGateway gateway,
    FakeQrActions actions,
  ) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: InvoicePaymentPage(
            invoiceId: 1,
            gateway: gateway,
            qrActions: actions,
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('Tiếp tục thanh toán'));
    await tester.tap(find.text('Tiếp tục thanh toán'));
    await tester.pumpAndSettle();
  }

  Future<void> tap(WidgetTester tester, String label) async {
    await tester.scrollUntilVisible(
      find.text(label),
      200,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.ensureVisible(find.text(label));
    await tester.pumpAndSettle();
    await tester.tap(find.text(label));
    await tester.pumpAndSettle();
  }

  testWidgets('saves before opening MB and never marks invoice paid', (
    tester,
  ) async {
    final gateway = QrGateway();
    final actions = FakeQrActions();
    await show(tester, gateway, actions);
    await tap(tester, 'Lưu QR và mở MB Bank');
    expect(actions.events, ['save', 'open']);
    expect(gateway.checks, 1);
    expect(find.text('Thanh toán thành công'), findsNothing);
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('save-only never opens a bank', (tester) async {
    final actions = FakeQrActions();
    await show(tester, QrGateway(), actions);
    await tap(tester, 'Lưu mã QR');
    expect(actions.events, ['save']);
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('save failure prevents bank launch', (tester) async {
    final actions = FakeQrActions()..failSave = true;
    await show(tester, QrGateway(), actions);
    await tap(tester, 'Lưu QR và mở MB Bank');
    expect(actions.events, ['save']);
    expect(find.text('Chưa lưu được ảnh QR.'), findsOneWidget);
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('missing MB keeps saved QR and explains fallback', (
    tester,
  ) async {
    final actions = FakeQrActions()..openResult = false;
    await show(tester, QrGateway(), actions);
    await tap(tester, 'Lưu QR và mở MB Bank');
    expect(
      find.descendant(
        of: find.byType(SnackBar),
        matching: find.textContaining('Đã lưu QR nhưng chưa mở được MB Bank'),
      ),
      findsOneWidget,
    );
    await tester.pumpWidget(const SizedBox());
  });
  for (final state in ['expired', 'paid', 'forbidden']) {
    testWidgets('$state discovered before export prevents saving and opening', (
      tester,
    ) async {
      final gateway = QrGateway();
      final actions = FakeQrActions();
      await show(tester, gateway, actions);
      if (state == 'expired') gateway.expired = true;
      if (state == 'paid') gateway.nextStatus = 'SUCCEEDED';
      if (state == 'forbidden') gateway.errorStatus = 403;
      await tap(tester, 'Lưu QR và mở MB Bank');
      expect(actions.events, isEmpty);
      await tester.pumpWidget(const SizedBox());
    });
  }
  testWidgets('leaving screen during save prevents late launch', (
    tester,
  ) async {
    final actions = FakeQrActions()..pendingSave = Completer<void>();
    await show(tester, QrGateway(), actions);
    await tester.scrollUntilVisible(
      find.text('Lưu QR và mở MB Bank'),
      200,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.tap(find.text('Lưu QR và mở MB Bank'));
    await tester.pump();
    await tester.pumpWidget(const SizedBox());
    actions.pendingSave!.complete();
    await tester.pumpAndSettle();
    expect(actions.events, ['save']);
  });

  group('Android QR service', () {
    final calls = <MethodCall>[];
    setUp(() {
      debugDefaultTargetPlatformOverride = TargetPlatform.android;
      calls.clear();
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(AndroidPaymentQrActions.channel, (
            call,
          ) async {
            calls.add(call);
            if (call.method == 'needsStoragePermission') return false;
            if (call.method == 'openMbBank') return false;
            return null;
          });
    });
    tearDown(() {
      debugDefaultTargetPlatformOverride = null;
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(AndroidPaymentQrActions.channel, null);
    });
    test('download has no JWT and sends bytes to gallery bridge', () async {
      final dio = Dio();
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (request, handler) {
            expect(request.headers['Authorization'], isNull);
            handler.resolve(Response(requestOptions: request, data: [1, 2, 3]));
          },
        ),
      );
      await AndroidPaymentQrActions(
        downloadClient: dio,
      ).save(qrSession(), isCurrent: () => true);
      expect(calls.map((e) => e.method), ['needsStoragePermission', 'saveQr']);
      expect((calls.last.arguments as Map)['bytes'], isA<Uint8List>());
    });
    test('download failure never saves an image', () async {
      final dio = Dio();
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (request, handler) {
            handler.reject(DioException(requestOptions: request));
          },
        ),
      );
      await expectLater(
        AndroidPaymentQrActions(
          downloadClient: dio,
        ).save(qrSession(), isCurrent: () => true),
        throwsA(isA<PaymentException>()),
      );
      expect(calls.map((e) => e.method), ['needsStoragePermission']);
    });
    test('stale screen after download prevents gallery write', () async {
      var current = true;
      final dio = Dio();
      dio.interceptors.add(
        InterceptorsWrapper(
          onRequest: (request, handler) {
            current = false;
            handler.resolve(Response(requestOptions: request, data: [1, 2, 3]));
          },
        ),
      );
      await expectLater(
        AndroidPaymentQrActions(
          downloadClient: dio,
        ).save(qrSession(), isCurrent: () => current),
        throwsA(isA<PaymentException>()),
      );
      expect(calls.map((e) => e.method), ['needsStoragePermission']);
    });
  });
}
