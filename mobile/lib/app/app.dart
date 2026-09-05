import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../core/auth/auth_controller.dart';
import '../core/theme/app_theme.dart';
import 'router.dart';

class GarageApp extends StatefulWidget {
  const GarageApp({super.key});

  @override
  State<GarageApp> createState() => _GarageAppState();
}

class _GarageAppState extends State<GarageApp> {
  late final AppRouter _appRouter;

  @override
  void initState() {
    super.initState();
    _appRouter = AppRouter(context.read<AuthController>());
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'AutoCare Garage',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      routerConfig: _appRouter.router,
    );
  }
}
