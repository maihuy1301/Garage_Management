import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../../../core/theme/app_colors.dart';
import '../data/appointment_service.dart';
import '../domain/appointment_models.dart';

class AppointmentDetailPage extends StatefulWidget {
  const AppointmentDetailPage({
    super.key,
    required this.appointmentId,
    this.gateway,
  });

  final int appointmentId;
  final AppointmentGateway? gateway;

  @override
  State<AppointmentDetailPage> createState() => _AppointmentDetailPageState();
}

class _AppointmentDetailPageState extends State<AppointmentDetailPage> {
  late AppointmentGateway _gateway;
  bool _initialized = false;
  bool _isLoading = true;
  String? _error;
  AppointmentBookingResult? _appointment;

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
    if (widget.appointmentId < 1) {
      setState(() {
        _isLoading = false;
        _error = 'Mã lịch hẹn không hợp lệ.';
      });
      return;
    }
    try {
      final appointment = await _gateway.loadAppointment(widget.appointmentId);
      if (mounted) setState(() => _appointment = appointment);
    } on AppointmentException catch (error) {
      if (mounted) setState(() => _error = error.message);
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          key: const ValueKey('appointment-detail-page'),
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.fromLTRB(20, 10, 20, 32),
          children: [
            Row(
              children: [
                IconButton(
                  tooltip: 'Quay lại',
                  onPressed: () => context.pop(),
                  icon: const Icon(Icons.arrow_back_rounded),
                ),
                const SizedBox(width: 4),
                Expanded(
                  child: Text(
                    'Chi tiết lịch hẹn',
                    style: Theme.of(context).textTheme.headlineMedium,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            if (_isLoading)
              const _DetailMessage(
                key: ValueKey('appointment-detail-loading'),
                icon: Icons.sync_rounded,
                title: 'Đang tải chi tiết',
                message: 'Vui lòng chờ trong giây lát.',
                showProgress: true,
              )
            else if (_error != null)
              _DetailMessage(
                key: const ValueKey('appointment-detail-error'),
                icon: Icons.cloud_off_rounded,
                title: 'Chưa tải được lịch hẹn',
                message: _error!,
                actionLabel: 'Thử lại',
                onAction: _load,
              )
            else if (_appointment != null)
              ..._buildAppointment(context, _appointment!),
          ],
        ),
      ),
    );
  }

  List<Widget> _buildAppointment(
    BuildContext context,
    AppointmentBookingResult appointment,
  ) {
    final status = _statusPresentation(appointment.status);
    return [
      Container(
        padding: const EdgeInsets.all(22),
        decoration: BoxDecoration(
          color: AppColors.primary,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.directions_car, color: Colors.white, size: 30),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    appointment.vehicleLicensePlate,
                    style: Theme.of(
                      context,
                    ).textTheme.headlineMedium?.copyWith(color: Colors.white),
                  ),
                ),
              ],
            ),
            if (appointment.vehicleDescription.isNotEmpty) ...[
              const SizedBox(height: 4),
              Text(
                appointment.vehicleDescription,
                style: const TextStyle(color: Colors.white70),
              ),
            ],
            const SizedBox(height: 16),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(
                status.$1,
                style: TextStyle(color: status.$2, fontWeight: FontWeight.w700),
              ),
            ),
          ],
        ),
      ),
      if (appointment.isCancelled) ...[
        const SizedBox(height: 16),
        Card(
          color: AppColors.danger.withValues(alpha: 0.06),
          child: const Padding(
            padding: EdgeInsets.all(18),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(Icons.info_outline, color: AppColors.danger),
                SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Lịch hẹn này không còn hoạt động. Liên hệ garage nếu bạn cần hỗ trợ đặt lịch mới.',
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
      const SizedBox(height: 16),
      Card(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Tiến trình lịch hẹn',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 18),
              _Timeline(
                currentStep: appointment.progressStep,
                isCancelled: appointment.isCancelled,
              ),
            ],
          ),
        ),
      ),
      const SizedBox(height: 16),
      Card(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Thông tin lịch hẹn',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 16),
              _InfoRow(icon: Icons.tag, text: 'Mã lịch #${appointment.id}'),
              _InfoRow(icon: Icons.store, text: appointment.branchName),
              _InfoRow(
                icon: Icons.schedule,
                text: _formatDateTime(appointment.appointmentTime),
              ),
              if (appointment.note?.trim().isNotEmpty == true)
                _InfoRow(icon: Icons.notes, text: appointment.note!.trim()),
            ],
          ),
        ),
      ),
    ];
  }
}

