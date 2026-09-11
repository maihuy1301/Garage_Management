import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';
import '../data/vehicle_service.dart';
import '../domain/vehicle_models.dart';

Future<CustomerVehicle?> showVehicleFormSheet(
  BuildContext context,
  VehicleGateway gateway,
) {
  return showModalBottomSheet<CustomerVehicle>(
    context: context,
    isScrollControlled: true,
    useSafeArea: true,
    showDragHandle: true,
    builder: (_) => _VehicleFormSheet(gateway: gateway),
  );
}

class _VehicleFormSheet extends StatefulWidget {
  const _VehicleFormSheet({required this.gateway});
  final VehicleGateway gateway;

  @override
  State<_VehicleFormSheet> createState() => _VehicleFormSheetState();
}

class _VehicleFormSheetState extends State<_VehicleFormSheet> {
  final _formKey = GlobalKey<FormState>();
  final _plate = TextEditingController();
  final _year = TextEditingController();
  final _color = TextEditingController();
  final _odometer = TextEditingController();
  final _vin = TextEditingController();
  List<VehicleBrand> _brands = const [];
  List<VehicleModel> _models = const [];
  int? _selectedBrandId;
  int? _selectedModelId;
  bool _isLoadingBrands = true;
  bool _isLoadingModels = false;
  bool _isSubmitting = false;
  String? _catalogError;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadBrands();
  }

  @override
  void dispose() {
    for (final controller in [_plate, _year, _color, _odometer, _vin]) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _loadBrands() async {
    setState(() {
      _isLoadingBrands = true;
      _catalogError = null;
      _brands = const [];
      _models = const [];
      _selectedBrandId = null;
      _selectedModelId = null;
    });
    try {
      final brands = await widget.gateway.loadBrands();
      if (!mounted) return;
      setState(() {
        _brands = brands;
        if (brands.isEmpty) {
          _catalogError = 'Garage chưa cấu hình hãng xe đang hoạt động.';
        }
      });
    } on VehicleException catch (error) {
      if (mounted) setState(() => _catalogError = error.message);
    } finally {
      if (mounted) setState(() => _isLoadingBrands = false);
    }
  }

  Future<void> _selectBrand(int? brandId) async {
    setState(() {
      _selectedBrandId = brandId;
      _selectedModelId = null;
      _models = const [];
      _catalogError = null;
      _isLoadingModels = brandId != null;
    });
    if (brandId == null) return;

    try {
      final models = await widget.gateway.loadModels(brandId);
      if (!mounted || _selectedBrandId != brandId) return;
      setState(() {
        _models = models;
        if (models.isEmpty) {
          _catalogError = 'Hãng xe đã chọn chưa có model đang hoạt động.';
        }
      });
    } on VehicleException catch (error) {
      if (mounted && _selectedBrandId == brandId) {
        setState(() => _catalogError = error.message);
      }
    } finally {
      if (mounted && _selectedBrandId == brandId) {
        setState(() => _isLoadingModels = false);
      }
    }
  }

  Future<void> _retryCatalog() async {
    final brandId = _selectedBrandId;
    if (brandId == null) {
      await _loadBrands();
    } else {
      await _selectBrand(brandId);
    }
  }

  Future<void> _submit() async {
    FocusScope.of(context).unfocus();
    setState(() => _error = null);
    if (!_formKey.currentState!.validate()) return;
    setState(() => _isSubmitting = true);
    try {
      final vehicle = await widget.gateway.createVehicle(
        CreateCustomerVehicle(
          licensePlate: _plate.text,
          brandId: _selectedBrandId!,
          modelId: _selectedModelId!,
          year: int.tryParse(_year.text),
          color: _color.text,
          odometer: int.tryParse(_odometer.text),
          vin: _vin.text,
        ),
      );
      if (mounted) Navigator.pop(context, vehicle);
    } on VehicleException catch (error) {
      if (mounted) setState(() => _error = error.message);
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }

  String? _maxLength(String? value, int max, String label) {
    return (value?.trim().length ?? 0) > max
        ? '$label không được vượt quá $max ký tự.'
        : null;
  }

  Future<void> _showImageContractNotice() {
    return showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        icon: const Icon(
          Icons.add_a_photo_outlined,
          color: AppColors.primaryContainer,
        ),
        title: const Text('Ảnh xe chưa thể tải lên'),
        content: const Text(
          'API xe hiện tại chưa hỗ trợ lưu ảnh hoặc giấy đăng kiểm. '
          'Bạn vẫn có thể thêm xe bằng các thông tin trong form; '
          'tính năng ảnh sẽ được mở khi backend có endpoint phù hợp.',
        ),
        actions: [
          FilledButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Đã hiểu'),
          ),
        ],
      ),
    );
  }

  InputDecoration _fieldDecoration({String? hintText, String? suffixText}) {
    return InputDecoration(
      hintText: hintText,
      suffixText: suffixText,
      filled: true,
      fillColor: const Color(0xFFFAFCFF),
      isDense: true,
      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 14),
    );
  }

  @override
  Widget build(BuildContext context) {
    final currentYear = DateTime.now().year;
    return Padding(
      padding: EdgeInsets.fromLTRB(
        20,
        0,
        20,
        MediaQuery.viewInsetsOf(context).bottom + 20,
      ),
      child: SingleChildScrollView(
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Container(
                padding: const EdgeInsets.only(bottom: 14),
                decoration: const BoxDecoration(
                  border: Border(bottom: BorderSide(color: Color(0xFFE2E8F0))),
                ),
                child: Row(
                  children: [
                    Container(
                      width: 38,
                      height: 38,
                      decoration: BoxDecoration(
                        color: const Color(0xFFEEF2FF),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: const Icon(
                        Icons.directions_car_outlined,
                        color: AppColors.primaryContainer,
                        size: 21,
                      ),
                    ),
                    const SizedBox(width: 12),
                    const Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Đăng ký xe mới',
                            style: TextStyle(
                              color: Color(0xFF0F172A),
                              fontSize: 16,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          Text(
                            'Thêm phương tiện để tự động nhận lịch định kỳ',
                            style: TextStyle(
                              color: Color(0xFF64748B),
                              fontSize: 11,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 9,
                        vertical: 6,
                      ),
                      decoration: BoxDecoration(
                        color: const Color(0xFFEEF4FF),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: const Text(
                        'Bảo mật xe',
                        style: TextStyle(
                          color: AppColors.primaryContainer,
                          fontSize: 10,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16),
              if (_catalogError != null) ...[
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.warning.withValues(alpha: 0.08),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: AppColors.warning.withValues(alpha: 0.35),
                    ),
                  ),
                  child: Row(
                    children: [
                      const Icon(
                        Icons.info_outline_rounded,
                        color: AppColors.warning,
                      ),
                      const SizedBox(width: 8),
                      Expanded(child: Text(_catalogError!)),
                      TextButton(
                        key: const ValueKey('vehicle-catalog-retry-button'),
                        onPressed: _isLoadingBrands || _isLoadingModels
                            ? null
                            : _retryCatalog,
                        child: const Text('Thử lại'),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 12),
              ],
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: _VehicleFormField(
                      label: 'Biển số xe',
                      required: true,
                      child: TextFormField(
                        key: const ValueKey('vehicle-license-plate-field'),
                        controller: _plate,
                        enabled: !_isSubmitting,
                        textCapitalization: TextCapitalization.characters,
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          letterSpacing: 1,
                        ),
                        decoration: _fieldDecoration(
                          hintText: 'VD: 30K-999.99',
                        ),
                        validator: (value) {
                          final normalized = value?.trim() ?? '';
                          if (normalized.isEmpty) {
                            return 'Vui lòng nhập biển số.';
                          }
                          return normalized.length > 20
                              ? 'Tối đa 20 ký tự.'
                              : null;
                        },
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _VehicleFormField(
                      label: 'Hãng xe',
                      required: true,
                      child: DropdownButtonFormField<int>(
                        key: const ValueKey('vehicle-brand-field'),
                        initialValue: _selectedBrandId,
                        isExpanded: true,
                        decoration: _fieldDecoration(
                          hintText: _isLoadingBrands
                              ? 'Đang tải hãng xe...'
                              : 'Chọn hãng xe',
                        ),
                        items: _brands
                            .map(
                              (brand) => DropdownMenuItem<int>(
                                value: brand.id,
                                child: Text(
                                  brand.name,
                                  overflow: TextOverflow.ellipsis,
                                ),
                              ),
                            )
                            .toList(growable: false),
                        onChanged:
                            _isSubmitting || _isLoadingBrands || _brands.isEmpty
                            ? null
                            : _selectBrand,
                        validator: (value) =>
                            value == null ? 'Vui lòng chọn hãng xe.' : null,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    flex: 3,
                    child: _VehicleFormField(
                      label: 'Dòng xe / Model',
                      required: true,
                      child: SizedBox(
                        key: const ValueKey('vehicle-model-field'),
                        child: DropdownButtonFormField<int>(
                          key: ValueKey(
                            'vehicle-model-dropdown-${_selectedBrandId ?? 0}',
                          ),
                          initialValue: _selectedModelId,
                          isExpanded: true,
                          decoration: _fieldDecoration(
                            hintText: _selectedBrandId == null
                                ? 'Chọn hãng trước'
                                : _isLoadingModels
                                ? 'Đang tải model...'
                                : 'Chọn model xe',
                          ),
                          items: _models
                              .map(
                                (model) => DropdownMenuItem<int>(
                                  value: model.id,
                                  child: Text(
                                    model.name,
                                    overflow: TextOverflow.ellipsis,
                                  ),
                                ),
                              )
                              .toList(growable: false),
                          onChanged:
                              _isSubmitting ||
                                  _isLoadingModels ||
                                  _models.isEmpty
                              ? null
                              : (value) =>
                                    setState(() => _selectedModelId = value),
                          validator: (value) =>
                              value == null ? 'Vui lòng chọn model xe.' : null,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    flex: 2,
                    child: _VehicleFormField(
                      label: 'Năm SX',
                      child: TextFormField(
                        controller: _year,
                        enabled: !_isSubmitting,
                        keyboardType: TextInputType.number,
                        decoration: _fieldDecoration(
                          hintText: currentYear.toString(),
                        ),
                        validator: (value) {
                          if (value?.trim().isEmpty ?? true) return null;
                          final year = int.tryParse(value!.trim());
                          return year == null ||
                                  year < 1950 ||
                                  year > currentYear + 1
                              ? 'Năm không hợp lệ.'
                              : null;
                        },
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: _VehicleFormField(
                      label: 'Số ODO hiện tại',
                      child: TextFormField(
                        controller: _odometer,
                        enabled: !_isSubmitting,
                        keyboardType: TextInputType.number,
                        decoration: _fieldDecoration(
                          hintText: '12,500',
                          suffixText: 'km',
                        ),
                        validator: (value) {
                          if (value?.trim().isEmpty ?? true) return null;
                          final number = int.tryParse(value!.trim());
                          return number == null || number < 0
                              ? 'Nhập số không âm.'
                              : null;
                        },
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _VehicleFormField(
                      label: 'Số khung / VIN',
                      optional: true,
                      child: TextFormField(
                        controller: _vin,
                        enabled: !_isSubmitting,
                        textCapitalization: TextCapitalization.characters,
                        style: const TextStyle(letterSpacing: 0.5),
                        decoration: _fieldDecoration(hintText: '17 ký tự VIN'),
                        validator: (value) => _maxLength(value, 50, 'Số VIN'),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              _VehicleFormField(
                label: 'Màu xe',
                optional: true,
                child: TextFormField(
                  controller: _color,
                  enabled: !_isSubmitting,
                  textCapitalization: TextCapitalization.words,
                  decoration: _fieldDecoration(hintText: 'VD: Trắng'),
                  validator: (value) => _maxLength(value, 50, 'Màu xe'),
                ),
              ),
              const SizedBox(height: 14),
              _VehicleFormField(
                label: 'Ảnh xe hoặc Giấy đăng kiểm',
                optional: true,
                child: _VehicleImagePlaceholder(
                  enabled: !_isSubmitting,
                  onTap: _showImageContractNotice,
                ),
              ),
              if (_error != null) ...[
                const SizedBox(height: 14),
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.danger.withValues(alpha: 0.08),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Row(
                    children: [
                      const Icon(
                        Icons.error_outline_rounded,
                        color: AppColors.danger,
                      ),
                      const SizedBox(width: 8),
                      Expanded(child: Text(_error!)),
                    ],
                  ),
                ),
              ],
              const SizedBox(height: 16),
              FilledButton.icon(
                key: const ValueKey('vehicle-submit-button'),
                onPressed: _isSubmitting ? null : _submit,
                style: FilledButton.styleFrom(
                  backgroundColor: AppColors.primaryContainer,
                  foregroundColor: Colors.white,
                  minimumSize: const Size.fromHeight(56),
                ),
                icon: _isSubmitting
                    ? const SizedBox.square(
                        dimension: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.add_circle_outline_rounded),
                label: Text(
                  _isSubmitting ? 'Đang thêm xe...' : 'Thêm xe vào danh sách',
                ),
              ),
              const SizedBox(height: 18),
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFFF4F8FF),
                  border: Border.all(color: const Color(0xFFD7E5FF)),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Icon(
                      Icons.help_outline_rounded,
                      size: 18,
                      color: AppColors.primaryContainer,
                    ),
                    SizedBox(width: 9),
                    Expanded(
                      child: Text(
                        'Cần hỗ trợ tra cứu lịch sử sửa chữa xe đời cũ? '
                        'Vui lòng liên hệ garage để được hỗ trợ liên kết.',
                        style: TextStyle(
                          color: Color(0xFF64748B),
                          fontSize: 11,
                          height: 1.4,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _VehicleFormField extends StatelessWidget {
  const _VehicleFormField({
    required this.label,
    required this.child,
    this.required = false,
    this.optional = false,
  });

  final String label;
  final Widget child;
  final bool required;
  final bool optional;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text.rich(
          TextSpan(
            text: label,
            children: [
              if (required)
                const TextSpan(
                  text: ' *',
                  style: TextStyle(color: AppColors.danger),
                ),
              if (optional)
                const TextSpan(
                  text: ' (Tùy chọn)',
                  style: TextStyle(
                    color: Color(0xFF94A3B8),
                    fontWeight: FontWeight.w400,
                  ),
                ),
            ],
          ),
          style: const TextStyle(
            color: Color(0xFF334155),
            fontSize: 12,
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 6),
        child,
      ],
    );
  }
}

class _VehicleImagePlaceholder extends StatelessWidget {
  const _VehicleImagePlaceholder({required this.enabled, required this.onTap});

  final bool enabled;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      enabled: enabled,
      label: 'Thông tin tải ảnh xe',
      child: InkWell(
        key: const ValueKey('vehicle-image-placeholder'),
        onTap: enabled ? onTap : null,
        borderRadius: BorderRadius.circular(12),
        child: CustomPaint(
          painter: _DashedBorderPainter(
            color: const Color(0xFFCBD5E1),
            radius: 12,
          ),
          child: Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: const Color(0xFFF8FAFC).withValues(alpha: 0.72),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                Container(
                  width: 42,
                  height: 42,
                  decoration: BoxDecoration(
                    color: Colors.white,
                    border: Border.all(color: const Color(0xFFE2E8F0)),
                    borderRadius: BorderRadius.circular(9),
                  ),
                  child: const Icon(
                    Icons.add_a_photo_outlined,
                    color: AppColors.primaryContainer,
                    size: 22,
                  ),
                ),
                const SizedBox(width: 12),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Chụp ảnh hoặc chọn file',
                        style: TextStyle(
                          color: Color(0xFF1E293B),
                          fontSize: 12,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      SizedBox(height: 2),
                      Text(
                        'Cần API lưu ảnh để kích hoạt',
                        style: TextStyle(
                          color: Color(0xFF64748B),
                          fontSize: 10,
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 7,
                  ),
                  decoration: BoxDecoration(
                    color: const Color(0xFFEEF2FF),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: const Text(
                    'Tải lên',
                    style: TextStyle(
                      color: AppColors.primaryContainer,
                      fontSize: 11,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _DashedBorderPainter extends CustomPainter {
  const _DashedBorderPainter({required this.color, required this.radius});

  final Color color;
  final double radius;

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5;
    final path = Path()
      ..addRRect(
        RRect.fromRectAndRadius(Offset.zero & size, Radius.circular(radius)),
      );

    for (final metric in path.computeMetrics()) {
      var distance = 0.0;
      while (distance < metric.length) {
        final end = distance + 6 < metric.length ? distance + 6 : metric.length;
        canvas.drawPath(metric.extractPath(distance, end), paint);
        distance += 10;
      }
    }
  }

  @override
  bool shouldRepaint(covariant _DashedBorderPainter oldDelegate) {
    return oldDelegate.color != color || oldDelegate.radius != radius;
  }
}
