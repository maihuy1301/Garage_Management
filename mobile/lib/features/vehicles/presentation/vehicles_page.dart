import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';

import '../../../core/theme/app_colors.dart';
import '../data/vehicle_service.dart';
import '../domain/vehicle_models.dart';
import 'vehicle_form_sheet.dart';

class VehiclesPage extends StatefulWidget {
  const VehiclesPage({super.key, this.gateway});
  final VehicleGateway? gateway;

  @override
  State<VehiclesPage> createState() => _VehiclesPageState();
}

class _VehiclesPageState extends State<VehiclesPage> {
  late VehicleGateway _gateway;
  bool _initialized = false;
  bool _isLoading = true;
  String? _error;
  List<CustomerVehicle> _vehicles = const [];

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (_initialized) return;
    _gateway = widget.gateway ?? context.read<VehicleGateway>();
    _initialized = true;
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final vehicles = await _gateway.loadVehicles();
      if (mounted) setState(() => _vehicles = vehicles);
    } on VehicleException catch (error) {
      if (mounted) setState(() => _error = error.message);
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _addVehicle() async {
    final created = await showVehicleFormSheet(context, _gateway);
    if (created == null || !mounted) return;
    setState(
      () => _vehicles = [
        created,
        ..._vehicles.where((item) => item.id != created.id),
      ],
    );
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(const SnackBar(content: Text('Đã thêm xe thành công.')));
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: RefreshIndicator(
        onRefresh: _load,
        child: ListView(
          key: const ValueKey('vehicles-page'),
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 32),
          children: [
            _VehicleHeader(
              total: _vehicles.length,
              active: _vehicles.where((vehicle) => vehicle.isActive).length,
              onAdd: _addVehicle,
              onBack: () => context.go('/account'),
              onNotifications: () => context.go('/notifications'),
            ),
            const SizedBox(height: 22),
            Row(
              children: [
                Text(
                  'Danh sách xe cá nhân',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const SizedBox(width: 7),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 8,
                    vertical: 3,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.surfaceContainer,
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Text(
                    '${_vehicles.length}',
                    style: const TextStyle(
                      color: AppColors.primary,
                      fontWeight: FontWeight.w700,
                      fontSize: 12,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            if (_isLoading)
              const Card(
                child: Padding(
                  padding: EdgeInsets.symmetric(vertical: 48),
                  child: Center(child: CircularProgressIndicator()),
                ),
              )
            else if (_error != null)
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    children: [
                      const Icon(
                        Icons.cloud_off_rounded,
                        size: 42,
                        color: AppColors.primary,
                      ),
                      const SizedBox(height: 12),
                      Text(
                        'Chưa tải được danh sách xe',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 6),
                      Text(_error!, textAlign: TextAlign.center),
                      const SizedBox(height: 16),
                      OutlinedButton(
                        onPressed: _load,
                        child: const Text('Thử lại'),
                      ),
                    ],
                  ),
                ),
              )
            else if (_vehicles.isEmpty)
              _EmptyVehiclesCard(onAdd: _addVehicle)
            else
              for (final vehicle in _vehicles) ...[
                _VehicleCard(
                  vehicle: vehicle,
                  onBook: () =>
                      context.go('/appointments?vehicleId=${vehicle.id}'),
                ),
                const SizedBox(height: 12),
              ],
          ],
        ),
      ),
    );
  }
}

class _VehicleHeader extends StatelessWidget {
  const _VehicleHeader({
    required this.total,
    required this.active,
    required this.onAdd,
    required this.onBack,
    required this.onNotifications,
  });