class _Timeline extends StatelessWidget {
  const _Timeline({required this.currentStep, required this.isCancelled});

  final int currentStep;
  final bool isCancelled;

  static const _steps = [
    ('Đã gửi yêu cầu', 'Garage đã nhận được yêu cầu đặt lịch.', Icons.send),
    (
      'Garage xác nhận',
      'Thời gian và chi nhánh đã được xác nhận.',
      Icons.event_available,
    ),
    (
      'Xe đã tiếp nhận',
      'Nhân viên đã check-in xe tại garage.',
      Icons.car_repair,
    ),
    (
      'Hoàn tất lịch hẹn',
      'Quy trình của lịch hẹn đã hoàn tất.',
      Icons.check_circle,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        for (var index = 0; index < _steps.length; index++)
          _TimelineStep(
            title: _steps[index].$1,
            description: _steps[index].$2,
            icon: _steps[index].$3,
            isLast: index == _steps.length - 1,
            isComplete: !isCancelled && index < currentStep,
            isCurrent: !isCancelled && index == currentStep,
          ),
      ],
    );
  }
}

class _TimelineStep extends StatelessWidget {
  const _TimelineStep({
    required this.title,
    required this.description,
    required this.icon,
    required this.isLast,
    required this.isComplete,
    required this.isCurrent,
  });

  final String title;
  final String description;
  final IconData icon;
  final bool isLast;
  final bool isComplete;
  final bool isCurrent;

  @override
  Widget build(BuildContext context) {
    final isActive = isComplete || isCurrent;
    final color = isActive ? AppColors.primary : AppColors.outlineVariant;
    return IntrinsicHeight(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          SizedBox(
            width: 46,
            child: Column(
              children: [
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: isActive
                        ? AppColors.primary
                        : AppColors.surfaceContainer,
                    shape: BoxShape.circle,
                    border: Border.all(color: color),
                  ),
                  child: Icon(
                    isComplete ? Icons.check : icon,
                    size: 20,
                    color: isActive ? Colors.white : AppColors.onSurfaceVariant,
                  ),
                ),
                if (!isLast) Expanded(child: Container(width: 2, color: color)),
              ],
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.only(bottom: 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          title,
                          style: Theme.of(context).textTheme.titleLarge
                              ?.copyWith(
                                color: isActive
                                    ? AppColors.primary
                                    : AppColors.onSurfaceVariant,
                              ),
                        ),
                      ),
                      if (isCurrent)
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 3,
                          ),
                          decoration: BoxDecoration(
                            color: AppColors.primary,
                            borderRadius: BorderRadius.circular(999),
                          ),
                          child: const Text(
                            'HIỆN TẠI',
                            style: TextStyle(
                              color: Colors.white,
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(description),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  const _InfoRow({required this.icon, required this.text});

  final IconData icon;
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 20, color: AppColors.onSurfaceVariant),
          const SizedBox(width: 10),
          Expanded(child: Text(text)),
        ],
      ),
    );
  }
}

class _DetailMessage extends StatelessWidget {
  const _DetailMessage({
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

(String, Color) _statusPresentation(String status) {
  return switch (status) {
    'CHO_XAC_NHAN' => ('Chờ xác nhận', AppColors.warning),
    'DA_XAC_NHAN' => ('Đã xác nhận', AppColors.primary),
    'DA_TIEP_NHAN' => ('Đã tiếp nhận', AppColors.primary),
    'HOAN_TAT' => ('Hoàn tất', AppColors.success),
    'HUY' || 'DA_HUY' => ('Đã hủy', AppColors.danger),
    'KHONG_DEN' => ('Không đến', AppColors.danger),
    _ => (status.replaceAll('_', ' '), AppColors.onSurfaceVariant),
  };
}

String _formatDateTime(DateTime value) {
  String twoDigits(int number) => number.toString().padLeft(2, '0');
  return '${twoDigits(value.hour)}:${twoDigits(value.minute)} · '
      '${twoDigits(value.day)}/${twoDigits(value.month)}/${value.year}';
}
