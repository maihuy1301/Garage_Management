import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:geolocator/geolocator.dart';
import 'package:garage_mobile/features/support_chat/data/support_chat_service.dart';
import 'package:garage_mobile/features/support_chat/presentation/support_branch_picker.dart';

class FakeLocation extends GeolocatorPlatform {
  int requests = 0, reads = 0;
  LocationPermission permission = LocationPermission.denied;
  Completer<Position>? pending;
  @override
  Future<bool> isLocationServiceEnabled() async => true;
  @override
  Future<LocationPermission> checkPermission() async => permission;
  @override
  Future<LocationPermission> requestPermission() async {
    requests++;
    return permission;
  }

  @override
  Future<Position> getCurrentPosition({LocationSettings? locationSettings}) {
    reads++;
    return pending?.future ?? Future.value(position());
  }

  Position position() => Position(
    longitude: 106,
    latitude: 10,
    timestamp: DateTime.now(),
    accuracy: 30,
    altitude: 0,
    altitudeAccuracy: 0,
    heading: 0,
    headingAccuracy: 0,
    speed: 0,
    speedAccuracy: 0,
  );
}

class FakeBranches implements SupportBranchGateway {
  int gpsCalls = 0;
  @override
  Future<BranchSuggestions> suggestions(
    String token, {
    double? latitude,
    double? longitude,
  }) async {
    if (latitude != null) gpsCalls++;
    return BranchSuggestions.fromJson({
      'explanation': 'Gợi ý theo lịch đặt',
      'branches': [
        {
          'id': 1,
          'name': 'Gara 1',
          'address': 'Địa chỉ',
          'reason': 'Bạn có 2 lịch tại đây',
          'distanceKm': null,
        },
      ],
    });
  }
}

void main() {
  late GeolocatorPlatform original;
  late FakeLocation location;
  late FakeBranches gateway;
  setUp(() {
    original = GeolocatorPlatform.instance;
    location = FakeLocation();
    GeolocatorPlatform.instance = location;
    gateway = FakeBranches();
  });
  tearDown(() => GeolocatorPlatform.instance = original);
  Future<void> show(WidgetTester tester, {bool Function()? valid}) async {
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: SupportBranchPicker(
            gateway: gateway,
            token: 'session',
            sessionValid: valid ?? () => true,
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();
  }

  testWidgets('no GPS without click; denial keeps manual choices', (
    tester,
  ) async {
    await show(tester);
    expect(location.requests, 0);
    expect(location.reads, 0);
    expect(gateway.gpsCalls, 0);
    await tester.tap(find.text('Dùng vị trí của tôi'));
    await tester.pumpAndSettle();
    expect(location.requests, 1);
    expect(location.reads, 0);
    expect(gateway.gpsCalls, 0);
    expect(find.text('Gara 1'), findsOneWidget);
    expect(find.textContaining('Bạn chưa cho phép vị trí'), findsOneWidget);
  });
  testWidgets('GPS only sent after user action and permission', (tester) async {
    location.permission = LocationPermission.whileInUse;
    await show(tester);
    await tester.tap(find.text('Dùng vị trí của tôi'));
    await tester.pumpAndSettle();
    expect(location.reads, 1);
    expect(gateway.gpsCalls, 1);
  });
  testWidgets('logout while GPS pending prevents location upload', (
    tester,
  ) async {
    var valid = true;
    location.permission = LocationPermission.whileInUse;
    location.pending = Completer<Position>();
    await show(tester, valid: () => valid);
    await tester.tap(find.text('Dùng vị trí của tôi'));
    await tester.pump();
    valid = false;
    location.pending!.complete(location.position());
    await tester.pump();
    expect(gateway.gpsCalls, 0);
    await tester.pumpWidget(const SizedBox.shrink());
  });
}
