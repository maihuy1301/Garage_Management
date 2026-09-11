import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/features/vehicles/data/vehicle_service.dart';
import 'package:garage_mobile/features/vehicles/domain/vehicle_models.dart';
import 'package:garage_mobile/features/vehicles/presentation/vehicles_page.dart';
import 'package:garage_mobile/features/customer/presentation/customer_shell.dart';

void main() {
  testWidgets('empty state và nút góc phải cùng mở form thêm xe', (
    tester,
  ) async {
    final gateway = _FakeVehicleGateway();
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: VehiclesPage(gateway: gateway)),
      ),
    );
    await tester.pumpAndSettle();

    expect(
      find.byKey(const ValueKey('vehicles-header-add-button')),
      findsOneWidget,
    );
    expect(
      find.byKey(const ValueKey('vehicles-empty-add-button')),
      findsOneWidget,
    );

    await tester.tap(find.byKey(const ValueKey('vehicles-empty-add-button')));
    await tester.pumpAndSettle();
    expect(find.text('Đăng ký xe mới'), findsOneWidget);
    expect(
      find.byKey(const ValueKey('vehicle-license-plate-field')),
      findsOneWidget,
    );
    expect(find.text('Ảnh xe hoặc Giấy đăng kiểm (Tùy chọn)'), findsOneWidget);
    expect(
      find.byKey(const ValueKey('vehicle-image-placeholder')),
      findsOneWidget,
    );

    await tester.ensureVisible(
      find.byKey(const ValueKey('vehicle-image-placeholder')),
    );
    await tester.tap(find.byKey(const ValueKey('vehicle-image-placeholder')));
    await tester.pumpAndSettle();
    expect(find.text('Ảnh xe chưa thể tải lên'), findsOneWidget);
    await tester.tap(find.text('Đã hiểu'));
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
    await tester.ensureVisible(
      find.byKey(const ValueKey('vehicle-submit-button')),
    );
    await tester.tap(find.byKey(const ValueKey('vehicle-submit-button')));
    await tester.pumpAndSettle();

    expect(gateway.createCalls, 1);
    expect(gateway.lastRequest?.brandId, 1);
    expect(gateway.lastRequest?.modelId, 10);
    expect(find.text('51A-12345'), findsOneWidget);
  });

  testWidgets('route xe giữ navbar 5 mục và chọn Tài khoản', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: CustomerShell(
          currentLocation: '/vehicles',
          child: SizedBox.shrink(),
        ),
      ),
    );

    final navigation = tester.widget<NavigationBar>(find.byType(NavigationBar));
    expect(navigation.destinations, hasLength(5));
    expect(navigation.selectedIndex, 4);
    expect(find.text('Tài khoản'), findsOneWidget);
  });
}

class _FakeVehicleGateway implements VehicleGateway {
  int createCalls = 0;
  CreateCustomerVehicle? lastRequest;

  @override
  Future<CustomerVehicle> createVehicle(CreateCustomerVehicle request) async {
    createCalls += 1;
    lastRequest = request;
    return CustomerVehicle(id: 1, licensePlate: request.licensePlate.trim());
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
