import 'dart:convert';

class JwtPayload {
  const JwtPayload({
    required this.subject,
    required this.roles,
    required this.expiresAt,
  });

  final String subject;
  final List<String> roles;
  final DateTime? expiresAt;

  bool get isExpired =>
      expiresAt != null && DateTime.now().toUtc().isAfter(expiresAt!);
}

abstract final class JwtDecoder {
  static JwtPayload decode(String token) {
    final parts = token.split('.');
    if (parts.length != 3) {
      throw const FormatException('JWT không hợp lệ');
    }

    final payloadBytes = base64Url.decode(base64Url.normalize(parts[1]));
    final payload =
        jsonDecode(utf8.decode(payloadBytes)) as Map<String, dynamic>;
    final rawRoles = payload['roles'];
    final expiresAt = payload['exp'] is num
        ? DateTime.fromMillisecondsSinceEpoch(
            (payload['exp'] as num).toInt() * 1000,
            isUtc: true,
          )
        : null;

    return JwtPayload(
      subject: payload['sub']?.toString() ?? '',
      roles: rawRoles is List
          ? rawRoles.map((role) => role.toString()).toList(growable: false)
          : const [],
      expiresAt: expiresAt,
    );
  }
}
