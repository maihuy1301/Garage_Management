import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../../core/theme/app_colors.dart';
import '../../appointments/data/appointment_service.dart';
import '../../appointments/domain/appointment_models.dart';
import '../../appointments/presentation/appointment_booking_page.dart';
import '../data/support_chat_service.dart';
import 'support_chat_controller.dart';
import 'support_branch_picker.dart';

class SupportChatPage extends StatefulWidget {
  const SupportChatPage({super.key});
  @override
  State<SupportChatPage> createState() => _SupportChatPageState();
}

class _SupportChatPageState extends State<SupportChatPage>
    with WidgetsBindingObserver {
  final _input = TextEditingController();
  final _scroll = ScrollController();
  SupportChatController? _chat;
  int? _lastMessage;
  bool _booking = false;
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final chat = context.read<SupportChatController>();
    if (_chat != chat) {
      _chat = chat;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted) chat.setVisible(true);
      });
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    _chat?.setVisible(state == AppLifecycleState.resumed && !_booking);
  }

  @override
  void dispose() {
    _chat?.setVisible(false);
    WidgetsBinding.instance.removeObserver(this);
    _input.dispose();
    _scroll.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    final chat = _chat!;
    if (await chat.send(_input.text) && mounted) _input.clear();
  }

  Future<void> _findGarage(SupportChatController chat) async {
    final gateway = chat.gateway;
    final token = chat.token;
    if (gateway is! SupportBranchGateway || token == null) return;
    final id = await showModalBottomSheet<int>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      builder: (sheet) => Consumer<SupportChatController>(
        builder: (_, current, _) {
          if (current.token != token) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              if (sheet.mounted) Navigator.pop(sheet);
            });
            return const SizedBox.shrink();
          }
          return ConstrainedBox(
            constraints: BoxConstraints(
              maxHeight: MediaQuery.sizeOf(sheet).height * .85,
            ),
            child: SupportBranchPicker(
              gateway: gateway as SupportBranchGateway,
              token: token,
              sessionValid: () => mounted && chat.token == token,
            ),
          );
        },
      ),
    );
    if (mounted && chat.token == token && id != null) await chat.open(id);
  }

  Future<void> _book(SupportChatController chat) async {
    final room = chat.selected;
    final token = chat.token;
    if (room == null || token == null) return;
    final gateway = _SupportBookingGateway(
      context.read<AppointmentGateway>(),
      chat.gateway,
      token,
      room.id,
      room.branchId,
    );
    _booking = true;
    chat.setVisible(false);
    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      builder: (sheet) => Consumer<SupportChatController>(
        builder: (_, current, _) {
          if (current.token != token) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              if (sheet.mounted) Navigator.pop(sheet);
            });
            return const SizedBox.shrink();
          }
          return SizedBox(
            height: MediaQuery.sizeOf(sheet).height * .92,
            child: Scaffold(
              appBar: AppBar(title: const Text('Đặt lịch từ trò chuyện')),
              body: AppointmentBookingPage(
                gateway: gateway,
                initialBranchId: room.branchId,
                confirmBeforeSubmit: true,
                onBooked: (_) => Navigator.pop(sheet),
              ),
            ),
          );
        },
      ),
    );
    _booking = false;
    if (mounted) chat.setVisible(true);
  }

  @override
  Widget build(BuildContext context) {
    final chat = context.watch<SupportChatController>();
    final room = chat.selected;
    final last = chat.messages.lastOrNull?.id;
    if (_lastMessage != last) {
      _lastMessage = last;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && _scroll.hasClients) {
          _scroll.animateTo(
            _scroll.position.maxScrollExtent,
            duration: const Duration(milliseconds: 180),
            curve: Curves.easeOut,
          );
        }
      });
    }
    return Scaffold(
      appBar: AppBar(
        title: const Text('Tư vấn & đặt lịch'),
        actions: [
          if (chat.enabled == true && chat.gateway is SupportBranchGateway)
            TextButton.icon(
              onPressed: chat.busy ? null : () => _findGarage(chat),
              icon: const Icon(Icons.location_on_outlined),
              label: const Text('Tìm gara'),
            ),
          if (room != null)
            IconButton(
              tooltip: 'Chọn chi nhánh',
              onPressed: chat.busy ? null : () => _branches(chat),
              icon: const Icon(Icons.store_outlined),
            ),
        ],
      ),
      body: SafeArea(
        child: Column(
          children: [
            if (chat.error != null)
              MaterialBanner(
                content: Text(chat.error!),
                actions: [
                  TextButton(
                    onPressed: chat.loading ? null : chat.initialize,
                    child: const Text('Thử lại'),
                  ),
                ],
              ),
            if (chat.enabled == false)
              const Expanded(
                child: Center(
                  child: Padding(
                    padding: EdgeInsets.all(24),
                    child: Text(
                      'Chat hỗ trợ đang được chuẩn bị. Bạn vẫn có thể đặt lịch tại mục Đặt lịch.',
                    ),
                  ),
                ),
              )
            else if (chat.enabled == null || chat.loading)
              const Expanded(child: Center(child: CircularProgressIndicator()))
            else if (room == null)
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.all(20),
                  children: [
                    const Icon(
                      Icons.support_agent_rounded,
                      size: 60,
                      color: AppColors.primary,
                    ),
                    const SizedBox(height: 16),
                    const Text(
                      'Bạn muốn được chi nhánh nào hỗ trợ?',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 8),
                    const Text(
                      'Trợ lý tự động hỗ trợ trước. Bạn có thể gặp tiếp tân khi cần.',
                    ),
                    const SizedBox(height: 20),
                    if (chat.gateway is SupportBranchGateway)
                      OutlinedButton.icon(
                        onPressed: chat.busy ? null : () => _findGarage(chat),
                        icon: const Icon(Icons.my_location),
                        label: const Text('Gợi ý theo lịch đặt / GPS'),
                      ),
                    for (final r in chat.rooms)
                      Card(
                        child: ListTile(
                          title: Text(r.branchName),
                          subtitle: Text(r.label),
                          trailing: r.unread > 0
                              ? Badge(label: Text('${r.unread}'))
                              : const Icon(Icons.chevron_right),
                          onTap: chat.busy ? null : () => chat.select(r.id),
                        ),
                      ),
                    for (final branch in chat.branches.where(
                      (b) => !chat.rooms.any((r) => r.branchId == b.id),
                    ))
                      Card(
                        child: ListTile(
                          title: Text(branch.name),
                          subtitle: Text(branch.address),
                          trailing: const Icon(Icons.chevron_right),
                          onTap: chat.busy ? null : () => chat.open(branch.id),
                        ),
                      ),
                    if (chat.branches.isEmpty && chat.rooms.isEmpty)
                      const Text('Chưa có chi nhánh đang hoạt động.'),
                  ],
                ),
              )
            else ...[
              Container(
                width: double.infinity,
                color: AppColors.primary.withValues(alpha: .06),
                padding: const EdgeInsets.all(14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      room.branchName,
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                    Text(room.label),
                    Text(
                      chat.connected ? 'Đã kết nối' : 'Đang kết nối lại…',
                      style: const TextStyle(fontSize: 11),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: ListView.builder(
                  controller: _scroll,
                  padding: const EdgeInsets.all(16),
                  itemCount: chat.messages.length + 1,
                  itemBuilder: (context, index) {
                    if (index == 0) {
                      return chat.hasMore
                          ? TextButton(
                              onPressed: chat.busy ? null : chat.older,
                              child: const Text('Xem tin nhắn trước'),
                            )
                          : const SizedBox.shrink();
                    }
                    final message = chat.messages[index - 1];
                    final mine = message.type == 'CUSTOMER';
                    final system = message.type == 'SYSTEM';
                    return Align(
                      alignment: system
                          ? Alignment.center
                          : mine
                          ? Alignment.centerRight
                          : Alignment.centerLeft,
                      child: Container(
                        constraints: BoxConstraints(
                          maxWidth: MediaQuery.sizeOf(context).width * .83,
                        ),
                        margin: const EdgeInsets.only(bottom: 12),
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: system
                              ? Colors.transparent
                              : mine
                              ? AppColors.primary
                              : AppColors.primary.withValues(alpha: .07),
                          borderRadius: BorderRadius.circular(16),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              message.name,
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w600,
                                color: mine
                                    ? Colors.white70
                                    : AppColors.primary,
                              ),
                            ),
                            const SizedBox(height: 5),
                            Text(
                              message.content,
                              style: TextStyle(
                                color: mine ? Colors.white : null,
                              ),
                            ),
                            const SizedBox(height: 5),
                            Text(
                              '${message.time.hour.toString().padLeft(2, '0')}:${message.time.minute.toString().padLeft(2, '0')}',
                              style: TextStyle(
                                fontSize: 10,
                                color: mine ? Colors.white70 : Colors.grey,
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: Wrap(
                  spacing: 8,
                  children: [
                    ActionChip(
                      avatar: const Icon(Icons.calendar_month, size: 18),
                      label: const Text('Đặt lịch'),
                      onPressed: chat.busy ? null : () => _book(chat),
                    ),
                    if (room.status == 'BOT')
                      ActionChip(
                        label: const Text('Dịch vụ & giá'),
                        onPressed: chat.busy
                            ? null
                            : () =>
                                  chat.send('Cho tôi xem dịch vụ và bảng giá'),
                      ),
                    if (room.status == 'BOT')
                      ActionChip(
                        avatar: const Icon(Icons.support_agent, size: 18),
                        label: const Text('Gặp tiếp tân'),
                        onPressed: chat.busy ? null : chat.handoff,
                      ),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(12),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _input,
                        enabled: !chat.busy,
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
                      onPressed: chat.busy || _input.text.trim().isEmpty
                          ? null
                          : _send,
                      icon: chat.busy
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            )
                          : const Icon(Icons.send),
                    ),
                  ],
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Future<void> _branches(SupportChatController chat) async {
    final id = await showModalBottomSheet<int>(
      context: context,
      useSafeArea: true,
      builder: (ctx) => ListView(
        shrinkWrap: true,
        children: [
          const ListTile(title: Text('Chọn chi nhánh hỗ trợ')),
          for (final b in chat.branches)
            ListTile(
              title: Text(b.name),
              subtitle: Text(b.address),
              onTap: () => Navigator.pop(ctx, b.id),
            ),
        ],
      ),
    );
    if (id != null && mounted) {
      _input.clear();
      await chat.open(id);
    }
  }
}

class _SupportBookingGateway implements AppointmentGateway {
  _SupportBookingGateway(
    this.original,
    this.chat,
    this.token,
    this.room,
    this.branch,
  );
  final AppointmentGateway original;
  final SupportGateway chat;
  final String token;
  final int room, branch;
  String? _key, _payload;
  @override
  Future<AppointmentBookingOptions> loadBookingOptions() async {
    final options = await original.loadBookingOptions();
    return AppointmentBookingOptions(
      vehicles: options.vehicles,
      branches: options.branches.where((b) => b.id == branch).toList(),
      services: options.services,
    );
  }

  @override
  Future<List<AppointmentBookingResult>> loadMyAppointments() =>
      original.loadMyAppointments();
  @override
  Future<AppointmentBookingResult> loadAppointment(int appointmentId) =>
      original.loadAppointment(appointmentId);
  @override
  Future<AppointmentBookingResult> cancelAppointment(int appointmentId) =>
      original.cancelAppointment(appointmentId);
  @override
  Future<AppointmentBookingResult> createAppointment({
    required int vehicleId,
    required int branchId,
    required DateTime appointmentTime,
    String? note,
    List<int>? serviceIds,
  }) async {
    final data = <String, dynamic>{
      'maXe': vehicleId,
      'maChiNhanh': branch,
      'thoiGianHen': appointmentTime.toIso8601String(),
      'ghiChu': note,
      'maDichVuList': serviceIds,
    };
    if (_payload != data.toString()) {
      _payload = data.toString();
      _key = supportRequestId();
    }
    try {
      return await chat.book(token, room, _key!, data);
    } on SupportException catch (e) {
      throw AppointmentException(e.message);
    }
  }
}
