import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import '../data/support_chat_service.dart';

class SupportBranchPicker extends StatefulWidget {
  const SupportBranchPicker({
    super.key,
    required this.gateway,
    required this.token,
    required this.sessionValid,
  });
  final SupportBranchGateway gateway;
  final String token;
  final bool Function() sessionValid;
  @override
  State<SupportBranchPicker> createState() => _SupportBranchPickerState();
}

class _SupportBranchPickerState extends State<SupportBranchPicker> {
  BranchSuggestions? result;
  bool busy = true;
  String? error;
  bool get active => mounted && widget.sessionValid();
  @override
  void initState() {
    super.initState();
    _load(false);
  }

  Future<void> _load(bool gps) async {
    setState(() {
      busy = true;
      error = null;
    });
    try {
      double? latitude, longitude;
      if (gps) {
        if (!await Geolocator.isLocationServiceEnabled()) {
          throw const SupportException(
            'Hãy bật dịch vụ vị trí trên điện thoại hoặc chọn chi nhánh thủ công.',
          );
        }
        if (!active) return;
        var permission = await Geolocator.checkPermission();
        if (!active) return;
        if (permission == LocationPermission.denied) {
          permission = await Geolocator.requestPermission();
        }
        if (!active) return;
        if (permission == LocationPermission.deniedForever) {
          throw const SupportException(
            'Quyền vị trí đã bị tắt. Bạn có thể bật lại trong cài đặt ứng dụng hoặc chọn gara thủ công.',
          );
        }
        if (permission == LocationPermission.denied) {
          throw const SupportException(
            'Bạn chưa cho phép vị trí. Vẫn có thể chọn gara theo địa chỉ hoặc lịch đặt.',
          );
        }
        final position = await Geolocator.getCurrentPosition(
          locationSettings: const LocationSettings(
            accuracy: LocationAccuracy.medium,
            timeLimit: Duration(seconds: 15),
          ),
        );
        if (!active) return;
        latitude = position.latitude;
        longitude = position.longitude;
      }
      final loaded = await widget.gateway.suggestions(
        widget.token,
        latitude: latitude,
        longitude: longitude,
      );
      if (active) setState(() => result = loaded);
    } catch (e) {
      if (active) {
        setState(
          () => error = e is SupportException
              ? e.message
              : 'Chưa lấy được vị trí hoặc danh sách gara. Bạn có thể thử lại hoặc chọn thủ công.',
        );
      }
    } finally {
      if (active) setState(() => busy = false);
    }
  }

  @override
  Widget build(BuildContext context) => SafeArea(
    child: Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Tìm gara phù hợp',
            style: Theme.of(context).textTheme.titleLarge,
          ),
          const SizedBox(height: 8),
          const Text(
            'Gợi ý theo lịch đặt của bạn. Vị trí chỉ được gửi tới garage để tính khoảng cách khi bạn bấm nút bên dưới; không lưu GPS vào lịch sử chat.',
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              FilledButton.icon(
                onPressed: busy ? null : () => _load(true),
                icon: const Icon(Icons.my_location),
                label: const Text('Dùng vị trí của tôi'),
              ),
              TextButton(
                onPressed: busy ? null : () => _load(false),
                child: const Text('Theo lịch đặt'),
              ),
            ],
          ),
          if (busy) const LinearProgressIndicator(),
          if (error != null)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: Text(error!),
            ),
          if (result != null) ...[
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: Text(result!.explanation),
            ),
            Flexible(
              child: ListView(
                shrinkWrap: true,
                children: [
                  for (final branch in result!.branches)
                    ListTile(
                      title: Text(branch.name),
                      subtitle: Text(
                        '${branch.address}\n${branch.reason}${branch.distanceKm == null ? '' : '\nCách khoảng ${branch.distanceKm!.toStringAsFixed(1)} km (đường thẳng)'}',
                      ),
                      isThreeLine: true,
                      trailing: const Icon(Icons.chevron_right),
                      onTap: busy
                          ? null
                          : () {
                              if (widget.sessionValid()) {
                                Navigator.pop(context, branch.id);
                              }
                            },
                    ),
                  if (result!.branches.isEmpty)
                    const Text('Chưa có chi nhánh đang hoạt động.'),
                ],
              ),
            ),
          ],
        ],
      ),
    ),
  );
}
