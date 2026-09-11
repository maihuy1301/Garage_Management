import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/features/appointments/data/appointment_service.dart';
import 'package:garage_mobile/features/appointments/domain/appointment_models.dart';
import 'package:garage_mobile/features/appointments/presentation/appointment_detail_page.dart';
import 'package:garage_mobile/features/appointments/presentation/appointment_tracking_page.dart';

void main() {
  testWidgets('tracking hiển thị trạng thái nhận từ backend', (tester) async {
    final gateway = _FakeTrackingGateway();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: AppointmentTrackingPage(gateway: gateway)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Theo dõi lịch hẹn'), findsOneWidget);
    expect(find.text('51A-11111'), findsOneWidget);
    expect(find.text('Đã tiếp nhận'), findsOneWidget);
    expect(find.byKey(const ValueKey('tracking-card-16')), findsOneWidget);
  });

  testWidgets('chi tiết hiển thị timeline tại bước xe đã tiếp nhận', (
    tester,
  ) async {
    final gateway = _FakeTrackingGateway();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: AppointmentDetailPage(appointmentId: 16, gateway: gateway),
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Chi tiết lịch hẹn'), findsOneWidget);
    expect(find.text('Xe đã tiếp nhận'), findsOneWidget);
    expect(find.text('HIỆN TẠI'), findsOneWidget);
    await tester.scrollUntilVisible(
      find.text('Mã lịch #16'),
      300,
      scrollable: find.byType(Scrollable).first,
    );
    expect(find.text('Mã lịch #16'), findsOneWidget);
    expect(gateway.loadedIds, [16]);
  });

  testWidgets('tracking có empty state và hành động đặt lịch', (tester) async {
    final gateway = _FakeTrackingGateway(appointments: const []);
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: AppointmentTrackingPage(gateway: gateway)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.byKey(const ValueKey('tracking-empty')), findsOneWidget);
    expect(find.text('Chưa có lịch để theo dõi'), findsOneWidget);
    expect(find.text('Đặt lịch ngay'), findsOneWidget);
  });
}

class _FakeTrackingGateway implements AppointmentGateway {
  _FakeTrackingGateway({List<AppointmentBookingResult>? appointments})
    : appointments =
          appointments ??
          [
            AppointmentBookingResult(
              id: 16,
              status: 'DA_TIEP_NHAN',
              appointmentTime: DateTime(2026, 9, 9, 9),
              vehicleLicensePlate: '51A-11111',
              vehicleBrand: 'Toyota',
              vehicleModel: 'Camry',
              branchName: 'Garage Trung tâm',
              note: 'Kiểm tra phanh',
            ),
          ];

  final List<AppointmentBookingResult> appointments;
  final List<int> loadedIds = [];

  @override
  Future<List<AppointmentBookingResult>> loadMyAppointments() async =>
      appointments;

  @override
  Future<AppointmentBookingResult> loadAppointment(int appointmentId) async {
    loadedIds.add(appointmentId);
    return appointments.firstWhere(
      (appointment) => appointment.id == appointmentId,
    );
  }

  @override
  Future<AppointmentBookingResult> cancelAppointment(int appointmentId) =>
      throw UnimplementedError();

  @override
  Future<AppointmentBookingResult> createAppointment({
    required int vehicleId,
    required int branchId,
    required DateTime appointmentTime,
    String? note,
  }) => throw UnimplementedError();

  @override
  Future<AppointmentBookingOptions> loadBookingOptions() =>
      throw UnimplementedError();
}
