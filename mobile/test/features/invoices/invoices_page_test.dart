import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/features/invoices/data/invoice_service.dart';
import 'package:garage_mobile/features/invoices/domain/invoice_models.dart';
import 'package:garage_mobile/features/invoices/presentation/invoices_page.dart';

import 'invoice_fixture.dart';

class FakeInvoices implements InvoiceGateway {
  List<CustomerInvoice> invoices = [CustomerInvoice.fromJson(invoiceJson())];
  String? error;
  int calls = 0;
  Completer<List<CustomerInvoice>>? pending;
  @override
  Future<List<CustomerInvoice>> loadInvoices() async {
    calls++;
    if (error != null) throw InvoiceException(error!);
    return pending == null ? invoices : pending!.future;
  }

  @override
  Future<CustomerInvoice> loadInvoice(int id) async =>
      (await loadInvoices()).first;
}

void main() {
  Future<void> show(
    WidgetTester tester,
    FakeInvoices gateway, {
    int? id,
  }) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: InvoicesPage(invoiceId: id, gateway: gateway),
        ),
      ),
    );
    await tester.pump();
  }

  testWidgets('loading, backend totals/status and pull to refresh', (
    tester,
  ) async {
    final gateway = FakeInvoices()..pending = Completer();
    await show(tester, gateway);
    expect(find.byType(CircularProgressIndicator), findsOneWidget);
    gateway.pending!.complete(gateway.invoices);
    await tester.pumpAndSettle();
    expect(find.text('Chưa thanh toán'), findsOneWidget);
    expect(find.text('319.999,50 đ'), findsOneWidget);
    gateway.pending = null;
    gateway.invoices = [];
    await tester.drag(find.byType(ListView), const Offset(0, 400));
    await tester.pumpAndSettle();
    expect(gateway.calls, 2);
    expect(find.text('Chưa có hóa đơn'), findsOneWidget);
    await tester.drag(find.byType(ListView), const Offset(0, 400));
    await tester.pumpAndSettle();
    expect(gateway.calls, 3);
  });

  testWidgets(
    'error can retry and refresh failure removes old financial data',
    (tester) async {
      final gateway = FakeInvoices()
        ..error = 'Bạn không có quyền xem hóa đơn này.';
      await show(tester, gateway);
      await tester.pumpAndSettle();
      expect(find.text(gateway.error!), findsOneWidget);
      gateway.error = null;
      await tester.tap(find.text('Thử lại'));
      await tester.pumpAndSettle();
      expect(find.text('419.999,50 đ'), findsOneWidget);
      gateway.error = 'Phiên đăng nhập đã hết hạn.';
      await tester.drag(find.byType(ListView), const Offset(0, 400));
      await tester.pumpAndSettle();
      expect(find.text('419.999,50 đ'), findsNothing);
      expect(find.text(gateway.error!), findsOneWidget);
    },
  );

  testWidgets(
    'detail displays services, parts, totals and every payment including failed',
    (tester) async {
      final gateway = FakeInvoices();
      await show(tester, gateway, id: 9);
      await tester.pumpAndSettle();
      expect(find.text('Thay dầu'), findsOneWidget);
      await tester.scrollUntilVisible(find.text('Dầu động cơ'), 200);
      expect(find.text('SL: 4 lít × 50.000 đ'), findsOneWidget);
      await tester.scrollUntilVisible(find.text('Tổng cộng'), 200);
      expect(find.text('10.000,50 đ'), findsOneWidget);
      await tester.scrollUntilVisible(find.text('Lịch sử thanh toán'), 200);
      await tester.scrollUntilVisible(find.text('Mã giao dịch: GD-13'), 200);
      expect(find.text('Tiền mặt'), findsOneWidget);
      expect(find.text('Thành công'), findsOneWidget);
      expect(find.text('Thất bại'), findsOneWidget);
      expect(find.textContaining('Online'), findsNothing);
      expect(tester.takeException(), isNull);
    },
  );

  testWidgets('empty detail sections and unknown status on narrow display', (
    tester,
  ) async {
    tester.view.physicalSize = const Size(320, 720);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    final gateway = FakeInvoices()
      ..invoices = [
        CustomerInvoice.fromJson(
          invoiceJson()
            ..['services'] = []
            ..['parts'] = []
            ..['payments'] = []
            ..['trangThai'] = 'TRANG_THAI_MOI',
        ),
      ];
    await show(tester, gateway, id: 9);
    await tester.pumpAndSettle();
    expect(find.text('TRANG_THAI_MOI'), findsOneWidget);
    expect(find.text('Không có dịch vụ.'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.text('Chưa có giao dịch thanh toán.'),
      200,
    );
    expect(find.text('Chưa có giao dịch thanh toán.'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('detail error, retry, pull refresh and invalid ID', (
    tester,
  ) async {
    final gateway = FakeInvoices()..error = 'Không tìm thấy hóa đơn.';
    await show(tester, gateway, id: 9);
    await tester.pumpAndSettle();
    expect(find.text('Không tìm thấy hóa đơn.'), findsOneWidget);
    gateway.error = null;
    await tester.tap(find.text('Thử lại'));
    await tester.pumpAndSettle();
    await tester.drag(find.byType(ListView), const Offset(0, 400));
    await tester.pumpAndSettle();
    expect(gateway.calls, 3);
    await show(tester, gateway, id: -1);
    await tester.pumpAndSettle();
    expect(find.text('Mã hóa đơn không hợp lệ.'), findsOneWidget);
    expect(gateway.calls, 3);
  });

  testWidgets(
    'late response cannot overwrite a newer request or update disposed page',
    (tester) async {
      final gateway = FakeInvoices()..pending = Completer();
      await show(tester, gateway);
      final old = gateway.pending!;
      gateway.pending = null;
      gateway.invoices = [];
      final refresh = tester
          .widget<RefreshIndicator>(find.byType(RefreshIndicator))
          .onRefresh();
      await tester.pumpAndSettle();
      await refresh;
      old.complete([CustomerInvoice.fromJson(invoiceJson())]);
      await tester.pumpAndSettle();
      expect(find.text('Chưa có hóa đơn'), findsOneWidget);
      gateway.pending = Completer();
      final next = tester
          .widget<RefreshIndicator>(find.byType(RefreshIndicator))
          .onRefresh();
      await tester.pump(const Duration(seconds: 1));
      await tester.pumpWidget(const MaterialApp(home: SizedBox()));
      gateway.pending!.complete([]);
      await tester.pumpAndSettle();
      await next;
      expect(tester.takeException(), isNull);
    },
  );
}
