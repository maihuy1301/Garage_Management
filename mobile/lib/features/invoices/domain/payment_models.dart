class PaymentReceiver {
  PaymentReceiver.fromJson(Map<String, dynamic> json)
    : bankCode = json['bankCode'] as String,
      bankName = json['bankName'] as String,
      accountNumber = json['accountNumber'] as String,
      accountName = json['accountName'] as String;
  final String bankCode, bankName, accountNumber, accountName;
}

class PaymentOptions {
  PaymentOptions.fromJson(Map<String, dynamic> json)
    : available = json['available'] as bool,
      message = json['message'] as String,
      environment = json['environment'] as String,
      remaining = _amount(json['remainingAmount']),
      receivers = (json['receivers'] as List)
          .map((e) => PaymentReceiver.fromJson(e as Map<String, dynamic>))
          .toList();
  final bool available;
  final String message, environment;
  final double remaining;
  final List<PaymentReceiver> receivers;
}

class PaymentSession {
  PaymentSession.fromJson(Map<String, dynamic> json)
    : id = json['sessionId'] as String,
      invoiceId = (json['invoiceId'] as num).toInt(),
      status = json['status'] as String,
      environment = json['environment'] as String,
      amount = _amount(json['amount']),
      content = json['transferContent'] as String,
      expiresAt = DateTime.parse(json['expiresAt'] as String),
      receiver = PaymentReceiver.fromJson(
        json['receiver'] as Map<String, dynamic>,
      ),
      qrImageUrl = json['qrImageUrl'] as String?,
      invoiceStatus = json['invoiceStatus'] as String,
      remaining = _amount(json['remainingAmount']);
  final String id, status, environment, content, invoiceStatus;
  final int invoiceId;
  final double amount, remaining;
  final DateTime expiresAt;
  final PaymentReceiver receiver;
  final String? qrImageUrl;
  bool get pending => status == 'PENDING';
  bool get paid => invoiceStatus == 'DA_THANH_TOAN' && remaining == 0;
}

double _amount(Object? value) {
  final amount = value is num
      ? value.toDouble()
      : double.parse(value as String);
  if (!amount.isFinite || amount < 0) {
    throw const FormatException('Invalid amount');
  }
  return amount;
}
