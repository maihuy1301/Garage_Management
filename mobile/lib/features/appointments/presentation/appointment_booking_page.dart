import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../../../core/theme/app_colors.dart';
import '../data/appointment_service.dart';
import '../domain/appointment_models.dart';
import '../../vehicles/data/vehicle_service.dart';
import '../../vehicles/presentation/vehicle_form_sheet.dart';

class AppointmentBookingPage extends StatefulWidget {
  const AppointmentBookingPage({
    super.key,
    this.gateway,
    this.vehicleGateway,
    this.initialVehicleId,
  });

  final AppointmentGateway? gateway;
  final VehicleGateway? vehicleGateway;
  final int? initialVehicleId;

  @override
  State<AppointmentBookingPage> createState() => _AppointmentBookingPageState();
}

class _AppointmentBookingPageState extends State<AppointmentBookingPage> {
  final _formKey = GlobalKey<FormState>();
  final _noteController = TextEditingController();
  final _dateController = TextEditingController();
  final _timeController = TextEditingController();

  late AppointmentGateway _gateway;
  bool _initialized = false;
  bool _isLoading = true;
  bool _isLoadingAppointments = true;
  bool _isSubmitting = false;
  String? _loadError;
  String? _appointmentsError;
  String? _submitError;
  AppointmentBookingOptions? _options;
  List<AppointmentBookingResult> _appointments = const [];
  AppointmentBookingResult? _result;
  final Set<int> _cancellingIds = {};
  int _selectedView = 0;
  int? _vehicleId;
  int? _branchId;
  DateTime? _appointmentTime;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_initialized) return;
    _gateway = widget.gateway ?? context.read<AppointmentGateway>();
    _initialized = true;
    _loadOptions();
    _loadAppointments();
  }

  @override
  void dispose() {
    _noteController.dispose();
    _dateController.dispose();
    _timeController.dispose();
    super.dispose();
  }

  Future<void> _loadOptions({int? preferredVehicleId}) async {
    setState(() {
      _isLoading = true;
      _loadError = null;
    });
    try {
      final options = await _gateway.loadBookingOptions();
      if (!mounted) return;
      setState(() {
        _options = options;
        final currentVehicleIsAvailable = options.vehicles.any(
          (vehicle) => vehicle.id == _vehicleId,
        );
        final requestedVehicleId =
            preferredVehicleId ?? widget.initialVehicleId;
        final preferredVehicleIsAvailable = options.vehicles.any(
          (vehicle) => vehicle.id == requestedVehicleId,
        );
        _vehicleId = preferredVehicleIsAvailable
            ? requestedVehicleId
            : currentVehicleIsAvailable
            ? _vehicleId
            : options.vehicles.length == 1
            ? options.vehicles.first.id
            : null;
        _branchId = options.branches.length == 1
            ? options.branches.first.id
            : null;
      });
    } on AppointmentException catch (error) {
      if (mounted) setState(() => _loadError = error.message);
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _addVehicle() async {
    final vehicleGateway =
        widget.vehicleGateway ?? context.read<VehicleGateway>();
    final created = await showVehicleFormSheet(context, vehicleGateway);
    if (created == null || !mounted) return;
    await _loadOptions(preferredVehicleId: created.id);
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Đã thêm xe. Bạn có thể tiếp tục đặt lịch.'),
      ),
    );
  }

  Future<void> _loadAppointments() async {
    setState(() {
      _isLoadingAppointments = true;
      _appointmentsError = null;
    });
    try {
      final appointments = await _gateway.loadMyAppointments();
      if (!mounted) return;
      setState(() => _appointments = appointments);
    } on AppointmentException catch (error) {
      if (mounted) setState(() => _appointmentsError = error.message);
    } finally {
      if (mounted) setState(() => _isLoadingAppointments = false);
    }
  }

  Future<void> _pickDate() async {
    final now = DateTime.now();
    final initial = _appointmentTime != null && _appointmentTime!.isAfter(now)
        ? _appointmentTime!
        : now.add(const Duration(days: 1));
    final date = await showDatePicker(
      context: context,
      initialDate: DateTime(initial.year, initial.month, initial.day),
      firstDate: DateTime(now.year, now.month, now.day),
      lastDate: DateTime(now.year + 1, now.month, now.day),
      helpText: 'Chọn ngày hẹn',
    );
    if (date == null || !mounted) return;
    final currentTime = _appointmentTime == null
        ? const TimeOfDay(hour: 8, minute: 0)
        : TimeOfDay.fromDateTime(_appointmentTime!);
    _setAppointmentTime(date, currentTime);
  }

  Future<void> _pickTime() async {
    final now = DateTime.now();
    final initial = _appointmentTime == null
        ? const TimeOfDay(hour: 8, minute: 0)
        : TimeOfDay.fromDateTime(_appointmentTime!);
    final time = await showTimePicker(
      context: context,
      initialTime: initial,
      helpText: 'Chọn giờ hẹn',
    );
    if (time == null || !mounted) return;
    final date = _appointmentTime ?? now.add(const Duration(days: 1));
    _setAppointmentTime(date, time);
  }

  void _setAppointmentTime(DateTime date, TimeOfDay time) {
    final value = DateTime(
      date.year,
      date.month,
      date.day,
      time.hour,
      time.minute,
    );
    setState(() {
      _appointmentTime = value;
      _dateController.text = _formatDate(value);
      _timeController.text = _formatTime(value);
      _submitError = null;
    });
    _formKey.currentState?.validate();
  }

  Future<void> _submit() async {
    FocusScope.of(context).unfocus();
    setState(() {
      _submitError = null;
      _result = null;
    });
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSubmitting = true);
    try {
      final result = await _gateway.createAppointment(
        vehicleId: _vehicleId!,
        branchId: _branchId!,
        appointmentTime: _appointmentTime!,
        note: _noteController.text,
      );
      if (!mounted) return;
      setState(() {
        _result = result;
        _appointments = [
          result,
          ..._appointments.where((appointment) => appointment.id != result.id),
        ];
      });
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Đặt lịch hẹn thành công.')));
    } on AppointmentException catch (error) {
      if (mounted) setState(() => _submitError = error.message);
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  Future<void> _cancelAppointment(AppointmentBookingResult appointment) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Hủy lịch hẹn?'),
        content: Text(
          'Bạn có muốn hủy lịch #${appointment.id} cho xe ${appointment.vehicleLicensePlate} không?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext, false),
            child: const Text('Giữ lịch'),
          ),
          FilledButton(
            key: const ValueKey('confirm-cancel-appointment'),
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Hủy lịch'),
          ),
        ],
      ),
    );
    if (confirmed != true || !mounted) return;

    setState(() => _cancellingIds.add(appointment.id));
    try {
      final cancelled = await _gateway.cancelAppointment(appointment.id);
      if (!mounted) return;
      setState(() {
        _appointments = [
          for (final current in _appointments)
            if (current.id == cancelled.id) cancelled else current,
        ];
      });
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Đã hủy lịch hẹn.')));
    } on AppointmentException catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(error.message),
          backgroundColor: AppColors.danger,
        ),
      );
    } finally {
      if (mounted) setState(() => _cancellingIds.remove(appointment.id));
    }
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: RefreshIndicator(
        onRefresh: () => _selectedView == 0
            ? Future.wait([_loadOptions(), _loadAppointments()])
            : _loadAppointments(),
        child: ListView(
          key: const ValueKey('appointment-booking-page'),
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 32),
          children: [
            _AppointmentHeader(
              appointmentCount: _appointments.length,
              onAddVehicle: _addVehicle,
            ),
            const SizedBox(height: 16),
            SegmentedButton<int>(
              key: const ValueKey('appointment-view-selector'),
              segments: const [
                ButtonSegment(
                  value: 0,
                  icon: Icon(Icons.add_circle_outline_rounded),
                  label: Text('Đặt lịch'),
                ),
                ButtonSegment(
                  value: 1,
                  icon: Icon(Icons.event_note_rounded),
                  label: Text('Lịch của tôi'),
                ),
              ],
              selected: {_selectedView},
              onSelectionChanged: (selection) {
                setState(() => _selectedView = selection.first);
              },
            ),
            const SizedBox(height: 20),
            if (_selectedView == 0) ..._buildBookingContent(context),
            if (_selectedView == 1) ..._buildAppointmentsContent(context),
          ],
        ),
      ),
    );
  }

  List<Widget> _buildBookingContent(BuildContext context) {
    if (_isLoading) return const [_LoadingCard()];
    if (_loadError != null) {
      return [
        _MessageCard(
          icon: Icons.cloud_off_rounded,
          title: 'Chưa tải được dữ liệu',
          message: _loadError!,
          actionLabel: 'Thử lại',
          onAction: _loadOptions,
        ),
      ];
    }
    if (_options!.vehicles.isEmpty) {
      return [_NoVehicleCard(onAdd: _addVehicle)];
    }
    if (_options!.branches.isEmpty) {
      return const [
        _MessageCard(
          icon: Icons.store_mall_directory_outlined,
          title: 'Chưa có chi nhánh hoạt động',
          message: 'Vui lòng quay lại sau hoặc liên hệ garage để được hỗ trợ.',
        ),
      ];
    }
    return [_buildForm(context)];
  }

  List<Widget> _buildAppointmentsContent(BuildContext context) {
    if (_isLoadingAppointments) return const [_LoadingCard()];
    if (_appointmentsError != null) {
      return [
        _MessageCard(
          icon: Icons.cloud_off_rounded,
          title: 'Chưa tải được lịch hẹn',
          message: _appointmentsError!,
          actionLabel: 'Thử lại',
          onAction: _loadAppointments,
        ),
      ];
    }
    if (_appointments.isEmpty) {
      return const [
        _MessageCard(
          icon: Icons.event_busy_rounded,
          title: 'Chưa có lịch hẹn',
          message: 'Các lịch bạn tạo sẽ xuất hiện tại đây.',
        ),
      ];
    }
    return [
      for (final appointment in _appointments) ...[
        _AppointmentCard(
          appointment: appointment,
          isCancelling: _cancellingIds.contains(appointment.id),
          onCancel: () => _cancelAppointment(appointment),
          onDetails: () => context.push('/tracking/${appointment.id}'),
          formatDateTime: _formatDateTime,
        ),
        const SizedBox(height: 12),
      ],
    ];
  }

  Widget _buildForm(BuildContext context) {
    final options = _options!;
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _SectionTitle(
                    icon: Icons.directions_car_filled_rounded,
                    title: 'Xe và chi nhánh',
                  ),
                  const SizedBox(height: 16),
                  DropdownButtonFormField<int>(
                    key: const ValueKey('appointment-vehicle-field'),
                    initialValue: _vehicleId,
                    isExpanded: true,
                    decoration: const InputDecoration(labelText: 'Xe của bạn'),
                    items: [
                      for (final vehicle in options.vehicles)
                        DropdownMenuItem(
                          value: vehicle.id,
                          child: Text(
                            vehicle.description.isEmpty
                                ? vehicle.licensePlate
                                : '${vehicle.licensePlate} · ${vehicle.description}',
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                    ],
                    onChanged: _isSubmitting
                        ? null
                        : (value) => setState(() => _vehicleId = value),
                    validator: (value) => value == null
                        ? 'Vui lòng chọn xe cần bảo dưỡng hoặc sửa chữa.'
                        : null,
                  ),
                  const SizedBox(height: 14),
                  DropdownButtonFormField<int>(
                    key: const ValueKey('appointment-branch-field'),
                    initialValue: _branchId,
                    isExpanded: true,
                    decoration: const InputDecoration(labelText: 'Chi nhánh'),
                    items: [
                      for (final branch in options.branches)
                        DropdownMenuItem(
                          value: branch.id,
                          child: Text(
                            branch.name,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                    ],
                    onChanged: _isSubmitting
                        ? null
                        : (value) => setState(() => _branchId = value),
                    validator: (value) => value == null
                        ? 'Vui lòng chọn chi nhánh tiếp nhận.'
                        : null,
                  ),
                  if (_branchId != null) ...[
                    const SizedBox(height: 8),
                    Text(
                      options.branches
                          .firstWhere((branch) => branch.id == _branchId)
                          .address,
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                ],
              ),
            ),
          ),
          const SizedBox(height: 14),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const _SectionTitle(
                    icon: Icons.schedule_rounded,
                    title: 'Thời gian hẹn',
                  ),
                  const SizedBox(height: 16),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Expanded(
                        flex: 3,
                        child: TextFormField(
                          key: const ValueKey('appointment-date-field'),
                          controller: _dateController,
                          readOnly: true,
                          onTap: _isSubmitting ? null : _pickDate,
                          decoration: const InputDecoration(
                            labelText: 'Ngày',
                            suffixIcon: Icon(Icons.calendar_today_outlined),
                          ),
                          validator: (_) => _validateAppointmentTime(),
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        flex: 2,
                        child: TextFormField(
                          key: const ValueKey('appointment-time-field'),
                          controller: _timeController,
                          readOnly: true,
                          onTap: _isSubmitting ? null : _pickTime,
                          decoration: const InputDecoration(
                            labelText: 'Giờ',
                            suffixIcon: Icon(Icons.access_time_rounded),
                          ),
                          validator: (_) =>
                              _appointmentTime == null ? 'Chọn giờ.' : null,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),
                  TextFormField(
                    key: const ValueKey('appointment-note-field'),
                    controller: _noteController,
                    enabled: !_isSubmitting,
                    minLines: 3,
                    maxLines: 5,
                    maxLength: 500,
                    textCapitalization: TextCapitalization.sentences,
                    decoration: const InputDecoration(
                      labelText: 'Ghi chú (không bắt buộc)',
                      alignLabelWithHint: true,
                      hintText: 'Mô tả nhu cầu bảo dưỡng hoặc tình trạng xe...',
                    ),
                  ),
                ],
              ),
            ),
          ),
          if (_submitError != null) ...[
            const SizedBox(height: 14),
            _InlineStatus(
              icon: Icons.error_outline_rounded,
              color: AppColors.danger,
              message: _submitError!,
            ),
          ],
          if (_result != null) ...[
            const SizedBox(height: 14),
            _InlineStatus(
              key: const ValueKey('appointment-success'),
              icon: Icons.check_circle_outline_rounded,
              color: AppColors.success,
              message:
                  'Đã tạo lịch #${_result!.id} cho xe ${_result!.vehicleLicensePlate} tại ${_result!.branchName}. Trạng thái: Chờ xác nhận.',
            ),
          ],
          const SizedBox(height: 18),
          FilledButton.icon(
            key: const ValueKey('appointment-submit-button'),
            onPressed: _isSubmitting ? null : _submit,
            style: FilledButton.styleFrom(
              backgroundColor: AppColors.secondaryContainer,
              foregroundColor: Colors.white,
              minimumSize: const Size.fromHeight(56),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(14),
              ),
            ),
            icon: _isSubmitting
                ? const SizedBox.square(
                    dimension: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.event_available_rounded),
            label: Text(_isSubmitting ? 'Đang gửi...' : 'Xác nhận đặt lịch'),
          ),
          const SizedBox(height: 10),
          Text(
            'Xe thuộc tài khoản và quyền tạo lịch sẽ được máy chủ kiểm tra trước khi ghi nhận.',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
      ),
    );
  }

  String? _validateAppointmentTime() {
    final value = _appointmentTime;
    if (value == null) return 'Vui lòng chọn ngày.';
    if (!value.isAfter(DateTime.now())) {
      return 'Thời gian hẹn phải ở tương lai.';
    }
    return null;
  }

  String _formatDate(DateTime value) =>
      '${value.day.toString().padLeft(2, '0')}/${value.month.toString().padLeft(2, '0')}/${value.year}';

  String _formatTime(DateTime value) =>
      '${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';

  String _formatDateTime(DateTime value) =>
      '${_formatTime(value)} · ${_formatDate(value)}';
}

