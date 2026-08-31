import 'package:dio/dio.dart';

import '../storage/secure_session_storage.dart';

class ApiClient {
  ApiClient(this.dio, SecureSessionStorage sessionStorage) {
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = await sessionStorage.readToken();
          if (token != null && token.isNotEmpty) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          handler.next(options);
        },
      ),
    );
  }

  final Dio dio;
}
