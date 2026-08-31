class AuthSession {
  const AuthSession({
    required this.accessToken,
    required this.username,
    required this.roles,
    this.userId,
    this.fullName,
  });

  final String accessToken;
  final int? userId;
  final String username;
  final String? fullName;
  final List<String> roles;

  bool get isCustomer => roles.contains('ROLE_CUSTOMER');
  bool get isTechnician => roles.contains('ROLE_TECHNICIAN');
}
