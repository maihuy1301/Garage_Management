import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:garage_mobile/features/public/presentation/splash_page.dart';
import 'package:garage_mobile/shared/widgets/brand_mark.dart';

void main() {
  testWidgets('hiển thị nhận diện AutoCare', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(body: Center(child: BrandMark())),
      ),
    );

    expect(find.text('AutoCare'), findsOneWidget);
    expect(find.byIcon(Icons.precision_manufacturing_rounded), findsOneWidget);
  });

  testWidgets('splash screen sử dụng ảnh nhận diện trong asset', (
    tester,
  ) async {
    await tester.pumpWidget(const MaterialApp(home: SplashPage()));

    expect(find.byKey(const ValueKey('splash-background')), findsOneWidget);
    expect(find.text('Đang khởi động...'), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsOneWidget);
  });
}
