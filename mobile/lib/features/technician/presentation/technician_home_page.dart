import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../../../core/auth/auth_controller.dart';
import '../../../core/theme/app_colors.dart';
import '../../../shared/widgets/brand_mark.dart';

class TechnicianHomePage extends StatelessWidget {
  const TechnicianHomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();
    return Scaffold(
      appBar: AppBar(
        title: const BrandMark(compact: true),
        actions: [
          IconButton(
            tooltip: 'Đăng xuất',
            onPressed: () async {
              await context.read<AuthController>().logout();
              if (context.mounted) context.go('/');
            },
            icon: const Icon(Icons.logout_rounded),
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Xin chào, ${auth.session?.fullName ?? auth.session?.username ?? 'Kỹ thuật viên'}',
                style: Theme.of(context).textTheme.headlineMedium,
              ),
              const SizedBox(height: 8),
              Text(
                'Bạn đã được điều hướng đúng khu vực theo role từ backend.',
                style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                  color: AppColors.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: 28),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Row(
                    children: [
                      Container(
                        width: 58,
                        height: 58,
                        decoration: BoxDecoration(
                          color: AppColors.surfaceContainer,
                          borderRadius: BorderRadius.circular(16),
                        ),
                        child: const Icon(
                          Icons.engineering_rounded,
                          color: AppColors.primary,
                          size: 30,
                        ),
                      ),
                      const SizedBox(width: 16),
                      const Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Khu vực kỹ thuật viên',
                              style: TextStyle(
                                fontWeight: FontWeight.w700,
                                fontSize: 17,
                              ),
                            ),
                            SizedBox(height: 5),
                            Text(
                              'Danh sách công việc và cập nhật tiến độ sẽ được triển khai sau customer MVP.',
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
