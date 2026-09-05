import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../../../core/auth/auth_controller.dart';
import '../../../core/theme/app_colors.dart';
import '../../../shared/widgets/brand_mark.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();
    final name = auth.session?.fullName?.trim();
    return SafeArea(
      child: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: Row(
                children: [
                  const BrandMark(compact: true),
                  const Spacer(),
                  if (auth.isAuthenticated)
                    CircleAvatar(
                      backgroundColor: AppColors.surfaceContainer,
                      child: Text(
                        _initial(name ?? auth.session!.username),
                        style: const TextStyle(
                          color: AppColors.primary,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    )
                  else
                    TextButton.icon(
                      onPressed: () => context.go('/login'),
                      icon: const Icon(Icons.login_rounded, size: 19),
                      label: const Text('Đăng nhập'),
                    ),
                ],
              ),
            ),
          ),
          SliverPadding(
            padding: const EdgeInsets.fromLTRB(16, 16, 16, 28),
            sliver: SliverList.list(
              children: [
                Text(
                  auth.isAuthenticated
                      ? 'Xin chào, ${name?.isNotEmpty == true ? name : auth.session!.username}'
                      : 'Chăm sóc xe nhẹ nhàng hơn',
                  style: Theme.of(context).textTheme.headlineMedium,
                ),
                const SizedBox(height: 6),
                Text(
                  'Đặt lịch, theo dõi sửa chữa và nắm rõ mọi chi phí trong một ứng dụng.',
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: AppColors.onSurfaceVariant,
                  ),
                ),
                const SizedBox(height: 22),
                _HeroBanner(onBook: () => context.go('/appointments')),
                const SizedBox(height: 28),
                Text(
                  'Truy cập nhanh',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 14),
                const _QuickActions(),
                const SizedBox(height: 30),
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        'Dịch vụ nổi bật',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                    ),
                    Text(
                      'Minh bạch • Chuyên nghiệp',
                      style: Theme.of(context).textTheme.labelSmall?.copyWith(
                        color: AppColors.primary,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 14),
                const _ServiceCard(
                  icon: Icons.oil_barrel_outlined,
                  title: 'Bảo dưỡng định kỳ',
                  description:
                      'Kiểm tra các hạng mục quan trọng theo tình trạng và mốc vận hành của xe.',
                ),
                const SizedBox(height: 12),
                const _ServiceCard(
                  icon: Icons.car_repair_outlined,
                  title: 'Chẩn đoán & sửa chữa',
                  description:
                      'Theo dõi tiến độ thực hiện và duyệt chi phí phát sinh trước khi sửa.',
                ),
                const SizedBox(height: 12),
                const _ServiceCard(
                  icon: Icons.receipt_long_outlined,
                  title: 'Hóa đơn rõ ràng',
                  description:
                      'Xem chi tiết tiền công, phụ tùng và trạng thái thanh toán tại một nơi.',
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  static String _initial(String value) {
    final trimmed = value.trim();
    return trimmed.isEmpty ? 'A' : trimmed.characters.first.toUpperCase();
  }
}

class _HeroBanner extends StatelessWidget {
  const _HeroBanner({required this.onBook});

  final VoidCallback onBook;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [AppColors.primary, AppColors.primaryContainer],
        ),
        borderRadius: BorderRadius.circular(22),
        boxShadow: [
          BoxShadow(
            color: AppColors.primary.withValues(alpha: 0.18),
            blurRadius: 22,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Stack(
        children: [
          const Positioned(
            right: -12,
            bottom: -20,
            child: Icon(
              Icons.directions_car_filled_rounded,
              size: 126,
              color: Color(0x1FFFFFFF),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 5,
                ),
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.13),
                  borderRadius: BorderRadius.circular(999),
                ),
                child: const Text(
                  'AUTOCARE ĐA CHI NHÁNH',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 11,
                    letterSpacing: 1.1,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ),
              const SizedBox(height: 18),
              const SizedBox(
                width: 235,
                child: Text(
                  'An tâm trên mọi hành trình',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 26,
                    height: 1.18,
                    fontWeight: FontWeight.w800,
                  ),
                ),
              ),
              const SizedBox(height: 9),
              const SizedBox(
                width: 245,
                child: Text(
                  'Chủ động chọn thời gian phù hợp và theo dõi xe ngay trên điện thoại.',
                  style: TextStyle(color: Color(0xFFDCE5FF), height: 1.45),
                ),
              ),
              const SizedBox(height: 20),
              FilledButton.icon(
                onPressed: onBook,
                style: FilledButton.styleFrom(
                  backgroundColor: AppColors.secondaryContainer,
                  foregroundColor: Colors.white,
                ),
                icon: const Icon(Icons.calendar_month_rounded),
                label: const Text('Đặt lịch ngay'),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _QuickActions extends StatelessWidget {
  const _QuickActions();

  @override
  Widget build(BuildContext context) {
    const actions = [
      ('Đặt lịch', Icons.calendar_month_rounded, '/appointments'),
      ('Theo dõi', Icons.car_repair_rounded, '/tracking'),
      ('Thông báo', Icons.notifications_rounded, '/notifications'),
      ('Tài khoản', Icons.person_rounded, '/account'),
    ];
    return Row(
      children: [
        for (var index = 0; index < actions.length; index++) ...[
          if (index > 0) const SizedBox(width: 10),
          Expanded(
            child: InkWell(
              onTap: () => context.go(actions[index].$3),
              borderRadius: BorderRadius.circular(16),
              child: Container(
                padding: const EdgeInsets.symmetric(
                  vertical: 14,
                  horizontal: 4,
                ),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: AppColors.outlineVariant),
                ),
                child: Column(
                  children: [
                    Container(
                      width: 46,
                      height: 46,
                      decoration: const BoxDecoration(
                        color: AppColors.surfaceContainerLow,
                        shape: BoxShape.circle,
                      ),
                      child: Icon(actions[index].$2, color: AppColors.primary),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      actions[index].$1,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 11.5,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ],
    );
  }
}

class _ServiceCard extends StatelessWidget {
  const _ServiceCard({
    required this.icon,
    required this.title,
    required this.description,
  });

  final IconData icon;
  final String title;
  final String description;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              width: 52,
              height: 52,
              decoration: BoxDecoration(
                color: AppColors.surfaceContainerLow,
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(icon, color: AppColors.primary),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(fontWeight: FontWeight.w700),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    description,
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