class _AppointmentHeader extends StatelessWidget {
  const _AppointmentHeader({
    required this.appointmentCount,
    required this.onAddVehicle,
  });

  final int appointmentCount;
  final VoidCallback onAddVehicle;

  @override
  Widget build(BuildContext context) {
    return Container(
      clipBehavior: Clip.antiAlias,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(18),
        boxShadow: [
          BoxShadow(
            color: AppColors.primary.withValues(alpha: 0.18),
            blurRadius: 14,
            offset: const Offset(0, 7),
          ),
        ],
      ),
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            color: AppColors.primaryContainer,
            child: Row(
              children: [
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'AUTOCARE SYSTEM',
                        style: TextStyle(
                          color: Color(0xFFBFDBFE),
                          fontSize: 9,
                          fontWeight: FontWeight.w700,
                          letterSpacing: 1.1,
                        ),
                      ),
                      Text(
                        'Lịch hẹn dịch vụ',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 19,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  key: const ValueKey('appointment-header-add-vehicle-button'),
                  onPressed: onAddVehicle,
                  color: Colors.white,
                  style: IconButton.styleFrom(
                    backgroundColor: Colors.white.withValues(alpha: 0.13),
                    minimumSize: const Size.square(48),
                  ),
                  icon: const Icon(Icons.add_rounded, size: 28),
                  tooltip: 'Thêm xe',
                ),
              ],
            ),
          ),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(16),
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFF1E40AF), Color(0xFF2563EB)],
              ),
            ),
            child: Stack(
              children: [
                const Positioned(
                  right: -4,
                  bottom: -20,
                  child: Icon(
                    Icons.calendar_month_rounded,
                    color: Color(0x24FFFFFF),
                    size: 100,
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Chăm sóc xe đúng hẹn',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 20,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const SizedBox(height: 4),
                    const Text(
                      'Chọn xe · Chọn chi nhánh · Chọn thời gian',
                      style: TextStyle(color: Color(0xFFDCE8FF), fontSize: 12),
                    ),
                    const SizedBox(height: 14),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 10,
                        vertical: 6,
                      ),
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.13),
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(
                        '$appointmentCount lịch hẹn trong tài khoản',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 11,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _AppointmentCard extends StatelessWidget {
  const _AppointmentCard({
    required this.appointment,
    required this.isCancelling,
    required this.onCancel,
    required this.onDetails,
    required this.formatDateTime,
  });

  final AppointmentBookingResult appointment;
  final bool isCancelling;
  final VoidCallback onCancel;
  final VoidCallback onDetails;
  final String Function(DateTime) formatDateTime;

  @override
  Widget build(BuildContext context) {
    final status = _statusPresentation(appointment.status);
    return Card(
      key: ValueKey('appointment-card-${appointment.id}'),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    appointment.vehicleLicensePlate,
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ),
                _StatusBadge(label: status.$1, color: status.$2),
              ],
            ),
            if (appointment.vehicleDescription.isNotEmpty) ...[
              const SizedBox(height: 4),
              Text(appointment.vehicleDescription),
            ],
            const SizedBox(height: 14),
            _AppointmentDetail(
              icon: Icons.store_mall_directory_outlined,
              text: appointment.branchName,
            ),
            const SizedBox(height: 8),
            _AppointmentDetail(
              icon: Icons.schedule_rounded,
              text: formatDateTime(appointment.appointmentTime),
            ),
            if (appointment.note?.trim().isNotEmpty == true) ...[
              const SizedBox(height: 8),
              _AppointmentDetail(
                icon: Icons.notes_rounded,
                text: appointment.note!.trim(),
              ),
            ],
            const SizedBox(height: 14),
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                TextButton.icon(
                  key: ValueKey('appointment-details-${appointment.id}'),
                  onPressed: onDetails,
                  icon: const Icon(Icons.arrow_forward_rounded),
                  label: const Text('Chi tiết'),
                ),
                if (appointment.canCancel) ...[
                  const SizedBox(width: 8),
                  OutlinedButton.icon(
                    key: ValueKey('cancel-appointment-${appointment.id}'),
                    onPressed: isCancelling ? null : onCancel,
                    icon: isCancelling
                        ? const SizedBox.square(
                            dimension: 18,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.cancel_outlined),
                    label: Text(isCancelling ? 'Đang hủy...' : 'Hủy lịch'),
                  ),
                ],
              ],
            ),
          ],
        ),
      ),
    );
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
}