  final int total;
  final int active;
  final VoidCallback onAdd;
  final VoidCallback onBack;
  final VoidCallback onNotifications;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
          decoration: const BoxDecoration(
            color: AppColors.primaryContainer,
            borderRadius: BorderRadius.vertical(top: Radius.circular(18)),
          ),
          child: Row(
            children: [
              IconButton(
                onPressed: onBack,
                color: Colors.white,
                visualDensity: VisualDensity.compact,
                icon: const Icon(Icons.arrow_back_rounded, size: 21),
                tooltip: 'Quay lại',
              ),
              const SizedBox(width: 4),
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
                      'Xe của tôi',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ],
                ),
              ),
              IconButton(
                onPressed: onNotifications,
                color: Colors.white,
                icon: const Icon(Icons.notifications_none_rounded),
                tooltip: 'Thông báo',
              ),
            ],
          ),
        ),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            gradient: const LinearGradient(
              colors: [Color(0xFF1E3A8A), Color(0xFF2563EB)],
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
            ),
            borderRadius: const BorderRadius.vertical(
              bottom: Radius.circular(18),
            ),
            boxShadow: [
              BoxShadow(
                color: AppColors.primary.withValues(alpha: 0.2),
                blurRadius: 14,
                offset: const Offset(0, 7),
              ),
            ],
          ),
          child: Stack(
            children: [
              const Positioned(
                right: -4,
                bottom: -24,
                child: Icon(
                  Icons.directions_car_filled_rounded,
                  size: 110,
                  color: Color(0x1FFFFFFF),
                ),
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 9,
                      vertical: 4,
                    ),
                    decoration: BoxDecoration(
                      color: Colors.white.withValues(alpha: 0.14),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: const Text(
                      '⚙  GarageFlow Multi-Branch',
                      style: TextStyle(
                        color: Color(0xFFDCE8FF),
                        fontSize: 11,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                  const SizedBox(height: 9),
                  Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '$total Phương tiện',
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 23,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                            const Text(
                              'Quản lý và đặt lịch bảo dưỡng định kỳ',
                              style: TextStyle(
                                color: Color(0xFFDCE8FF),
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ),
                      FilledButton.icon(
                        key: const ValueKey('vehicles-header-add-button'),
                        onPressed: onAdd,
                        style: FilledButton.styleFrom(
                          backgroundColor: AppColors.secondaryContainer,
                          foregroundColor: Colors.white,
                          minimumSize: const Size(48, 42),
                          padding: const EdgeInsets.symmetric(horizontal: 12),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                        ),
                        icon: const Icon(
                          Icons.add_circle_outline_rounded,
                          size: 18,
                        ),
                        label: const Text('Thêm xe'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),
                  Container(
                    padding: const EdgeInsets.only(top: 12),
                    decoration: const BoxDecoration(
                      border: Border(top: BorderSide(color: Color(0x33FFFFFF))),
                    ),
                    child: Row(
                      children: [
                        const Icon(
                          Icons.verified_rounded,
                          color: Color(0xFFFCD34D),
                          size: 19,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          '$active xe đang hoạt động',
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                        const Spacer(),
                        const Text(
                          'Dữ liệu hệ thống',
                          style: TextStyle(
                            color: Color(0xFFDCE8FF),
                            fontSize: 11,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _EmptyVehiclesCard extends StatelessWidget {
  const _EmptyVehiclesCard({required this.onAdd});
  final VoidCallback onAdd;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 30),
        child: Column(
          children: [
            InkWell(
              key: const ValueKey('vehicles-empty-add-button'),
              onTap: onAdd,
              customBorder: const CircleBorder(),
              child: Container(
                width: 72,
                height: 72,
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
                  size: 42,
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
              'Nhấn dấu cộng để thêm thông tin xe đầu tiên.',
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}

class _VehicleCard extends StatelessWidget {
  const _VehicleCard({required this.vehicle, required this.onBook});
  final CustomerVehicle vehicle;
  final VoidCallback onBook;

  @override
  Widget build(BuildContext context) {
    final title = vehicle.description.isEmpty
        ? 'Phương tiện của bạn'
        : vehicle.description;
    return Card(
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Container(
            height: 128,
            padding: const EdgeInsets.all(14),
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFFE8EEFF), Color(0xFFD8E6FF)],
              ),
            ),
            child: Stack(
              children: [
                const Align(
                  alignment: Alignment.center,
                  child: Icon(
                    Icons.directions_car_filled_rounded,
                    size: 92,
                    color: Color(0xFF7892D0),
                  ),
                ),
                Align(
                  alignment: Alignment.topRight,
                  child: Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 9,
                      vertical: 5,
                    ),
                    decoration: BoxDecoration(
                      color: vehicle.isActive
                          ? AppColors.success
                          : AppColors.warning,
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: Text(
                      vehicle.isActive ? '●  Đang hoạt động' : 'Tạm ngưng',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 11,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                ),
                if (vehicle.year != null || vehicle.color.trim().isNotEmpty)
                  Align(
                    alignment: Alignment.bottomLeft,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 9,
                        vertical: 5,
                      ),
                      decoration: BoxDecoration(
                        color: const Color(0xB3000000),
                        borderRadius: BorderRadius.circular(7),
                      ),
                      child: Text(
                        [
                          if (vehicle.color.trim().isNotEmpty) vehicle.color,
                          if (vehicle.year != null) 'Đời ${vehicle.year}',
                        ].join(' • '),
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 11,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(14),
            child: Column(
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: Text(
                        title,
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 10,
                        vertical: 6,
                      ),
                      decoration: BoxDecoration(
                        color: const Color(0xFFF1F5F9),
                        borderRadius: BorderRadius.circular(9),
                        border: Border.all(color: const Color(0xFFCBD5E1)),
                      ),
                      child: Text(
                        vehicle.licensePlate,
                        style: const TextStyle(
                          fontFamily: 'monospace',
                          fontWeight: FontWeight.w800,
                          fontSize: 12,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.all(11),
                  decoration: BoxDecoration(
                    color: const Color(0xFFF8FAFC),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: const Color(0xFFE2E8F0)),
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        child: _VehicleSpec(
                          icon: Icons.speed_rounded,
                          label: 'ODO hiện tại',
                          value: vehicle.odometer == null
                              ? 'Chưa cập nhật'
                              : '${vehicle.odometer} km',
                        ),
                      ),
                      Container(
                        width: 1,
                        height: 38,
                        color: const Color(0xFFE2E8F0),
                      ),
                      Expanded(
                        child: _VehicleSpec(
                          icon: Icons.fingerprint_rounded,
                          label: 'Số VIN',
                          value: vehicle.vin.trim().isEmpty
                              ? 'Chưa cập nhật'
                              : vehicle.vin,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 13),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton.icon(
                    onPressed: vehicle.isActive ? onBook : null,
                    style: FilledButton.styleFrom(
                      backgroundColor: AppColors.primaryContainer,
                      foregroundColor: Colors.white,
                    ),
                    icon: const Icon(Icons.calendar_month_rounded, size: 19),
                    label: const Text('Đặt lịch ngay'),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _VehicleSpec extends StatelessWidget {
  const _VehicleSpec({
    required this.icon,
    required this.label,
    required this.value,
  });
  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 7),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, size: 14, color: AppColors.onSurfaceVariant),
              const SizedBox(width: 5),
              Expanded(
                child: Text(
                  label,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: 10,
                    color: AppColors.onSurfaceVariant,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 3),
          Text(
            value,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w800),
          ),
        ],
      ),
    );
  }
}
