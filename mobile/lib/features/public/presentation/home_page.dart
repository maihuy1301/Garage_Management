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
    final fullName = auth.session?.fullName?.trim();
    final name = fullName != null && fullName.isNotEmpty
        ? fullName
        : auth.session?.username ?? 'Chào bạn!';
    return SafeArea(
      child: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: _Header(isAuthenticated: auth.isAuthenticated),
          ),
          SliverToBoxAdapter(
            child: Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 640),
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(20, 20, 20, 28),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          const CircleAvatar(
                            radius: 23,
                            backgroundColor: AppColors.surfaceContainer,
                            child: Icon(
                              Icons.person_rounded,
                              color: AppColors.primary,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text(
                                  'Chào mừng đến với AutoCare',
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: AppColors.onSurfaceVariant,
                                  ),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  name,
                                  style: const TextStyle(
                                    fontSize: 20,
                                    fontWeight: FontWeight.w800,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 20),
                      const _BookingBanner(),
                      const SizedBox(height: 26),
                      const _SectionTitle(title: 'Tiện ích cho bạn'),
                      const SizedBox(height: 16),
                      const _QuickActions(),
                      const SizedBox(height: 22),
                      _ActionCard(
                        icon: Icons.event_available_rounded,
                        title: auth.isAuthenticated
                            ? 'Lịch hẹn của bạn'
                            : 'Chăm xe dễ dàng hơn',
                        description: auth.isAuthenticated
                            ? 'Xem lịch hẹn và theo dõi tiến độ sửa xe.'
                            : 'Đăng nhập để quản lý xe và lịch hẹn của bạn.',
                        route: auth.isAuthenticated ? '/tracking' : '/login',
                        emphasized: true,
                      ),
                      const SizedBox(height: 26),
                      const _SectionTitle(title: 'Chăm sóc xe cùng AutoCare'),
                      const SizedBox(height: 4),
                      const Text(
                        'Chủ động từng bước, an tâm mỗi hành trình.',
                        style: TextStyle(
                          fontSize: 12,
                          color: AppColors.onSurfaceVariant,
                        ),
                      ),
                      const SizedBox(height: 14),
                      const _ActionCard(
                        icon: Icons.car_repair_rounded,
                        title: 'Đặt lịch dịch vụ',
                        description: 'Chọn xe, chi nhánh và dịch vụ phù hợp.',
                        route: '/appointments',
                      ),
                      const SizedBox(height: 10),
                      const _ActionCard(
                        icon: Icons.directions_car_outlined,
                        title: 'Garage của riêng bạn',
                        description:
                            'Thêm xe và quản lý thông tin xe tại một nơi.',
                        route: '/vehicles',
                      ),
                      const SizedBox(height: 10),
                      const _ActionCard(
                        icon: Icons.receipt_long_outlined,
                        title: 'Chi phí rõ ràng',
                        description:
                            'Xem hóa đơn và lịch sử thanh toán của bạn.',
                        route: '/invoices',
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.isAuthenticated});
  final bool isAuthenticated;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        border: Border(bottom: BorderSide(color: AppColors.surfaceContainer)),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
      child: Row(
        children: [
          const Expanded(
            child: FittedBox(
              fit: BoxFit.scaleDown,
              alignment: Alignment.centerLeft,
              child: BrandMark(compact: true),
            ),
          ),
          const SizedBox(width: 8),
          if (!isAuthenticated)
            TextButton(
              onPressed: () => context.go('/login'),
              child: const Text('Đăng nhập'),
            ),
          IconButton.filledTonal(
            tooltip: 'Mở thông báo',
            onPressed: () => context.go('/notifications'),
            style: IconButton.styleFrom(
              backgroundColor: AppColors.surfaceContainerLow,
              foregroundColor: AppColors.primary,
            ),
            icon: const Icon(Icons.notifications_none_rounded),
          ),
        ],
      ),
    );
  }
}

class _BookingBanner extends StatelessWidget {
  const _BookingBanner();

