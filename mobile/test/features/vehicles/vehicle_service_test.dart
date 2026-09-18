import 'dart:io';

import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/api/api_client.dart';
import 'package:garage_mobile/core/storage/secure_session_storage.dart';
import 'package:garage_mobile/features/vehicles/data/vehicle_service.dart';
import 'package:garage_mobile/features/vehicles/domain/vehicle_models.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late Dio dio;
  late VehicleService service;

  setUp(() {
    FlutterSecureStorage.setMockInitialValues({'access_token': 'test-token'});
    dio = Dio(BaseOptions(baseUrl: 'http://localhost/api'));
    const storage = SecureSessionStorage(FlutterSecureStorage());
    service = VehicleService(ApiClient(dio, storage));
  });

  test('tải danh sách xe chính chủ qua endpoint vehicles', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          expect(options.path, '/vehicles');
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 200,
              data: {
                'success': true,
                'data': [
                  {
                    'maXe': 7,
                    'bienSo': '51A-11111',
                    'maHangXe': 1,
                    'tenHangXe': 'Toyota',
                    'maModel': 10,
                    'tenModel': 'Camry',
                    'trangThai': true,
                  },
                ],
              },
            ),
          );
        },
      ),
    );

    final vehicles = await service.loadVehicles();

    expect(vehicles.single.licensePlate, '51A-11111');
    expect(vehicles.single.description, 'Toyota Camry');
    expect(vehicles.single.brandId, 1);
    expect(vehicles.single.modelId, 10);
  });

  test('tải hãng xe và model theo hãng cho cascading dropdown', () async {
    final requestedPaths = <String>[];
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          requestedPaths.add(options.path);
          final data = options.path == '/brands'
              ? [
                  {'maHangXe': 1, 'tenHangXe': 'Toyota'},
                ]
              : [
                  {
                    'maModel': 10,
                    'maHangXe': 1,
                    'tenHangXe': 'Toyota',
                    'tenModel': 'Camry',
                  },
                ];
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 200,
              data: {'success': true, 'data': data},
            ),
          );
        },
      ),
    );

    final brands = await service.loadBrands();
    final models = await service.loadModels(brands.single.id);

    expect(requestedPaths, ['/brands', '/brands/1/models']);
    expect(brands.single.name, 'Toyota');
    expect(models.single.name, 'Camry');
    expect(models.single.brandId, 1);
  });

  test('thêm xe không gửi mã khách hàng từ mobile', () async {
    Map<String, dynamic>? payload;
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          payload = Map<String, dynamic>.from(options.data as Map);
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 201,
              data: {
                'success': true,
                'data': {
                  'maXe': 8,
                  'bienSo': '51B-22222',
                  'maHangXe': 2,
                  'tenHangXe': 'Honda',
                  'maModel': 20,
                  'tenModel': 'Civic',
                  'trangThai': true,
                },
              },
            ),
          );
        },
      ),
    );

    final created = await service.createVehicle(
      const CreateCustomerVehicle(
        licensePlate: ' 51b-22222 ',
        brandId: 2,
        modelId: 20,
        odometer: 1200,
      ),
    );

    expect(payload, isNot(contains('maKhachHang')));
    expect(payload?['bienSo'], '51B-22222');
    expect(payload?['maHangXe'], 2);
    expect(payload?['maModel'], 20);
    expect(payload, isNot(contains('hangXe')));
    expect(payload, isNot(contains('model')));
    expect(created.id, 8);
  });

  test('upload ảnh xe gửi multipart đúng endpoint', () async {
    final tempDir = await Directory.systemTemp.createTemp('vehicle-image-test');
    final image = File('${tempDir.path}${Platform.pathSeparator}car.jpg');
    await image.writeAsBytes([0xFF, 0xD8, 0xFF, 0x00]);
    addTearDown(() => tempDir.delete(recursive: true));

    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          expect(options.path, '/vehicles/8/image');
          expect(options.data, isA<FormData>());
          final form = options.data as FormData;
          expect(form.files.single.key, 'file');
          handler.resolve(
            Response<Map<String, dynamic>>(
              requestOptions: options,
              statusCode: 200,
              data: {'success': true, 'data': <String, dynamic>{}},
            ),
          );
        },
      ),
    );

    await service.uploadVehicleImage(8, image.path);
  });

  test('ảnh xe không tồn tại trả null để UI dùng placeholder', () async {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) {
          handler.reject(
            DioException(
              requestOptions: options,
              response: Response<List<int>>(
                requestOptions: options,
                statusCode: 404,
              ),
            ),
          );
        },
      ),
    );

    expect(await service.loadVehicleImage(8), isNull);
  });
}
