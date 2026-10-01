import 'dart:async';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/theme/app_colors.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/invoices/data/payment_service.dart';
import 'package:garage_mobile/features/invoices/domain/payment_models.dart';
import 'package:garage_mobile/features/invoices/presentation/invoice_payment_page.dart';

Map<String, dynamic> receiver() => {
  'bankCode': 'TEST',
  'bankName': 'TestBank',
  'accountNumber': '000000',
  'accountName': 'TEST ONLY',
};
Map<String, dynamic> options({bool enabled = true}) => {
  'available': enabled,
  'message': enabled
      ? 'Chọn tài khoản nhận của garage'
      : 'Garage chưa cấu hình thanh toán ngân hàng.',
  'environment': 'test',
  'remainingAmount': 1190000,
  'receivers': enabled ? [receiver()] : [],
};
Map<String, dynamic> session({String status = 'PENDING'}) => {
  'sessionId': 'abc',
  'invoiceId': 1,
  'status': status,
  'environment': 'test',
  'amount': 1190000,
  'transferContent': 'GARABC',
  'expiresAt': DateTime.now()
      .toUtc()
      .add(const Duration(minutes: 15))
      .toIso8601String(),
  'receiver': receiver(),
  'qrImageUrl': null,
  'invoiceStatus': status == 'SUCCEEDED' ? 'DA_THANH_TOAN' : 'CHUA_THANH_TOAN',
  'remainingAmount': status == 'SUCCEEDED' ? 0 : 1190000,
};

class FakePayments implements PaymentGateway {
  bool enabled = true;
  String nextStatus = 'PENDING';
  String? error;
  int? errorStatus;
  int creates = 0, checks = 0;
  Completer<PaymentSession>? pending;
  Completer<PaymentOptions>? pendingOptions;
  @override
  Future<PaymentOptions> options(int id) async =>
      pendingOptions?.future ?? PaymentOptions.fromJson(paymentOptions());
  Map<String, dynamic> paymentOptions() => optionsJson(enabled);
  @override
  Future<PaymentSession> create(int id) async {
    creates++;
    return PaymentSession.fromJson(session());
  }

  @override
  Future<PaymentSession> status(int id, String sessionId) async {
    checks++;
    if (error != null) throw PaymentException(error!, statusCode: errorStatus);
    return pending?.future ??
        PaymentSession.fromJson(session(status: nextStatus));
  }
}

