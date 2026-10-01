import 'package:flutter/material.dart';

import '../../../core/theme/app_colors.dart';

class PreliminaryEstimateCard extends StatelessWidget {
  const PreliminaryEstimateCard({
    super.key,
    required this.labor,
    required this.parts,
    required this.total,
  });

  final double? labor;
  final double? parts;
  final double? total;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.surfaceContainerLow,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.outlineVariant),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Báo giá sơ bộ', style: Theme.of(context).textTheme.titleSmall),
          const SizedBox(height: 8),
          _amount(context, 'Tiền công dự kiến', labor),
          _amount(context, 'Phụ tùng định mức dự kiến', parts),
          const Divider(),
          _amount(context, 'Tổng chi phí ước tính', total),
          const SizedBox(height: 8),
          const Text(
            'Chi phí tham khảo theo dịch vụ đã chọn, có thể thay đổi sau khi '
            'kiểm tra xe. Số tiền thanh toán được thể hiện trên hóa đơn của garage.',
            style: TextStyle(color: AppColors.onSurfaceVariant, fontSize: 12),
          ),
        ],
      ),
    );
  }

  Widget _amount(BuildContext context, String label, double? value) {
    final digits = value?.toStringAsFixed(2).split('.');
    final amount = value == null
        ? 'Chưa có dự toán'
        : '${digits![0].replaceAllMapped(RegExp(r'(\d)(?=(\d{3})+$)'), (m) => '${m[1]}.')}'
              '${digits[1] == '00' ? '' : ',${digits[1]}'} đ';
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: LayoutBuilder(
        builder: (context, constraints) {
          final children = [
            Text(label),
            Text(amount, style: const TextStyle(fontWeight: FontWeight.w700)),
          ];
          if (constraints.maxWidth < 340 ||
              MediaQuery.textScalerOf(context).scale(14) > 20) {
            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: children,
            );
          }
          return Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(child: children.first),
              children.last,
            ],
          );
        },
      ),
    );
  }
}
