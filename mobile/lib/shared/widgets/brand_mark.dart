import 'package:flutter/material.dart';

import '../../core/theme/app_colors.dart';

class BrandMark extends StatelessWidget {
  const BrandMark({super.key, this.compact = false, this.light = false});

  final bool compact;
  final bool light;

  @override
  Widget build(BuildContext context) {
    final color = light ? Colors.white : AppColors.primary;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: compact ? 34 : 42,
          height: compact ? 34 : 42,
          decoration: BoxDecoration(
            color: light ? Colors.white.withValues(alpha: 0.14) : color,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(
            Icons.precision_manufacturing_rounded,
            color: light ? Colors.white : Colors.white,
            size: compact ? 20 : 25,
          ),
        ),
        const SizedBox(width: 10),
        Text(
          'AutoCare',
          style: TextStyle(
            color: color,
            fontSize: compact ? 20 : 24,
            fontWeight: FontWeight.w800,
            letterSpacing: -0.5,
          ),
        ),
      ],
    );
  }
}