class _AppointmentDetail extends StatelessWidget {
  const _AppointmentDetail({required this.icon, required this.text});

  final IconData icon;
  final String text;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 19, color: AppColors.onSurfaceVariant),
        const SizedBox(width: 9),
        Expanded(child: Text(text)),
      ],
    );
  }
}

class _StatusBadge extends StatelessWidget {
  const _StatusBadge({required this.label, required this.color});

  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        label,
        style: Theme.of(context).textTheme.labelMedium?.copyWith(color: color),
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.icon, required this.title});

  final IconData icon;
  final String title;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, color: AppColors.primary),
        const SizedBox(width: 10),
        Text(title, style: Theme.of(context).textTheme.titleMedium),
      ],
    );
  }
}

class _LoadingCard extends StatelessWidget {
  const _LoadingCard();

  @override
  Widget build(BuildContext context) {
    return const Card(
      child: Padding(
        padding: EdgeInsets.symmetric(vertical: 48),
        child: Center(child: CircularProgressIndicator()),
      ),
    );
  }
}

class _MessageCard extends StatelessWidget {
  const _MessageCard({
    required this.icon,
    required this.title,
    required this.message,
    this.actionLabel,
    this.onAction,
  });

  final IconData icon;
  final String title;
  final String message;
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
            if (onAction != null) ...[
              const SizedBox(height: 16),
              OutlinedButton(
                onPressed: onAction,
                child: Text(actionLabel ?? 'Thử lại'),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _NoVehicleCard extends StatelessWidget {
  const _NoVehicleCard({required this.onAdd});
  final VoidCallback onAdd;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 28),
        child: Column(
          children: [
            InkWell(
              key: const ValueKey('appointment-empty-add-vehicle-button'),
              onTap: onAdd,
              customBorder: const CircleBorder(),
              child: Container(
                width: 76,
                height: 76,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: const LinearGradient(
                    colors: [Color(0xFFF97316), Color(0xFFEA580C)],
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.primary.withValues(alpha: 0.22),
                      blurRadius: 14,
                      offset: const Offset(0, 5),
                    ),
                  ],
                ),
                child: const Icon(
                  Icons.add_rounded,
                  color: Colors.white,
                  size: 44,
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              'Bạn chưa có xe',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 6),
            const Text(
              'Nhấn dấu cộng để điền thông tin xe. Sau khi thêm, form đặt lịch sẽ hiện ngay tại đây.',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: onAdd,
              icon: const Icon(Icons.directions_car_outlined),
              label: const Text('Thêm xe để đặt lịch'),
            ),
          ],
        ),
      ),
    );
  }
}

class _InlineStatus extends StatelessWidget {
  const _InlineStatus({
    super.key,
    required this.icon,
    required this.color,
    required this.message,
  });

  final IconData icon;
  final Color color;
  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withValues(alpha: 0.35)),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: color),
          const SizedBox(width: 10),
          Expanded(child: Text(message)),
        ],
      ),
    );
  }
}
