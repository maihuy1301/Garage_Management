import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/features/appointments/data/appointment_service.dart';
import 'package:garage_mobile/features/appointments/domain/appointment_models.dart';
import 'package:garage_mobile/features/appointments/presentation/appointment_booking_page.dart';
import 'package:garage_mobile/features/vehicles/data/vehicle_service.dart';
import 'package:garage_mobile/features/vehicles/domain/vehicle_models.dart';

void main() {
  testWidgets('hiển thị form với xe và chi nhánh tải từ service', (
    tester,
  ) async {
    final gateway = _FakeAppointmentGateway();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: AppointmentBookingPage(gateway: gateway)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Lịch hẹn dịch vụ'), findsOneWidget);
    expect(find.textContaining('51A-11111'), findsOneWidget);
    expect(find.text('Garage Trung tâm'), findsOneWidget);
    expect(
      find.byKey(const ValueKey('appointment-note-field')),
      findsOneWidget,
    );
  });

  testWidgets('validation chặn gửi khi chưa chọn thời gian', (tester) async {
    final gateway = _FakeAppointmentGateway();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: AppointmentBookingPage(gateway: gateway)),
      ),
    );
    await tester.pumpAndSettle();

    final submit = find.byKey(const ValueKey('appointment-submit-button'));
    await tester.scrollUntilVisible(
      submit,
      300,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.pumpAndSettle();
    await tester.tap(submit);
    await tester.pump();

    expect(find.text('Vui lòng chọn ngày.'), findsOneWidget);
    expect(find.text('Chọn giờ.'), findsOneWidget);
    expect(gateway.createCalls, 0);
  });

  testWidgets('hiển thị empty state khi customer chưa có xe', (tester) async {
    final gateway = _FakeAppointmentGateway(hasVehicle: false);
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: AppointmentBookingPage(gateway: gateway)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Bạn chưa có xe'), findsOneWidget);
    expect(
      find.byKey(const ValueKey('appointment-empty-add-vehicle-button')),
      findsOneWidget,
    );
    expect(
      find.byKey(const ValueKey('appointment-header-add-vehicle-button')),
      findsOneWidget,
    );
    expect(find.text('Thêm xe để đặt lịch'), findsOneWidget);
    expect(
      find.byKey(const ValueKey('appointment-submit-button')),
      findsNothing,
    );
  });

  testWidgets('thêm xe từ empty state rồi hiện form đặt lịch', (tester) async {
    final appointmentGateway = _FakeAppointmentGateway(hasVehicle: false);
    final vehicleGateway = _FakeVehicleGateway(appointmentGateway);
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: AppointmentBookingPage(
            gateway: appointmentGateway,
            vehicleGateway: vehicleGateway,
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(
      find.byKey(const ValueKey('appointment-empty-add-vehicle-button')),
    );
    await tester.pumpAndSettle();
    await tester.enterText(
      find.byKey(const ValueKey('vehicle-license-plate-field')),
      '51A-12345',
    );
    final brandField = find.byKey(const ValueKey('vehicle-brand-field'));
    await tester.ensureVisible(brandField);
    await tester.pumpAndSettle();
    await tester.tap(brandField);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Toyota').last);
    await tester.pumpAndSettle();
    final modelField = find.byKey(const ValueKey('vehicle-model-field'));
    await tester.ensureVisible(modelField);
    await tester.pumpAndSettle();
    await tester.tap(modelField);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Camry').last);
    await tester.pumpAndSettle();
    final vehicleSubmit = find.byKey(const ValueKey('vehicle-submit-button'));
    await tester.ensureVisible(vehicleSubmit);
    await tester.pumpAndSettle();
    await tester.tap(vehicleSubmit);
    await tester.pumpAndSettle();

    expect(vehicleGateway.createCalls, 1);
    expect(
      find.byKey(const ValueKey('appointment-vehicle-field')),
      findsOneWidget,
    );
    expect(
      find.byKey(const ValueKey('appointment-submit-button')),
      findsOneWidget,
    );
    expect(find.textContaining('51A-11111'), findsOneWidget);
  });

  testWidgets('gửi lịch và hiển thị xác nhận thành công', (tester) async {
    final gateway = _FakeAppointmentGateway();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: AppointmentBookingPage(gateway: gateway)),
      ),
    );
    await tester.pumpAndSettle();

    final dateField = find.byKey(const ValueKey('appointment-date-field'));
    await tester.ensureVisible(dateField);
    await tester.pumpAndSettle();
    await tester.tap(dateField);
    await tester.pumpAndSettle();
    await tester.tap(find.text('OK'));
    await tester.pumpAndSettle();

    final timeField = find.byKey(const ValueKey('appointment-time-field'));
    await tester.tap(timeField);
    await tester.pumpAndSettle();
    await tester.tap(find.text('OK'));
    await tester.pumpAndSettle();

    final submit = find.byKey(const ValueKey('appointment-submit-button'));
    await tester.scrollUntilVisible(
      submit,
      300,
      scrollable: find.byType(Scrollable).first,
    );
    await tester.tap(submit);
    await tester.pumpAndSettle();

    expect(gateway.createCalls, 1);
    expect(find.byKey(const ValueKey('appointment-success')), findsOneWidget);
    expect(find.textContaining('Đã tạo lịch #1'), findsOneWidget);
  });

  testWidgets('hiển thị lịch của tôi và chỉ cho hủy trạng thái hợp lệ', (
    tester,
  ) async {
    final gateway = _FakeAppointmentGateway();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: AppointmentBookingPage(gateway: gateway)),
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(find.text('Lịch của tôi'));
    await tester.pumpAndSettle();

    expect(find.text('Chờ xác nhận'), findsOneWidget);
    expect(find.text('Đã tiếp nhận'), findsOneWidget);
    expect(find.byKey(const ValueKey('cancel-appointment-10')), findsOneWidget);
    expect(find.byKey(const ValueKey('cancel-appointment-11')), findsNothing);
  });

  testWidgets('xác nhận hủy và cập nhật trạng thái theo phản hồi backend', (
    tester,
  ) async {
    final gateway = _FakeAppointmentGateway();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: AppointmentBookingPage(gateway: gateway)),
      ),
    );
    await tester.pumpAndSettle();
    await tester.tap(find.text('Lịch của tôi'));
    await tester.pumpAndSettle();

    await tester.tap(find.byKey(const ValueKey('cancel-appointment-10')));
    await tester.pumpAndSettle();
    await tester.tap(find.byKey(const ValueKey('confirm-cancel-appointment')));
    await tester.pumpAndSettle();

    expect(gateway.cancelledIds, [10]);
    expect(find.text('Đã hủy'), findsOneWidget);
    expect(find.byKey(const ValueKey('cancel-appointment-10')), findsNothing);
  });
}