  @override
  Widget build(BuildContext context) {
    return Container(
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(
        color: AppColors.surfaceContainerLow,
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.surfaceContainer),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'CHĂM XE CHỦ ĐỘNG',
                  style: TextStyle(
                    fontSize: 10,
                    letterSpacing: 1.7,
                    color: AppColors.secondary,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 7),
                const Text(
                  'Đặt lịch dễ dàng.\nAn tâm lăn bánh.',
                  style: TextStyle(
                    fontSize: 26,
                    height: 1.15,
                    color: AppColors.primary,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(height: 12),
                FilledButton.icon(
                  key: const ValueKey('home-book'),
                  onPressed: () => context.go('/appointments'),
                  style: FilledButton.styleFrom(
                    backgroundColor: AppColors.secondaryContainer,
                    foregroundColor: AppColors.onSurface,
                    minimumSize: const Size(48, 44),
                    padding: const EdgeInsets.symmetric(
                      horizontal: 14,
                      vertical: 10,
                    ),
                  ),
                  icon: const Icon(Icons.calendar_month_outlined, size: 18),
                  label: const Text(
                    'Đặt lịch ngay',
                    style: TextStyle(fontWeight: FontWeight.w700),
                  ),
                ),
              ],
            ),
          ),
          ExcludeSemantics(
            child: SizedBox(
              height: 160,
              child: ShaderMask(
                blendMode: BlendMode.dstIn,
                shaderCallback: (bounds) => const LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [Colors.transparent, Colors.black, Colors.black],
                  stops: [0, 0.18, 1],
                ).createShader(bounds),
                child: Image.asset(
                  'asset/home_autocare_car.png',
                  fit: BoxFit.cover,
                  alignment: const Alignment(0, 0.4),
                  cacheWidth: 1000,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _QuickActions extends StatelessWidget {
  const _QuickActions();

  static const _actions = [
    (
      'Đặt lịch',
      Icons.calendar_month_outlined,
      '/appointments',
      AppColors.secondary,
    ),
    (
      'Xe của tôi',
      Icons.directions_car_outlined,
      '/vehicles',
      AppColors.primary,
    ),
    ('Theo dõi', Icons.build_outlined, '/tracking', AppColors.tertiary),
    ('Hóa đơn', Icons.receipt_long_outlined, '/invoices', AppColors.primary),
    (
      'Thông báo',
      Icons.notifications_none_rounded,
      '/notifications',
      AppColors.tertiary,
    ),
    (
      'Tài khoản',
      Icons.person_outline_rounded,
      '/account',
      AppColors.secondary,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final columns = MediaQuery.textScalerOf(context).scale(12) > 20 ? 2 : 3;
        final width = (constraints.maxWidth - (columns - 1) * 12) / columns;
        return Wrap(
          spacing: 12,
          runSpacing: 18,
          children: [
            for (final action in _actions)
              SizedBox(
                width: width,
                child: Material(
                  color: Colors.transparent,
                  child: InkWell(
                    key: ValueKey('home-action-${action.$3}'),
                    borderRadius: BorderRadius.circular(16),
                    onTap: () => context.go(action.$3),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 6),
                      child: Column(
                        children: [
                          Container(
                            width: 52,
                            height: 52,
                            decoration: BoxDecoration(
                              color: action.$4.withValues(alpha: 0.08),
                              borderRadius: BorderRadius.circular(17),
                            ),
                            child: Icon(action.$2, color: action.$4, size: 25),
                          ),
                          const SizedBox(height: 9),
                          Text(
                            action.$1,
                            textAlign: TextAlign.center,
                            style: const TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
          ],
        );
      },
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title});
  final String title;

  @override
  Widget build(BuildContext context) => Text(
    title,
    style: const TextStyle(
      fontSize: 18,
      fontWeight: FontWeight.w800,
      color: AppColors.onSurface,
    ),
  );
}

class _ActionCard extends StatelessWidget {
  const _ActionCard({
    required this.icon,
    required this.title,
    required this.description,
    required this.route,
    this.emphasized = false,
  });

  final IconData icon;
  final String title;
  final String description;
  final String route;
  final bool emphasized;

  @override
  Widget build(BuildContext context) {
    final foreground = emphasized ? Colors.white : AppColors.onSurface;
    return Material(
      color: emphasized ? AppColors.primary : Colors.white,
      borderRadius: BorderRadius.circular(18),
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: () => context.go(route),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: emphasized
                      ? Colors.white.withValues(alpha: 0.12)
                      : AppColors.surfaceContainerLow,
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Icon(
                  icon,
                  color: emphasized
                      ? const Color(0xFFFFB690)
                      : AppColors.primary,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: foreground,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      description,
                      style: TextStyle(
                        fontSize: 12,
                        height: 1.45,
                        color: emphasized
                            ? const Color(0xFFDCE5FF)
                            : AppColors.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              Icon(
                Icons.chevron_right_rounded,
                color: emphasized ? const Color(0xFFFFB690) : AppColors.primary,
                size: 22,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
