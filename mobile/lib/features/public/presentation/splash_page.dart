import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../core/theme/app_colors.dart';

class SplashPage extends StatelessWidget {
  const SplashPage({super.key});

  @override
  Widget build(BuildContext context) {
    return AnnotatedRegion<SystemUiOverlayStyle>(
      value: SystemUiOverlayStyle.light,
      child: Scaffold(
        backgroundColor: AppColors.primary,
        body: Stack(
          fit: StackFit.expand,
          children: [
            Image.asset(
              'asset/screen.png',
              key: const ValueKey('splash-background'),
              fit: BoxFit.cover,
              alignment: Alignment.center,
              semanticLabel: 'AutoCare - Trusted Vehicle Solutions',
            ),
            const DecoratedBox(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [
                    Color(0x26001A45),
                    Color(0x00001A45),
                    Color(0xB3001738),
                  ],
                  stops: [0, 0.55, 1],
                ),
              ),
            ),
            SafeArea(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(24, 24, 24, 34),
                child: Align(
                  alignment: Alignment.bottomCenter,
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const SizedBox(
                        width: 28,
                        height: 28,
                        child: CircularProgressIndicator(
                          color: Colors.white,
                          strokeWidth: 2.6,
                        ),
                      ),
                      const SizedBox(height: 16),
                      Text(
                        'Đang khởi động...',
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Colors.white,
                          fontWeight: FontWeight.w600,
                          letterSpacing: 0.2,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
