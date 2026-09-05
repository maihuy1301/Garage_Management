import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/core/auth/jwt_decoder.dart';

void main() {
  test('đọc role và subject từ JWT payload', () {
    final token = _token({
      'sub': 'customer',
      'roles': ['ROLE_CUSTOMER'],
      'exp':
          DateTime.now().add(const Duration(hours: 1)).millisecondsSinceEpoch ~/
          1000,
    });

    final payload = JwtDecoder.decode(token);

    expect(payload.subject, 'customer');
    expect(payload.roles, ['ROLE_CUSTOMER']);
    expect(payload.isExpired, isFalse);
  });

  test('nhận biết JWT đã hết hạn', () {
    final token = _token({
      'sub': 'customer',
      'roles': ['ROLE_CUSTOMER'],
      'exp':
          DateTime.now()
              .subtract(const Duration(minutes: 1))
              .millisecondsSinceEpoch ~/
          1000,
    });

    expect(JwtDecoder.decode(token).isExpired, isTrue);
  });
}

String _token(Map<String, Object> payload) {
  String part(Object value) =>
      base64Url.encode(utf8.encode(jsonEncode(value))).replaceAll('=', '');
  return '${part({'alg': 'HS256', 'typ': 'JWT'})}.${part(payload)}.signature';
}
