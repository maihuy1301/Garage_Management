import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../../../core/theme/app_colors.dart';
import '../data/profile_service.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key, this.gateway, this.onSaved});
  final ProfileGateway? gateway;
  final ValueChanged<CustomerProfile>? onSaved;
  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  final _form = GlobalKey<FormState>();
  final _name = TextEditingController();
  final _email = TextEditingController();
  final _phone = TextEditingController();
  final _address = TextEditingController();
  CustomerProfile? _profile;
  DateTime? _birthday;
  bool _loading = true, _saving = false;
  String? _error;
  ProfileGateway get _gateway =>
      widget.gateway ?? context.read<ProfileGateway>();

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    for (final controller in [_name, _email, _phone, _address]) {
      controller.dispose();
    }
    super.dispose();
  }

  void _fill(CustomerProfile profile) {
    _profile = profile;
    _name.text = profile.name;
    _email.text = profile.email;
    _phone.text = profile.phone;
    _address.text = profile.address;
    _birthday = profile.birthday;
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final result = await _gateway.load();
      if (mounted) setState(() => _fill(result));
    } on Object catch (error) {
      if (mounted) setState(() => _error = _message(error));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  String _message(Object error) => error is ProfileException
      ? error.message
      : 'Chưa thể cập nhật thông tin. Vui lòng thử lại.';
  Future<void> _save() async {
    if (_saving || !_form.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      final result = await _gateway.save({
        'hoTen': _name.text.trim(),
        'email': _email.text.trim(),
        'soDienThoai': _phone.text.trim(),
        'diaChi': _address.text.trim(),
        if (_birthday != null)
          'ngaySinh': _birthday!.toIso8601String().split('T').first,
      });
      if (!mounted) return;
      setState(() => _fill(result));
      widget.onSaved?.call(result);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Đã cập nhật thông tin cá nhân.')),
      );
    } on Object catch (error) {
      if (mounted) {
        setState(() => _error = _message(error));
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(_error!)));
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _pickBirthday() async {
    final today = DateUtils.dateOnly(DateTime.now());
    final current = _birthday;
    final selected = await showDatePicker(
      context: context,
      initialDate:
          current != null && !current.isAfter(today) && current.year >= 1900
          ? current
          : DateTime(today.year - 18, today.month, today.day),
      firstDate: DateTime(1900),
      lastDate: today,
      helpText: 'Chọn ngày sinh',
      cancelText: 'Hủy',
      confirmText: 'Chọn',
    );
    if (mounted && selected != null) setState(() => _birthday = selected);
  }

  @override
  Widget build(BuildContext context) => SafeArea(
    child: Column(
      children: [
        Row(
          children: [
            IconButton(
              tooltip: 'Quay lại',
              icon: const Icon(Icons.arrow_back),
              onPressed: () =>
                  context.canPop() ? context.pop() : context.go('/account'),
            ),
            Text(
              'Thông tin cá nhân',
              style: Theme.of(context).textTheme.titleLarge,
            ),
          ],
        ),
        Expanded(
          child: _loading
              ? const Center(child: CircularProgressIndicator())
              : ListView(
                  padding: const EdgeInsets.all(20),
                  children: [
                    if (_error != null) ...[
                      Text(
                        _error!,
                        style: const TextStyle(color: AppColors.danger),
                      ),
                      if (_profile == null)
                        OutlinedButton(
                          onPressed: _load,
                          child: const Text('Thử lại'),
                        ),
                      const SizedBox(height: 12),
                    ],
                    if (_profile != null)
                      Form(
                        key: _form,
                        child: Column(
                          children: [
                            const CircleAvatar(
                              radius: 36,
                              backgroundColor: AppColors.surfaceContainer,
                              child: Icon(
                                Icons.person_outline,
                                size: 40,
                                color: AppColors.primary,
                              ),
                            ),
                            const SizedBox(height: 12),
                            Text(
                              'Khách hàng',
                              style: Theme.of(context).textTheme.titleMedium,
                            ),
                            const SizedBox(height: 24),
                            TextFormField(
                              initialValue: _profile!.username,
                              readOnly: true,
                              decoration: const InputDecoration(
                                labelText: 'Tên đăng nhập',
                                helperText:
                                    'Số điện thoại liên hệ không thay đổi tên đăng nhập.',
                              ),
                            ),
                            const SizedBox(height: 16),
                            _field(
                              _name,
                              'Họ và tên',
                              100,
                              validator: (v) => v!.trim().isEmpty
                                  ? 'Vui lòng nhập họ tên.'
                                  : null,
                            ),
                            _field(
                              _email,
                              'Email',
                              100,
                              keyboard: TextInputType.emailAddress,
                              validator: (v) {
                                final value = v!.trim();
                                if (value.isEmpty) {
                                  return _profile!.email.isNotEmpty
                                      ? 'Nhập email mới để thay đổi email hiện tại.'
                                      : null;
                                }
                                return RegExp(
                                      r'^[^\s@]+@[^\s@]+\.[^\s@]+$',
                                    ).hasMatch(value)
                                    ? null
                                    : 'Email không hợp lệ.';
                              },
                            ),
                            _field(
                              _phone,
                              'Số điện thoại liên hệ',
                              20,
                              keyboard: TextInputType.phone,
                              validator: (v) =>
                                  RegExp(
                                    r'^\+?[0-9]{9,15}$',
                                  ).hasMatch(v!.trim())
                                  ? null
                                  : 'Nhập số điện thoại hợp lệ (9–15 chữ số).',
                            ),
                            _field(_address, 'Địa chỉ', 255, lines: 2),
                            ListTile(
                              contentPadding: EdgeInsets.zero,
                              title: const Text('Ngày sinh'),
                              subtitle: Text(
                                _birthday == null
                                    ? 'Chưa cập nhật'
                                    : '${_birthday!.day}/${_birthday!.month}/${_birthday!.year}',
                              ),
                              trailing: const Icon(
                                Icons.calendar_month,
                                color: AppColors.primary,
                              ),
                              onTap: _saving ? null : _pickBirthday,
                            ),
                            const SizedBox(height: 20),
                            SizedBox(
                              width: double.infinity,
                              child: FilledButton.icon(
                                onPressed: _saving ? null : _save,
                                icon: _saving
                                    ? const SizedBox(
                                        width: 18,
                                        height: 18,
                                        child: CircularProgressIndicator(
                                          strokeWidth: 2,
                                        ),
                                      )
                                    : const Icon(Icons.save_outlined),
                                label: Text(
                                  _saving ? 'Đang lưu…' : 'Lưu thay đổi',
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                  ],
                ),
        ),
      ],
    ),
  );
  Widget _field(
    TextEditingController controller,
    String label,
    int limit, {
    String? Function(String?)? validator,
    TextInputType? keyboard,
    int lines = 1,
  }) => Padding(
    padding: const EdgeInsets.only(bottom: 16),
    child: TextFormField(
      controller: controller,
      enabled: !_saving,
      maxLength: limit,
      maxLines: lines,
      keyboardType: keyboard,
      validator: validator,
      decoration: InputDecoration(labelText: label, counterText: ''),
    ),
  );
}
