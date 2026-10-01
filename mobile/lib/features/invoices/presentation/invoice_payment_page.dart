import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../../../core/theme/app_colors.dart';
import '../data/payment_service.dart';
import '../data/payment_qr_actions.dart';
import '../domain/payment_models.dart';
import 'invoices_page.dart';

class InvoicePaymentPage extends StatefulWidget {
  const InvoicePaymentPage({
    super.key,
    required this.invoiceId,
    this.gateway,
    this.qrActions,
  });
  final int invoiceId;
  final PaymentGateway? gateway;
  final PaymentQrActions? qrActions;
  @override
  State<InvoicePaymentPage> createState() => _InvoicePaymentPageState();
}

class _InvoicePaymentPageState extends State<InvoicePaymentPage>
    with WidgetsBindingObserver {
  PaymentOptions? _options;
  PaymentSession? _session;
  String? _error;
  String? _qrMessage;
  late final PaymentQrActions _defaultQrActions = AndroidPaymentQrActions();
  PaymentQrActions get _qrActions => widget.qrActions ?? _defaultQrActions;
  bool _busy = false;
  bool _active = true;
  int _generation = 0;
  Timer? _poll;
  PaymentGateway get _gateway =>
      widget.gateway ?? context.read<PaymentGateway>();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _loadOptions();
  }

  @override
  void didUpdateWidget(covariant InvoicePaymentPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.invoiceId != widget.invoiceId ||
        oldWidget.gateway != widget.gateway) {
      _generation++;
      _poll?.cancel();
      _busy = false;
      _session = null;
      _qrMessage = null;
      _options = null;
      _loadOptions();
    }
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    _active = state == AppLifecycleState.resumed;
    _poll?.cancel();
    if (_active) {
      if (_session != null) {
        _check();
      } else {
        _loadOptions();
      }
      _schedule();
    }
  }

  @override
  void dispose() {
    _generation++;
    _poll?.cancel();
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  Future<void> _perform(Future<void> Function(int) action) async {
    if (_busy) return;
    final generation = _generation;
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      await action(generation);
    } on Object catch (e) {
      if (mounted && generation == _generation) {
        setState(() {
          _error = e is PaymentException
              ? e.message
              : 'Chưa thể kiểm tra thanh toán. Vui lòng thử lại.';
          if (e is PaymentException && e.accessUnavailable) {
            _session = null;
            _qrMessage = null;
            _options = null;
            _poll?.cancel();
          }
        });
      }
    } finally {
      if (mounted && generation == _generation) {
        setState(() => _busy = false);
        _schedule();
      }
    }
  }

  Future<void> _loadOptions() => _perform((generation) async {
    final result = await _gateway.options(widget.invoiceId);
    if (mounted && generation == _generation) setState(() => _options = result);
  });

  Future<void> _create() => _perform((generation) async {
    final result = await _gateway.create(widget.invoiceId);
    if (mounted && generation == _generation) setState(() => _session = result);
  });

  Future<void> _check() => _perform((generation) async {
    final current = _session;
    if (current == null) return;
    final result = await _gateway.status(widget.invoiceId, current.id);
    if (mounted && generation == _generation) setState(() => _session = result);
  });

  bool _canSave(PaymentSession session) =>
      session.pending &&
      !session.paid &&
      session.expiresAt.isAfter(DateTime.now()) &&
      session.qrImageUrl != null;

  Future<void> _saveQr({required bool openBank}) async {
    final startedGeneration = _generation;
    await _perform((generation) async {
      final current = _session;
      if (current == null) return;
      _poll?.cancel();
      setState(() => _qrMessage = null);
      // Revalidate ownership and payment state before exporting a QR.
      final latest = await _gateway.status(widget.invoiceId, current.id);
      if (!mounted || generation != _generation) return;
      setState(() => _session = latest);
      if (!_canSave(latest)) {
        throw const PaymentException(
          'QR đã hết hạn hoặc hóa đơn đã thay đổi. Vui lòng kiểm tra trạng thái.',
        );
      }
      bool isCurrent() =>
          mounted &&
          generation == _generation &&
          _session?.id == latest.id &&
          _canSave(_session!);
      await _qrActions.save(latest, isCurrent: isCurrent);
      if (!mounted || generation != _generation) return;
      setState(
        () => _qrMessage =
            'Đã lưu QR vào thư viện ảnh. Trong MB Bank, chọn Quét QR rồi chọn ảnh vừa lưu.',
      );
      if (!openBank) return;
      if (!isCurrent() || !_active) {
        setState(
          () => _qrMessage =
              'Đã lưu ảnh QR. Hãy kiểm tra lại phiên thanh toán trước khi chuyển tiền.',
        );
        return;
      }
      final opened = await _qrActions.openMbBank();
      if (!mounted || generation != _generation) return;
      if (!opened) {
        setState(
          () => _qrMessage =
              'Đã lưu QR nhưng chưa mở được MB Bank. Hãy kiểm tra ứng dụng MB đã được cài trên thiết bị này; bạn vẫn có thể mở ngân hàng thủ công và chọn ảnh QR vừa lưu.',
        );
      }
    });
    final message = _error ?? _qrMessage;
    if (mounted &&
        startedGeneration == _generation &&
        _active &&
        message != null) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text(message)));
    }
  }

  void _schedule() {
    _poll?.cancel();
    if (_active && _session?.pending == true && _session?.paid != true) {
      _poll = Timer(const Duration(seconds: 5), () {
        if (mounted) _check();
      });
    }
  }

  Future<void> _copy(String label, String value) async {
    await Clipboard.setData(ClipboardData(text: value));
    if (!mounted) return;
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text('Đã sao chép $label')));
  }

  @override
  Widget build(BuildContext context) {
    final session = _session;
    final options = _options;
    if (session?.paid == true) return _success(session!);
    final waiting = session != null && session.pending && !session.paid;
    final canTransfer =
        waiting &&
        session.expiresAt.isAfter(DateTime.now()) &&
        session.qrImageUrl != null;
    return SafeArea(
      child: Column(
        children: [
          Row(
            children: [
              IconButton(
                tooltip: 'Quay lại',
                onPressed: () {
                  if (context.canPop()) {
                    context.pop();
                  } else {
                    context.go('/invoices/${widget.invoiceId}');
                  }
                },
                icon: const Icon(Icons.arrow_back),
              ),
              Expanded(
                child: Text(
                  'Thanh toán ngân hàng',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ),
            ],
          ),
          Expanded(
            child: RefreshIndicator(
              onRefresh: session == null ? _loadOptions : _check,
              child: ListView(
                padding: const EdgeInsets.all(16),
                physics: const AlwaysScrollableScrollPhysics(),
                children: [
                  _card([
                    Text(
                      'Hóa đơn #${widget.invoiceId}',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    const SizedBox(height: 8),
                    Text(
                      session == null && options == null
                          ? 'Chưa có thông tin số tiền'
                          : invoiceMoney(
                              session?.remaining ?? options!.remaining,
                            ),
                      style: Theme.of(context).textTheme.headlineMedium
                          ?.copyWith(
                            color: AppColors.primary,
                            fontWeight: FontWeight.w800,
                          ),
                    ),
                    const Text('Số tiền còn lại'),
                    if ((session?.environment ?? options?.environment) ==
                        'test') ...[
                      const SizedBox(height: 12),
                      const Text(
                        'Chế độ thử nghiệm — không chuyển tiền thật.',
                        style: TextStyle(
                          color: AppColors.warning,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ]),
                  if (_busy) const LinearProgressIndicator(),
                  if (_error != null)
                    _card([
                      Text(_error!, key: const ValueKey('payment-error')),
                      const SizedBox(height: 8),
                      OutlinedButton(
                        onPressed: _busy
                            ? null
                            : session == null
                            ? _loadOptions
                            : _check,
                        child: const Text('Thử lại'),
                      ),
                    ]),
                  if (session == null && options != null)
                    _card([
                      Text(options.message),
                      if (options.available &&
                          options.receivers.isNotEmpty) ...[
                        const SizedBox(height: 12),
                        // MVP has one configured receiving account, supplied by the backend.
                        ListTile(
                          contentPadding: EdgeInsets.zero,
                          leading: const Icon(
                            Icons.account_balance,
                            color: AppColors.primary,
                          ),
                          title: Text(options.receivers.first.bankName),
                          subtitle: Text(
                            '${options.receivers.first.accountName}\n${options.receivers.first.accountNumber}',
                          ),
                          trailing: const Icon(
                            Icons.check_circle,
                            color: AppColors.primary,
                          ),
                        ),
                        FilledButton(
                          onPressed: _busy ? null : _create,
                          child: const Text('Tiếp tục thanh toán'),
                        ),
                      ],
                    ]),
                  if (session != null)
                    _card([
                      Icon(
                        session.paid
                            ? Icons.check_circle
                            : session.status == 'REQUIRES_REVIEW'
                            ? Icons.support_agent
                            : Icons.qr_code_2,
                        color: session.paid
                            ? AppColors.success
                            : AppColors.primary,
                        size: 48,
                      ),
                      const SizedBox(height: 12),
                      Text(
                        session.paid
                            ? 'Hóa đơn đã thanh toán'
                            : switch (session.status) {
                                'PENDING' => 'Đang chờ ngân hàng xác nhận',
                                'REQUIRES_REVIEW' =>
                                  'Giao dịch cần garage đối soát',
                                'EXPIRED' => 'Phiên thanh toán đã hết hiệu lực',
                                _ => 'Vui lòng kiểm tra trạng thái hóa đơn',
                              },
                        textAlign: TextAlign.center,
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      if (canTransfer) ...[
                        const SizedBox(height: 16),
                        Center(
                          child: ConstrainedBox(
                            constraints: const BoxConstraints(maxWidth: 280),
                            child: Image.network(
                              session.qrImageUrl!,
                              semanticLabel: 'Mã QR chuyển khoản của hóa đơn',
                              errorBuilder: (_, error, stackTrace) => const Padding(
                                padding: EdgeInsets.all(20),
                                child: Text(
                                  'Chưa tải được QR. Bạn có thể sao chép thông tin bên dưới để chuyển khoản.',
                                ),
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(height: 12),
                        if (_qrActions.supported) ...[
                          const Text('Ngân hàng bạn dùng: MB Bank'),
                          const SizedBox(height: 8),
                          FilledButton.icon(
                            onPressed: _busy
                                ? null
                                : () => _saveQr(openBank: true),
                            icon: const Icon(Icons.account_balance),
                            label: const Text('Lưu QR và mở MB Bank'),
                          ),
                          OutlinedButton.icon(
                            onPressed: _busy
                                ? null
                                : () => _saveQr(openBank: false),
                            icon: const Icon(Icons.download),
                            label: const Text('Lưu mã QR'),
                          ),
                          const Text(
                            'Ảnh được lưu vào thư viện trên thiết bị này. Trong MB Bank, chọn Quét QR → chọn ảnh vừa lưu, kiểm tra thông tin rồi xác nhận.',
                          ),
                          const SizedBox(height: 12),
                        ],
                        const Text(
                          'Mở app ngân hàng để quét mã hoặc nhập thông tin chuyển khoản. Nếu dùng cùng điện thoại, sao chép thông tin bên dưới.',
                        ),
                        _copyRow('Ngân hàng', session.receiver.bankName),
                        _copyRow('Chủ tài khoản', session.receiver.accountName),
                        _copyRow(
                          'Số tài khoản',
                          session.receiver.accountNumber,
                        ),
                        _copyRow('Số tiền', session.amount.toStringAsFixed(0)),
                        _copyRow('Nội dung', session.content),
                        Text(
                          'Hiệu lực đến ${invoiceDate(session.expiresAt.toLocal())}',
                        ),
                      ],
                      if (_qrMessage != null)
                        Padding(
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          child: Semantics(
                            liveRegion: true,
                            child: Text(_qrMessage!),
                          ),
                        ),
                      if (waiting && !canTransfer)
                        const Text(
                          'QR không còn khả dụng. Hãy kiểm tra trạng thái trước khi tạo phiên mới.',
                        ),
                      if (!session.paid) ...[
                        const SizedBox(height: 12),
                        const Text(
                          'Nếu đã chuyển tiền, không chuyển thêm. Kết quả chỉ được xác nhận sau khi garage nhận thông tin ngân hàng.',
                        ),
                        OutlinedButton(
                          onPressed: _busy ? null : _check,
                          child: const Text('Kiểm tra thanh toán'),
                        ),
                      ],
                      if (session.status == 'EXPIRED' && !session.paid)
                        TextButton(
                          onPressed: _busy ? null : _create,
                          child: const Text(
                            'Tạo phiên mới nếu chưa chuyển tiền',
                          ),
                        ),
                      if (session.status == 'REQUIRES_REVIEW')
                        const Text(
                          'Vui lòng liên hệ quầy tiếp nhận để kiểm tra số tiền và nội dung chuyển khoản.',
                        ),
                      if (session.paid)
                        FilledButton(
                          onPressed: () =>
                              context.go('/invoices/${widget.invoiceId}'),
                          child: const Text('Xem hóa đơn'),
                        ),
                    ]),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _copyRow(String label, String value) => ListTile(
    contentPadding: EdgeInsets.zero,
    title: Text(label),
    subtitle: SelectableText(value),
    trailing: IconButton(
      tooltip: 'Sao chép $label',
      onPressed: () => _copy(label, value),
      icon: const Icon(Icons.copy, size: 20),
    ),
  );
  Widget _success(PaymentSession session) => SafeArea(
    child: ListView(
      padding: const EdgeInsets.all(20),
      children: [
        const SizedBox(height: 32),
        Card(
          color: Colors.white,
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 40),
            child: Column(
              children: [
                Semantics(
                  label: 'Thanh toán thành công',
                  child: const CircleAvatar(
                    radius: 36,
                    backgroundColor: AppColors.primary,
                    child: Icon(
                      Icons.check_rounded,
                      color: Colors.white,
                      size: 48,
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                Text(
                  'Thanh toán thành công',
                  textAlign: TextAlign.center,
                  style: Theme.of(
                    context,
                  ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 20),
                Text.rich(
                  TextSpan(
                    children: [
                      const TextSpan(text: 'Mã hóa đơn của bạn là '),
                      TextSpan(
                        text: '#${session.invoiceId}',
                        style: const TextStyle(
                          color: AppColors.primary,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ],
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 12),
                Text(
                  'Đã thanh toán ${invoiceMoney(session.amount)}',
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 12),
                const Text(
                  'Bạn có thể xem lại chi tiết và lịch sử thanh toán trong hóa đơn của tôi.',
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),
                FilledButton(
                  onPressed: () => context.go('/invoices/${session.invoiceId}'),
                  child: const Text('Xem hóa đơn'),
                ),
                TextButton(
                  onPressed: () => context.go('/invoices'),
                  child: const Text('Hóa đơn của tôi'),
                ),
              ],
            ),
          ),
        ),
      ],
    ),
  );
  Widget _card(List<Widget> children) => Card(
    margin: const EdgeInsets.only(bottom: 16),
    child: Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: children,
      ),
    ),
  );
}
