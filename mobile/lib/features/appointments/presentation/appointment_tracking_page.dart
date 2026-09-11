import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../../../core/theme/app_colors.dart';
import '../data/appointment_service.dart';
import '../domain/appointment_models.dart';

class AppointmentTrackingPage extends StatefulWidget {
  const AppointmentTrackingPage({super.key, this.gateway});

  final AppointmentGateway? gateway;

  @override
  State<AppointmentTrackingPage> createState() =>
      _AppointmentTrackingPageState();
}

class _AppointmentTrackingPageState extends State<AppointmentTrackingPage> {
  late AppointmentGateway _gateway;
  bool _initialized = false;
  bool _isLoading = true;
  String? _error;
  List<AppointmentBookingResult> _appointments = const [];

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_initialized) return;
    _gateway = widget.gateway ?? context.read<AppointmentGateway>();
    _initialized = true;
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final appointments = await _gateway.loadMyAppointments();
      if (mounted) setState(() => _appointments = appointments);
    } on AppointmentException catch (error) {
      if (mounted) setState(() => _error = error.message);
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final activeCount = _appointments
        .where((appointment) => !appointment.isCancelled)
        .length;
    return SafeArea(
      child: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          key: const ValueKey('appointment-tracking-page'),
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 32),
          children: [
            Container(
              padding: const EdgeInsets.all(22),
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(20),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Icon(
                    Icons.car_repair_rounded,
                    color: Colors.white,
                    size: 34,
                  ),
                  const SizedBox(height: 14),
                  Text(
                    'Theo dõi lịch hẹn',
                    style: Theme.of(
                      context,
                    ).textTheme.headlineMedium?.copyWith(color: Colors.white),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '$activeCount lịch đang hoạt động · Kéo xuống để cập nhật',
                    style: const TextStyle(color: Colors.white70),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 20),
            if (_isLoading)
              const _TrackingMessage(
                key: ValueKey('tracking-loading'),
                icon: Icons.sync_rounded,
                title: 'Đang tải trạng thái',
                message: 'Vui lòng chờ trong giây lát.',
                showProgress: true,
              )
            else if (_error != null)
              _TrackingMessage(
                key: const ValueKey('tracking-error'),
                icon: Icons.cloud_off_rounded,
                title: 'Chưa tải được lịch hẹn',
                message: _error!,
                actionLabel: 'Thử lại',
                onAction: _load,
              )
            else if (_appointments.isEmpty)
              _TrackingMessage(
                key: const ValueKey('tracking-empty'),
                icon: Icons.event_available_rounded,
                title: 'Chưa có lịch để theo dõi',
                message: 'Hãy đặt lịch dịch vụ trước để theo dõi tại đây.',
                actionLabel: 'Đặt lịch ngay',
                onAction: () => context.go('/appointments'),
              )
            else ...[
              Text(
                'Lịch hẹn của bạn',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 12),
              for (final appointment in _appointments) ...[
                _TrackingCard(
                  appointment: appointment,
                  onTap: () => context.push('/tracking/${appointment.id}'),
                ),
                const SizedBox(height: 12),
              ],
            ],
          ],
        ),
      ),
    );
  }
}

class _TrackingCard extends StatelessWidget {
  const _TrackingCard({required this.appointment, required this.onTap});

  final AppointmentBookingResult appointment;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final status = _statusPresentation(appointment.status);
    return Card(
      key: ValueKey('tracking-card-${appointment.id}'),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      color: status.$2.withValues(alpha: 0.1),
                      shape: BoxShape.circle,
                    ),
                    child: Icon(status.$3, color: status.$2),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          appointment.vehicleLicensePlate,
                          style: Theme.of(context).textTheme.titleLarge,
                        ),
                        Text(
                          appointment.vehicleDescription.isEmpty
                              ? appointment.branchName
                              : appointment.vehicleDescription,
                        ),
                      ],
                    ),
                  ),
                  const Icon(Icons.chevron_right_rounded),
                ],
              ),
              const SizedBox(height: 14),
              LinearProgressIndicator(
                value: appointment.isCancelled
                    ? 0
                    : (appointment.progressStep + 1) / 4,
                color: status.$2,
                backgroundColor: AppColors.surfaceContainer,
                borderRadius: BorderRadius.circular(999),
              ),
              const SizedBox(height: 10),
              Row(
                children: [
                  Expanded(
                    child: Text(
                      status.$1,
                      style: TextStyle(
                        color: status.$2,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                  Text(_formatDateTime(appointment.appointmentTime)),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _TrackingMessage extends StatelessWidget {
  const _TrackingMessage({
    super.key,
    required this.icon,
    required this.title,
    required this.message,
    this.showProgress = false,
    this.actionLabel,
    this.onAction,
  });

  final IconData icon;
  final String title;
  final String message;
  final bool showProgress;
  final String? actionLabel;
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            Icon(icon, size: 42, color: AppColors.primary),
            const SizedBox(height: 12),
            Text(title, style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 6),
            Text(message, textAlign: TextAlign.center),
            if (showProgress) ...[
              const SizedBox(height: 18),
              const CircularProgressIndicator(),
            ],
            if (actionLabel != null && onAction != null) ...[
              const SizedBox(height: 18),
              FilledButton(onPressed: onAction, child: Text(actionLabel!)),
            ],
          ],
        ),
      ),
    );
  }
}

(String, Color, IconData) _statusPresentation(String status) {
  return switch (status) {
    'CHO_XAC_NHAN' => ('Chờ xác nhận', AppColors.warning, Icons.schedule),
    'DA_XAC_NHAN' => ('Đã xác nhận', AppColors.primary, Icons.event_available),
    'DA_TIEP_NHAN' => ('Đã tiếp nhận', AppColors.primary, Icons.car_repair),
    'HOAN_TAT' => ('Hoàn tất', AppColors.success, Icons.check_circle),
    'HUY' || 'DA_HUY' => ('Đã hủy', AppColors.danger, Icons.cancel),
    'KHONG_DEN' => ('Không đến', AppColors.danger, Icons.event_busy),
    _ => (status.replaceAll('_', ' '), AppColors.onSurfaceVariant, Icons.info),
  };
}

String _formatDateTime(DateTime value) {
  String twoDigits(int number) => number.toString().padLeft(2, '0');
  return '${twoDigits(value.hour)}:${twoDigits(value.minute)} · '
      '${twoDigits(value.day)}/${twoDigits(value.month)}/${value.year}';
}