class _FakeAppointmentGateway implements AppointmentGateway {
  _FakeAppointmentGateway({this.hasVehicle = true});

  bool hasVehicle;
  int createCalls = 0;
  final List<int> cancelledIds = [];

  @override
  Future<AppointmentBookingResult> cancelAppointment(int appointmentId) async {
    cancelledIds.add(appointmentId);
    return AppointmentBookingResult(
      id: appointmentId,
      status: 'HUY',
      appointmentTime: DateTime(2026, 9, 10, 8, 30),
      vehicleLicensePlate: '51A-11111',
      branchName: 'Garage Trung tâm',
    );
  }

  @override
  Future<AppointmentBookingResult> createAppointment({
    required int vehicleId,
    required int branchId,
    required DateTime appointmentTime,
    String? note,
  }) async {
    createCalls += 1;
    return AppointmentBookingResult(
      id: 1,
      status: 'CHO_XAC_NHAN',
      appointmentTime: appointmentTime,
      vehicleLicensePlate: '51A-11111',
      branchName: 'Garage Trung tâm',
    );
  }

  @override
  Future<AppointmentBookingOptions> loadBookingOptions() async {
    return AppointmentBookingOptions(
      vehicles: hasVehicle
          ? const [
              VehicleOption(
                id: 7,
                licensePlate: '51A-11111',
                brand: 'Toyota',
                model: 'Camry',
              ),
            ]
          : const [],
      branches: const [
        BranchOption(
          id: 2,
          name: 'Garage Trung tâm',
          address: 'Quận 1, TP.HCM',
        ),
      ],
    );
  }

  @override
  Future<List<AppointmentBookingResult>> loadMyAppointments() async {
    return [
      AppointmentBookingResult(
        id: 10,
        status: 'CHO_XAC_NHAN',
        appointmentTime: DateTime(2026, 9, 10, 8, 30),
        vehicleLicensePlate: '51A-11111',
        branchName: 'Garage Trung tâm',
        note: 'Bảo dưỡng định kỳ',
      ),
      AppointmentBookingResult(
        id: 11,
        status: 'DA_TIEP_NHAN',
        appointmentTime: DateTime(2026, 9, 8, 9),
        vehicleLicensePlate: '51A-11111',
        branchName: 'Garage Trung tâm',
      ),
    ];
  }

  @override
  Future<AppointmentBookingResult> loadAppointment(int appointmentId) async {
    return (await loadMyAppointments()).firstWhere(
      (appointment) => appointment.id == appointmentId,
    );
  }
}

class _FakeVehicleGateway implements VehicleGateway {
  _FakeVehicleGateway(this.appointmentGateway);

  final _FakeAppointmentGateway appointmentGateway;
  int createCalls = 0;

  @override
  Future<CustomerVehicle> createVehicle(CreateCustomerVehicle request) async {
    createCalls += 1;
    appointmentGateway.hasVehicle = true;
    return CustomerVehicle(id: 7, licensePlate: request.licensePlate.trim());
  }

  @override
  Future<List<VehicleBrand>> loadBrands() async => const [
    VehicleBrand(id: 1, name: 'Toyota'),
  ];

  @override
  Future<List<VehicleModel>> loadModels(int brandId) async => const [
    VehicleModel(id: 10, brandId: 1, name: 'Camry'),
  ];

  @override
  Future<List<CustomerVehicle>> loadVehicles() async => const [];
}
