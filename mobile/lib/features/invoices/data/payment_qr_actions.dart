import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import '../domain/payment_models.dart';
import 'payment_service.dart';

abstract interface class PaymentQrActions {
  bool get supported;
  Future<void> save(
    PaymentSession session, {
    required bool Function() isCurrent,
  });
  Future<bool> openMbBank();
}

class AndroidPaymentQrActions implements PaymentQrActions {
  AndroidPaymentQrActions({Dio? downloadClient})
    : _download =
          downloadClient ??
          Dio(
            BaseOptions(
              connectTimeout: const Duration(seconds: 15),
              receiveTimeout: const Duration(seconds: 20),
            ),
          );

  // Deliberately separate from ApiClient: never send the Garage JWT to a QR host.
  final Dio _download;
  static const channel = MethodChannel('com.garage/payment_qr');

  @override
  bool get supported =>
      !kIsWeb && defaultTargetPlatform == TargetPlatform.android;

  @override
  Future<void> save(
    PaymentSession session, {
    required bool Function() isCurrent,
  }) async {
    void validate() {
      if (!isCurrent() ||
          !session.pending ||
          session.paid ||
          !session.expiresAt.isAfter(DateTime.now())) {
        throw const PaymentException(
          'Phiên thanh toán đã thay đổi hoặc hết hạn. Vui lòng kiểm tra lại.',
        );
      }
    }

    if (!supported) {
      throw const PaymentException('Lưu QR hiện được hỗ trợ trên Android.');
    }
    validate();
    final uri = Uri.tryParse(session.qrImageUrl ?? '');
    if (uri == null || uri.scheme != 'https' || uri.host.isEmpty) {
      throw const PaymentException('Không có mã QR hợp lệ để lưu.');
    }
    try {
      final needsPermission =
          await channel.invokeMethod<bool>('needsStoragePermission') ?? false;
      if (needsPermission && !(await Permission.storage.request()).isGranted) {
        throw const PaymentException(
          'Chưa có quyền lưu ảnh. Hãy cấp quyền bộ nhớ cho Garage trong Cài đặt rồi thử lại.',
        );
      }
      validate();
      final response = await _download.get<List<int>>(
        uri.toString(),
        options: Options(responseType: ResponseType.bytes),
      );
      final bytes = response.data;
      if (bytes == null || bytes.isEmpty || bytes.length > 5 * 1024 * 1024) {
        throw const PaymentException('Ảnh QR không hợp lệ. Vui lòng thử lại.');
      }
      validate();
      await channel.invokeMethod<void>('saveQr', {
        'bytes': Uint8List.fromList(bytes),
        'invoiceId': session.invoiceId,
      });
    } on PaymentException {
      rethrow;
    } on Object {
      throw const PaymentException(
        'Chưa lưu được ảnh QR. Kiểm tra kết nối và dung lượng máy rồi thử lại.',
      );
    }
  }

  @override
  Future<bool> openMbBank() async {
    try {
      return await channel.invokeMethod<bool>('openMbBank') ?? false;
    } on Object {
      return false;
    }
  }
}
