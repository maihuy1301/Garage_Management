import 'package:dio/dio.dart';

import '../../../core/api/api_client.dart';
import '../domain/invoice_models.dart';

abstract interface class InvoiceGateway {
  Future<List<CustomerInvoice>> loadInvoices();
  Future<CustomerInvoice> loadInvoice(int id);
}

class InvoiceException implements Exception {
  const InvoiceException(this.message);
  final String message;
}

class InvoiceService implements InvoiceGateway {
  const InvoiceService(this._api);
  final ApiClient _api;

  @override
  Future<List<CustomerInvoice>> loadInvoices() => _get('/invoices', (data) {
    final invoices = (data as List)
        .map((item) => CustomerInvoice.fromJson(item as Map<String, dynamic>))
        .toList();
    invoices.sort((a, b) => b.id.compareTo(a.id));
    return invoices;
  });

  // InvoiceResponse includes the payment history and backend-calculated totals.
  @override
  Future<CustomerInvoice> loadInvoice(int id) async {
    if (id <= 0) throw const InvoiceException('Mã hóa đơn không hợp lệ.');
    return _get(
      '/invoices/$id',
      (data) => CustomerInvoice.fromJson(data as Map<String, dynamic>),
    );
  }

  Future<T> _get<T>(String path, T Function(Object?) parse) async {
    try {
      final response = await _api.dio.get<Map<String, dynamic>>(path);
      if (response.data?['success'] != true) {
        throw const InvoiceException(
          'Không thể tải hóa đơn. Vui lòng thử lại.',
        );
      }
      return parse(response.data?['data']);
    } on InvoiceException {
      rethrow;
    } on DioException catch (error) {
      throw InvoiceException(switch (error.response?.statusCode) {
        401 => 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
        403 => 'Bạn không có quyền xem hóa đơn này.',
        404 => 'Không tìm thấy hóa đơn.',
        _ => 'Không thể tải hóa đơn. Vui lòng kiểm tra kết nối và thử lại.',
      });
    } on Object {
      throw const InvoiceException('Dữ liệu hóa đơn từ máy chủ không hợp lệ.');
    }
  }
}