Map<String, dynamic> optionsJson(bool enabled) => options(enabled: enabled);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  test('all requests use JWT and POST sends no money/owner/branch', () async {
    FlutterSecureStorage.setMockInitialValues({
      'garage_access_token': 'test-token',
    });
    final dio = Dio(BaseOptions(baseUrl: 'http://localhost/api'));
    final service = PaymentService(
      ApiClient(dio, const SecureSessionStorage(FlutterSecureStorage())),
    );
    final requests = <String>[];
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (request, handler) {
          expect(request.headers['Authorization'], 'Bearer test-token');
          expect(request.data, isNull);
          expect(request.queryParameters, isEmpty);
          requests.add('${request.method} ${request.path}');
          handler.resolve(
            Response(
              requestOptions: request,
              data: {
                'success': true,
                'data': request.path.endsWith('options')
                    ? options()
                    : session(),
              },
            ),
          );
        },
      ),
    );
    expect((await service.options(1)).available, isTrue);
    await service.create(1);
    await service.status(1, 'abc');
    expect(requests, [
      'GET /invoices/1/payment-options',
      'POST /invoices/1/payment-sessions',
      'GET /invoices/1/payment-sessions/abc',
    ]);
  });

  Future<void> show(
    WidgetTester tester,
    FakePayments gateway, {
    int id = 1,
  }) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: InvoicePaymentPage(invoiceId: id, gateway: gateway),
        ),
      ),
    );
    await tester.pumpAndSettle();
  }

  Future<void> begin(WidgetTester tester) async {
    await tester.scrollUntilVisible(
      find.text('Tiếp tục thanh toán'),
      180,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('Tiếp tục thanh toán'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Tiếp tục thanh toán'));
    await tester.pumpAndSettle();
  }

  testWidgets('disabled configuration does not offer a fake bank or transfer', (
    tester,
  ) async {
    await show(tester, FakePayments()..enabled = false);
    expect(
      find.text('Garage chưa cấu hình thanh toán ngân hàng.'),
      findsOneWidget,
    );
    expect(find.text('Tiếp tục thanh toán'), findsNothing);
    expect(find.text('TestBank'), findsNothing);
  });
  testWidgets('does not display a zero balance before options arrive', (
    tester,
  ) async {
    final gateway = FakePayments()
      ..pendingOptions = Completer<PaymentOptions>();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: InvoicePaymentPage(invoiceId: 1, gateway: gateway),
        ),
      ),
    );
    await tester.pump();
    expect(find.text('0 đ'), findsNothing);
    expect(find.text('Chưa có thông tin số tiền'), findsOneWidget);
    gateway.pendingOptions!.complete(PaymentOptions.fromJson(options()));
    await tester.pumpAndSettle();
    expect(find.text('1.190.000 đ'), findsOneWidget);
  });
  testWidgets('forbidden status removes payment data and stops polling', (
    tester,
  ) async {
    final gateway = FakePayments();
    await show(tester, gateway);
    await begin(tester);
    gateway.error = 'Bạn không có quyền thanh toán hóa đơn này.';
    gateway.errorStatus = 403;
    await tester.pump(const Duration(seconds: 5));
    await tester.pumpAndSettle();
    expect(find.text(gateway.error!), findsOneWidget);
    expect(find.text('1.190.000 đ'), findsNothing);
    expect(find.text('Đang chờ ngân hàng xác nhận'), findsNothing);
    await tester.pump(const Duration(seconds: 10));
    expect(gateway.checks, 1);
  });
  testWidgets('creates only on explicit tap, polls, then stops at success', (
    tester,
  ) async {
    final gateway = FakePayments();
    await show(tester, gateway);
    expect(gateway.creates, 0);
    await begin(tester);
    expect(gateway.creates, 1);
    gateway.nextStatus = 'SUCCEEDED';
    await tester.pump(const Duration(seconds: 5));
    await tester.pumpAndSettle();
    expect(find.text('Thanh toán thành công'), findsOneWidget);
    expect(
      find.textContaining('Mã hóa đơn của bạn là', findRichText: true),
      findsOneWidget,
    );
    expect(
      tester.widget<CircleAvatar>(find.byType(CircleAvatar)).backgroundColor,
      AppColors.primary,
    );
    expect(find.text('Kiểm tra thanh toán'), findsNothing);
    final count = gateway.checks;
    await tester.pump(const Duration(seconds: 10));
    expect(gateway.checks, count);
  });
  testWidgets('network error keeps pending, never pretends payment succeeded', (
    tester,
  ) async {
    final gateway = FakePayments();
    await show(tester, gateway);
    await begin(tester);
    gateway.error = 'Chưa kiểm tra được thanh toán';
    await tester.pump(const Duration(seconds: 5));
    await tester.pumpAndSettle();
    expect(find.text(gateway.error!), findsOneWidget);
    expect(find.text('Thanh toán thành công'), findsNothing);
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('stops in background, checks immediately on resume', (
    tester,
  ) async {
    final gateway = FakePayments();
    await show(tester, gateway);
    await begin(tester);
    tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.paused);
    await tester.pump(const Duration(seconds: 12));
    expect(gateway.checks, 0);
    tester.binding.handleAppLifecycleStateChanged(AppLifecycleState.resumed);
    await tester.pumpAndSettle();
    expect(gateway.checks, 1);
    await tester.pumpWidget(const SizedBox());
  });
  testWidgets('in flight response from previous invoice is discarded', (
    tester,
  ) async {
    final gateway = FakePayments();
    await show(tester, gateway);
    await begin(tester);
    gateway.pending = Completer();
    await tester.pump(const Duration(seconds: 5));
    await show(tester, gateway, id: 2);
    gateway.pending!.complete(
      PaymentSession.fromJson(session(status: 'SUCCEEDED')),
    );
    await tester.pumpAndSettle();
    expect(find.text('Hóa đơn #2'), findsOneWidget);
    expect(find.text('Thanh toán thành công'), findsNothing);
    expect(find.text('Tiếp tục thanh toán'), findsOneWidget);
  });
  testWidgets(
    'small screen and large text remain scrollable without overflow',
    (tester) async {
      tester.view.physicalSize = const Size(320, 640);
      tester.view.devicePixelRatio = 1;
      addTearDown(tester.view.resetPhysicalSize);
      addTearDown(tester.view.resetDevicePixelRatio);
      await tester.pumpWidget(
        MaterialApp(
          builder: (context, child) => MediaQuery(
            data: MediaQuery.of(
              context,
            ).copyWith(textScaler: const TextScaler.linear(2)),
            child: child!,
          ),
          home: Scaffold(
            body: InvoicePaymentPage(invoiceId: 1, gateway: FakePayments()),
          ),
        ),
      );
      await tester.pumpAndSettle();
      await begin(tester);
      expect(find.text('Đang chờ ngân hàng xác nhận'), findsOneWidget);
      expect(tester.takeException(), isNull);
      await tester.pumpWidget(const SizedBox());
    },
  );
  testWidgets('review stops polling and prevents new transfer', (tester) async {
    final gateway = FakePayments()..nextStatus = 'REQUIRES_REVIEW';
    await show(tester, gateway);
    await begin(tester);
    await tester.pump(const Duration(seconds: 5));
    await tester.pumpAndSettle();
    expect(find.text('Giao dịch cần garage đối soát'), findsOneWidget);
    expect(find.text('Tạo phiên mới nếu chưa chuyển tiền'), findsNothing);
    await tester.pump(const Duration(seconds: 10));
    expect(gateway.checks, 1);
  });
}
