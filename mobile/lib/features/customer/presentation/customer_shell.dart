import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class CustomerShell extends StatelessWidget {
  const CustomerShell({
    super.key,
    required this.currentLocation,
    required this.child,
  });

  final String currentLocation;
  final Widget child;

  static const _destinations = [
    ('/', 'Trang chủ', Icons.home_outlined, Icons.home_rounded),
    (
      '/appointments',
      'Đặt lịch',
      Icons.calendar_month_outlined,
      Icons.calendar_month_rounded,
    ),
    ('/tracking', 'Theo dõi', Icons.build_outlined, Icons.build_rounded),
    (
      '/notifications',
      'Thông báo',
      Icons.notifications_outlined,
      Icons.notifications_rounded,
    ),
    ('/account', 'Tài khoản', Icons.person_outline, Icons.person_rounded),
  ];

  @override
  Widget build(BuildContext context) {
    final navigationLocation = currentLocation == '/vehicles'
        ? '/account'
        : currentLocation.startsWith('/tracking/')
        ? '/tracking'
        : currentLocation.startsWith('/appointments/')
        ? '/appointments'
        : currentLocation;
    final index = _destinations.indexWhere(
      (destination) => destination.$1 == navigationLocation,
    );
    return Scaffold(
      body: child,
      bottomNavigationBar: NavigationBar(
        height: 72,
        selectedIndex: index < 0 ? 0 : index,
        labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
        onDestinationSelected: (selectedIndex) {
          context.go(_destinations[selectedIndex].$1);
        },
        destinations: [
          for (final destination in _destinations)
            NavigationDestination(
              icon: Icon(destination.$3),
              selectedIcon: Icon(destination.$4),
              label: destination.$2,
            ),
        ],
      ),
    );
  }
}
