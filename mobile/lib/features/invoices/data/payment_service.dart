import 'package:dio/dio.dart';
import '../../../core/api/api_client.dart';
import '../domain/payment_models.dart';

abstract interface class PaymentGateway {
  Future<PaymentOptions> options(int invoiceId);
  Future<PaymentSession> create(int invoiceId);
  Future<PaymentSession> status(int invoiceId, String sessionId);
}

class PaymentException implements Exception {
  const PaymentException(this.message, {this.statusCode});
  final String message;
  final int? statusCode;
  bool get accessUnavailable => {401, 403, 404}.contains(statusCode);
}

class PaymentService implements PaymentGateway {
  const PaymentService(this._api);
  final ApiClient _api;

  @override
  Future<PaymentOptions> options(int invoiceId) => _request(
    invoiceId,
    '/invoices/$invoiceId/payment-options',
    PaymentOptions.fromJson,
  );
  @override
  Future<PaymentSession> create(int invoiceId) => _request(
    invoiceId,
    '/invoices/$invoiceId/payment-sessions',
    PaymentSession.fromJson,
    post: true,
  );
  @override
  Future<PaymentSession> status(int invoiceId, String sessionId) => _request(
    invoiceId,
    '/invoices/$invoiceId/payment-sessions/${Uri.encodeComponent(sessionId)}',
    PaymentSession.fromJson,
  );

  Future<T> _request<T>(
    int invoiceId,
    String path,
    T Function(Map<String, dynamic>) parse, {
    bool post = false,
  }) async {
    if (invoiceId <= 0) {
      throw const PaymentException('Mã hóa đơn không hợp lệ.');
    }
    try {
      final response = post
          ? await _api.dio.post<Map<String, dynamic>>(path)
          : await _api.dio.get<Map<String, dynamic>>(path);
      if (response.data?['success'] != true) {
        throw const PaymentException('Không thể tải thông tin thanh toán.');
      }
      return parse(response.data!['data'] as Map<String, dynamic>);
    } on PaymentException {
      rethrow;
    } on DioException catch (e) {
      final data = e.response?.data;
      final validation = data is Map ? data['message'] : null;
      throw PaymentException(switch (e.response?.statusCode) {
        400 || 409 =>
          validation is String
              ? validation
              : 'Hóa đơn đã thay đổi. Vui lòng tải lại.',
        401 => 'Phiên đăng nhập đã hết hạn.',
        403 => 'Bạn không có quyền thanh toán hóa đơn này.',
        404 => 'Không tìm thấy thông tin thanh toán.',
        _ =>
          'Chưa kiểm tra được thanh toán. Vui lòng thử lại, không chuyển thêm tiền nếu bạn đã chuyển.',
      }, statusCode: e.response?.statusCode);
    } on Object {
      throw const PaymentException(
        'Dữ liệu thanh toán không hợp lệ. Vui lòng thử lại.',
      );
    }
  }
}
