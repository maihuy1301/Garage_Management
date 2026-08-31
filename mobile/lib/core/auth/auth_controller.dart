import 'package:flutter/foundation.dart';

import 'auth_exception.dart';
import 'auth_repository.dart';
import 'auth_session.dart';

class AuthController extends ChangeNotifier {
  AuthController(this._repository);

  final AuthRepository _repository;
  AuthSession? _session;
  bool _isInitialized = false;
  bool _isSubmitting = false;
  String? _errorMessage;

  AuthSession? get session => _session;
  bool get isInitialized => _isInitialized;
  bool get isAuthenticated => _session != null;
  bool get isSubmitting => _isSubmitting;
  String? get errorMessage => _errorMessage;

  Future<void> restoreSession({
    Duration minimumSplashDuration = Duration.zero,
  }) async {
    final startedAt = DateTime.now();
    _session = await _repository.restoreSession();

    final elapsed = DateTime.now().difference(startedAt);
    final remaining = minimumSplashDuration - elapsed;
    if (remaining > Duration.zero) {
      await Future<void>.delayed(remaining);
    }

    _isInitialized = true;
    notifyListeners();
  }

  Future<bool> login({
    required String username,
    required String password,
  }) async {
    _isSubmitting = true;
    _errorMessage = null;
    notifyListeners();
    try {
      final candidate = await _repository.login(
        username: username,
        password: password,
      );
      if (!candidate.isCustomer && !candidate.isTechnician) {
        await _repository.logout();
        throw const AuthException(
          'Ứng dụng mobile chỉ dành cho khách hàng và kỹ thuật viên.',
        );
      }
      _session = candidate;
      return true;
    } on AuthException catch (error) {
      _errorMessage = error.message;
      return false;
    } finally {
      _isSubmitting = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    await _repository.logout();
    _session = null;
    _errorMessage = null;
    notifyListeners();
  }

  void clearError() {
    if (_errorMessage == null) return;
    _errorMessage = null;
    notifyListeners();
  }
}
