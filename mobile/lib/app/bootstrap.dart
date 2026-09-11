import 'package:dio/dio.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:provider/provider.dart';

import '../core/api/api_client.dart';
import '../core/auth/auth_controller.dart';
import '../core/auth/auth_repository.dart';
import '../core/config/app_config.dart';
import '../core/storage/secure_session_storage.dart';
import '../features/appointments/data/appointment_service.dart';
import '../features/vehicles/data/vehicle_service.dart';
import 'app.dart';

Future<void> bootstrap() async {
  const secureStorage = FlutterSecureStorage();
  final sessionStorage = SecureSessionStorage(secureStorage);
  final apiClient = ApiClient(
    Dio(
      BaseOptions(
        baseUrl: AppConfig.apiBaseUrl,
        connectTimeout: const Duration(seconds: 12),
        receiveTimeout: const Duration(seconds: 12),
        headers: const {'Accept': 'application/json'},
      ),
    ),
    sessionStorage,
  );
  final authController = AuthController(
    AuthRepository(apiClient, sessionStorage),
  );

  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider.value(value: authController),
        Provider<AppointmentGateway>(
          create: (_) => AppointmentService(apiClient),
        ),
        Provider<VehicleGateway>(create: (_) => VehicleService(apiClient)),
      ],
      child: const GarageApp(),
    ),
  );

  await authController.restoreSession(
    minimumSplashDuration: const Duration(milliseconds: 1600),
  );
}
