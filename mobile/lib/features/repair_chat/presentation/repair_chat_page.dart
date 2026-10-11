import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/auth/auth_controller.dart';
import '../../../core/theme/app_colors.dart';
import '../data/repair_chat_service.dart';
import 'repair_chat_controller.dart';

class RepairChatPage extends StatefulWidget {
  const RepairChatPage({super.key, required this.orderId});
  final int orderId;
  @override
  State<RepairChatPage> createState() => _RepairChatPageState();
}

class _RepairChatPageState extends State<RepairChatPage>
    with WidgetsBindingObserver {
  late final RepairChatController chat;
  late final AuthController auth;
  final input = TextEditingController();
  final scroll = ScrollController();
  Timer? timer;
  String? _lastToken;
  bool _foreground = true;
  int _lastId = 0;

  @override
  void initState() {
    super.initState();
    auth = context.read<AuthController>();
    chat = RepairChatController(
      context.read<RepairChatGateway>(),
      widget.orderId,
    );
    chat.addListener(_updated);
    auth.addListener(_bind);
    WidgetsBinding.instance.addObserver(this);
    _bind();
    timer = Timer.periodic(const Duration(seconds: 5), (_) {
      if (_foreground && ModalRoute.of(context)?.isCurrent == true) {
        unawaited(chat.refresh());
      }
    });
  }

  void _bind() {
    final token = auth.session?.isTechnician == true
        ? auth.session!.accessToken
        : null;
    if (_lastToken != token) {
      input.clear();
      _lastId = 0;
      _lastToken = token;
    }
    chat.bind(token);
  }

  void _updated() {
    if (!mounted) return;
    if (chat.room == null || chat.room?.state == 'DELETED') input.clear();
    final latest = chat.messages.isEmpty ? 0 : chat.messages.last.id;
    final nearBottom =
        !scroll.hasClients ||
        scroll.position.maxScrollExtent - scroll.offset < 100;
    setState(() {});
    if (latest != _lastId && nearBottom) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && scroll.hasClients) {
          scroll.jumpTo(scroll.position.maxScrollExtent);
        }
      });
    }
    _lastId = latest;
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    _foreground = state == AppLifecycleState.resumed;
    if (_foreground) unawaited(chat.refresh());
  }

  @override
  void dispose() {
    timer?.cancel();
    auth.removeListener(_bind);
    WidgetsBinding.instance.removeObserver(this);
    chat.removeListener(_updated);
    chat.dispose();
    input.dispose();
    scroll.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    final sent = await chat.send(input.text);
    if (sent && mounted) input.clear();
  }

  @override
  Widget build(BuildContext context) {
    final room = chat.room;
    return Scaffold(
      appBar: AppBar(title: const Text('Trao đổi sửa chữa')),
      body: SafeArea(
        child: Column(
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              color: AppColors.surfaceContainer,
              child: Row(
                children: [
                  ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: chat.image == null
                        ? const SizedBox(
                            width: 60,
                            height: 52,
                            child: Icon(
                              Icons.directions_car_rounded,
                              size: 34,
                              color: AppColors.primary,
                            ),
                          )
                        : Image.memory(
                            chat.image!,
                            width: 60,
                            height: 52,
                            fit: BoxFit.cover,
                            errorBuilder: (_, error, stack) => const Icon(
                              Icons.directions_car_rounded,
                              size: 34,
                            ),
                          ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          room?.plate ?? 'Phiếu #${widget.orderId}',
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        if (room != null) ...[
                          Text('${room.vehicleName} · Phiếu #${room.orderId}'),
                          Text(
                            room.branchName,
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                          Text(
                            room.label,
                            style: const TextStyle(
                              color: AppColors.primary,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ] else
                          const Text('Tiếp tân và kỹ thuật viên'),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            if (chat.error != null)
              MaterialBanner(
                content: Text(chat.error!),
                actions: [
                  TextButton(
                    onPressed: chat.busy ? null : chat.load,
                    child: const Text('Thử lại'),
                  ),
                ],
              ),
            Expanded(
              child: chat.loading
                  ? const Center(child: CircularProgressIndicator())
                  : !chat.enabled
                  ? const Center(
                      child: Padding(
                        padding: EdgeInsets.all(24),
                        child: Text(
                          'Chat sửa chữa chưa được bật. Bạn có thể quay lại sau.',
                          textAlign: TextAlign.center,
                        ),
                      ),
                    )
                  : ListView(
                      controller: scroll,
                      padding: const EdgeInsets.all(16),
                      children: [
                        if (chat.hasMore)
                          TextButton(
                            onPressed: chat.busy
                                ? null
                                : () => chat.refresh(older: true),
                            child: const Text('Xem tin nhắn trước'),
                          ),
                        if (chat.messages.isEmpty)
                          Padding(
                            padding: const EdgeInsets.symmetric(vertical: 48),
                            child: Text(
                              room?.state == 'DELETED'
                                  ? 'Nội dung cuộc trò chuyện đã được xóa vĩnh viễn.'
                                  : room?.canSend == true
                                  ? 'Chưa có tin nhắn. Trao đổi với tiếp tân về xe và công việc sửa chữa tại đây.'
                                  : room?.label ??
                                        'Chưa tải được cuộc trò chuyện.',
                              textAlign: TextAlign.center,
                            ),
                          ),
                        for (final message in chat.messages)
                          _Bubble(message: message),
                      ],
                    ),
            ),
            if (room != null && !room.canSend)
              Padding(
                padding: const EdgeInsets.all(12),
                child: Text(room.label),
              ),
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 8, 12, 12),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Expanded(
                    child: TextField(
                      controller: input,
                      enabled: room?.canSend == true && !chat.busy,
                      minLines: 1,
                      maxLines: 4,
                      maxLength: 2000,
                      decoration: const InputDecoration(
                        hintText: 'Nhập tin nhắn…',
                        counterText: '',
                        border: OutlineInputBorder(),
                      ),
                      onChanged: (_) => setState(() {}),
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton.filled(
                    tooltip: 'Gửi tin nhắn',
                    onPressed:
                        room?.canSend == true &&
                            !chat.busy &&
                            input.text.trim().isNotEmpty
                        ? _send
                        : null,
                    icon: chat.busy
                        ? const SizedBox.square(
                            dimension: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.send_rounded),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _Bubble extends StatelessWidget {
  const _Bubble({required this.message});
  final RepairChatMessage message;
  @override
  Widget build(BuildContext context) {
    final time = message.sentAt;
    return Align(
      alignment: message.mine ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        constraints: BoxConstraints(
          maxWidth: MediaQuery.sizeOf(context).width * .82,
        ),
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: message.mine ? AppColors.primary : AppColors.surfaceContainer,
          borderRadius: BorderRadius.circular(12),
        ),
        child: DefaultTextStyle(
          style: TextStyle(
            color: message.mine ? Colors.white : AppColors.primary,
            fontSize: 15,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '${message.name} · ${message.roleLabel}',
                style: const TextStyle(fontSize: 11),
              ),
              const SizedBox(height: 6),
              SelectableText(
                message.content,
                style: TextStyle(
                  color: message.mine ? Colors.white : AppColors.primary,
                ),
              ),
              const SizedBox(height: 6),
              Text(
                '${time.day}/${time.month}/${time.year} ${time.hour.toString().padLeft(2, '0')}:${time.minute.toString().padLeft(2, '0')}',
                style: const TextStyle(fontSize: 11),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
