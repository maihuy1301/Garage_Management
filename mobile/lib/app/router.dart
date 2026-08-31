import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../core/auth/auth_controller.dart';
import '../features/auth/presentation/login_page.dart';
import '../features/customer/presentation/account_page.dart';
import '../features/customer/presentation/customer_shell.dart';
import '../features/customer/presentation/feature_placeholder_page.dart';
import '../features/public/presentation/home_page.dart';
import '../features/public/presentation/splash_page.dart';
import '../features/technician/presentation/technician_home_page.dart';

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
            builder: (context, state) =>
                LoginPage(returnTo: state.uri.queryParameters['returnTo']),
          ),
          ShellRoute(
            builder: (context, state, child) =>
                CustomerShell(currentLocation: state.uri.path, child: child),
            routes: [
              GoRoute(path: '/', builder: (context, state) => const HomePage()),
              GoRoute(
                path: '/appointments',
                builder: (context, state) => const FeaturePlaceholderPage(
                  icon: Icons.calendar_month_rounded,
                  title: 'Đặt lịch dịch vụ',
                  description:
                      'Luồng đặt lịch và lịch hẹn của bạn sẽ được triển khai ở phase kế tiếp.',
                ),
              ),
              GoRoute(
                path: '/tracking',
                builder: (context, state) => const FeaturePlaceholderPage(
                  icon: Icons.car_repair_rounded,
                  title: 'Theo dõi sửa chữa',
                  description:
                      'Theo dõi tiến độ, hạng mục và báo giá phát sinh tại đây.',
                ),
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
  };

  static String? _redirect(AuthController auth, GoRouterState state) {
    final location = state.uri.path;
    if (!auth.isInitialized) {
      return location == '/splash' ? null : '/splash';
    }

    if (location == '/splash') {
      return auth.session?.isTechnician == true ? '/technician' : '/';
    }

    if (!auth.isAuthenticated && _protectedCustomerPaths.contains(location)) {
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

    if (auth.isAuthenticated && location == '/login') {
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

  static String? _safeReturnTo(String? value) {
    if (value == null || !value.startsWith('/') || value.startsWith('//')) {
      return null;
    }
    return value;
  }
}
