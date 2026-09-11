import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../core/auth/auth_controller.dart';
import '../features/appointments/presentation/appointment_booking_page.dart';
import '../features/appointments/presentation/appointment_detail_page.dart';
import '../features/appointments/presentation/appointment_tracking_page.dart';
import '../features/auth/presentation/login_page.dart';
import '../features/auth/presentation/register_page.dart';
import '../features/customer/presentation/account_page.dart';
import '../features/customer/presentation/customer_shell.dart';
import '../features/customer/presentation/feature_placeholder_page.dart';
import '../features/public/presentation/home_page.dart';
import '../features/public/presentation/splash_page.dart';
import '../features/technician/presentation/technician_home_page.dart';
import '../features/vehicles/presentation/vehicles_page.dart';

class AppRouter {
  AppRouter(AuthController authController)
    : router = GoRouter(
        initialLocation: '/splash',
        refreshListenable: authController,
        redirect: (context, state) => _redirect(authController, state),
        routes: [
          GoRoute(
            path: '/splash',
            builder: (context, state) => const SplashPage(),
          ),
          GoRoute(
            path: '/login',
            builder: (context, state) => LoginPage(
              returnTo: state.uri.queryParameters['returnTo'],
              initialUsername: state.uri.queryParameters['username'],
              registrationSucceeded:
                  state.uri.queryParameters['registered'] == 'true',
            ),
          ),
          GoRoute(
            path: '/register',
            builder: (context, state) => const RegisterPage(),
          ),
          ShellRoute(
            builder: (context, state, child) =>
                CustomerShell(currentLocation: state.uri.path, child: child),
            routes: [
              GoRoute(path: '/', builder: (context, state) => const HomePage()),
              GoRoute(
                path: '/appointments',
                builder: (context, state) => AppointmentBookingPage(
                  initialVehicleId: int.tryParse(
                    state.uri.queryParameters['vehicleId'] ?? '',
                  ),
                ),
              ),
              GoRoute(
                path: '/tracking',
                builder: (context, state) => const AppointmentTrackingPage(),
                routes: [
                  GoRoute(
                    path: ':appointmentId',
                    builder: (context, state) => AppointmentDetailPage(
                      appointmentId:
                          int.tryParse(
                            state.pathParameters['appointmentId'] ?? '',
                          ) ??
                          -1,
                    ),
                  ),
                ],
              ),
              GoRoute(
                path: '/notifications',
                builder: (context, state) => const FeaturePlaceholderPage(
                  icon: Icons.notifications_active_rounded,
                  title: 'Thông báo của tôi',
                  description:
                      'Thông báo lịch hẹn và tiến độ sửa chữa sẽ xuất hiện tại đây.',
                ),
              ),
              GoRoute(
                path: '/account',
                builder: (context, state) => const AccountPage(),
              ),
              GoRoute(
                path: '/vehicles',
                builder: (context, state) => const VehiclesPage(),
              ),
            ],
          ),
          GoRoute(
            path: '/technician',
            builder: (context, state) => const TechnicianHomePage(),
          ),
        ],
      );

  final GoRouter router;

  static const _protectedCustomerPaths = {
    '/appointments',
    '/tracking',
    '/notifications',
    '/account',
    '/vehicles',
  };

  static String? _redirect(AuthController auth, GoRouterState state) {
    final location = state.uri.path;
    if (!auth.isInitialized) {
      return location == '/splash' ? null : '/splash';
    }

    if (location == '/splash') {
      return auth.session?.isTechnician == true ? '/technician' : '/';
    }

    if (!auth.isAuthenticated && _isProtectedCustomerPath(location)) {
      return Uri(
        path: '/login',
        queryParameters: {'returnTo': state.uri.toString()},
      ).toString();
    }

    if (!auth.isAuthenticated && location == '/technician') {
      return Uri(
        path: '/login',
        queryParameters: const {'returnTo': '/technician'},
      ).toString();
    }

    if (auth.isAuthenticated &&
        (location == '/login' || location == '/register')) {
      if (auth.session?.isTechnician == true) return '/technician';
      final returnTo = state.uri.queryParameters['returnTo'];
      return _safeReturnTo(returnTo) ?? '/';
    }

    if (auth.session?.isTechnician == true && location != '/technician') {
      return '/technician';
    }
    if (auth.session?.isCustomer == true && location == '/technician') {
      return '/';
    }
    return null;
  }

  static bool _isProtectedCustomerPath(String location) {
    return _protectedCustomerPaths.any(
      (path) => location == path || location.startsWith('$path/'),
    );
  }

  static String? _safeReturnTo(String? value) {
    if (value == null || !value.startsWith('/') || value.startsWith('//')) {
      return null;
    }
    return value;
  }
}
