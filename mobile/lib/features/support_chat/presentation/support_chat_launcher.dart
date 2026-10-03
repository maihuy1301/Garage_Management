import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'support_chat_controller.dart';

class SupportChatLauncher extends StatelessWidget {
  const SupportChatLauncher({super.key});
  @override
  Widget build(BuildContext context) {
    final chat = context.watch<SupportChatController?>();
    return FloatingActionButton.small(
      heroTag: 'customer-support',
      tooltip: 'Tư vấn & đặt lịch',
      onPressed: () => context.push('/support-chat'),
      child: Badge(
        isLabelVisible: (chat?.unread ?? 0) > 0,
        label: Text('${chat?.unread ?? 0}'),
        child: const Icon(Icons.smart_toy_outlined),
      ),
    );
  }
}
