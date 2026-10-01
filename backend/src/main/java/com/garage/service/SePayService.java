package com.garage.service;

import com.garage.config.SePayProperties;
import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.exception.*;
import com.garage.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

@Service
public class SePayService {
    private final SePayProperties config;
    private final InvoiceService invoices;
    private final HoaDonRepository invoiceRepo;
    private final ThanhToanRepository payments;
    private final PhienThanhToanRepository sessions;
    private final GiaoDichSePayRepository events;
    private final jakarta.persistence.EntityManager entityManager;

    public SePayService(SePayProperties config, InvoiceService invoices, HoaDonRepository invoiceRepo,
                        ThanhToanRepository payments, PhienThanhToanRepository sessions, GiaoDichSePayRepository events,
                        jakarta.persistence.EntityManager entityManager) {
        this.config = config; this.invoices = invoices; this.invoiceRepo = invoiceRepo;
        this.payments = payments; this.sessions = sessions; this.events = events;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public PaymentOptionsResponse options(Integer invoiceId) {
        InvoiceResponse invoice = invoices.getInvoiceById(invoiceId); // ownership / branch authorization
        boolean payable = canPay(invoice.getTrangThai(), invoice.getConLai());
        boolean ready = config.isReady() && payable;
        return new PaymentOptionsResponse(ready,
                !payable ? "Hóa đơn không còn khả dụng để thanh toán" :
                !config.isReady() ? "Garage chưa cấu hình thanh toán ngân hàng. Vui lòng liên hệ quầy tiếp nhận." :
                "Chọn tài khoản nhận của garage", config.getEnvironment(), invoice.getConLai(),
                ready ? List.of(receiver()) : List.of());
    }

    @Transactional
    public PaymentSessionResponse createSession(Integer invoiceId) {
        invoices.getInvoiceById(invoiceId);
        requireReady();
        HoaDon invoice = lockInvoice(invoiceId);
        BigDecimal remaining = remaining(invoice);
        if (!canPay(invoice.getTrangThai(), remaining)) throw new BadRequestException("Hóa đơn không thể thanh toán");
        if (remaining.stripTrailingZeros().scale() > 0) throw new BadRequestException("Số tiền chuyển khoản phải là số nguyên VND");
        LocalDateTime now = now();
        // The invoice lock serializes POSTs across devices and cash/webhook writes.
        for (PhienThanhToan session : sessions.findByInvoiceMaHoaDonOrderByCreatedAtDesc(invoiceId)) {
            if ("REQUIRES_REVIEW".equals(session.getStatus())) {
                throw new BadRequestException("Giao dịch đang cần đối soát. Vui lòng liên hệ garage trước khi chuyển thêm tiền.");
            }
            if (!"PENDING".equals(session.getStatus())) continue;
            if (session.getExpiresAt().isAfter(now) && session.getAmount().compareTo(remaining) == 0
                    && session.getEnvironment().equals(config.getEnvironment())
                    && session.getAccountNumber().equals(config.getAccountNumber())
                    && session.getBankCode().equals(config.getBankCode())) return response(session, invoice);
            session.setStatus("EXPIRED");
        }
        PhienThanhToan session = new PhienThanhToan();
        session.setId(UUID.randomUUID().toString());
        session.setInvoice(invoice);
        session.setAmount(remaining);
        session.setContent("GAR" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase(Locale.ROOT));
        session.setEnvironment(config.getEnvironment());
        session.setBankCode(config.getBankCode()); session.setBankName(config.getBankName());
        session.setAccountNumber(config.getAccountNumber()); session.setAccountName(config.getAccountName());
        session.setStatus("PENDING"); session.setCreatedAt(now); session.setExpiresAt(now.plusMinutes(15));
        sessions.save(session);
        return response(session, invoice);
    }

    @Transactional(readOnly = true)
    public PaymentSessionResponse getSession(Integer invoiceId, String sessionId) {
        invoices.getInvoiceById(invoiceId);
        PhienThanhToan session = sessions.findById(sessionId)
                .filter(s -> s.getInvoice().getMaHoaDon().equals(invoiceId))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiên thanh toán"));
        return response(session, session.getInvoice());
    }

    public void authenticateWebhook(String authorization) {
        if (!config.isReady() || authorization == null || !MessageDigest.isEqual(
                ("Apikey " + config.getWebhookKey()).getBytes(StandardCharsets.UTF_8),
                authorization.getBytes(StandardCharsets.UTF_8))) {
            throw new AccessDeniedException("Webhook authentication failed");
        }
    }

    @Transactional
    public void receiveWebhook(SePayWebhookRequest request) {
        requireReady();
        String eventId = "SEPAY:" + config.getEnvironment() + ":" + request.id();
        // Only exact provider-extracted codes are accepted. Never guess from invoice IDs or free text.
        Integer invoiceId = request.code() == null ? null : sessions.findInvoiceIdByContent(request.code()).orElse(null);
        HoaDon invoice = invoiceId == null ? null : lockInvoice(invoiceId);
        // Load mutable session state only after the lock, avoiding stale status after a concurrent webhook.
        PhienThanhToan session = invoiceId == null ? null : sessions.findByContent(request.code()).orElse(null);
        // Check after obtaining invoice lock: concurrent retries cannot both credit an invoice.
        if (events.existsById(eventId)) return;
        String outcome;
        if (!"in".equals(request.transferType())) outcome = "IGNORED_OUT";
        else if (session == null) outcome = "UNKNOWN_CODE";
        else if (!session.getEnvironment().equals(config.getEnvironment())) outcome = "WRONG_ENVIRONMENT";
        else if (!session.getAccountNumber().equals(request.accountNumber())
                || !session.getBankName().equalsIgnoreCase(request.gateway())) outcome = "WRONG_RECEIVER";
        else if (!"PENDING".equals(session.getStatus()) || !session.getExpiresAt().isAfter(now())) outcome = "LATE_OR_CLOSED";
        else if (!canPay(invoice.getTrangThai(), remaining(invoice))) outcome = "INVOICE_CLOSED";
        else if (request.transferAmount().compareTo(session.getAmount()) != 0
                || request.transferAmount().compareTo(remaining(invoice)) != 0) outcome = "AMOUNT_MISMATCH";
        else {
            ThanhToan payment = new ThanhToan();
            payment.setHoaDon(invoice); payment.setSoTien(request.transferAmount());
            payment.setPhuongThuc("CHUYEN_KHOAN"); payment.setMaGiaoDich(eventId); payment.setTrangThai("THANH_CONG");
            payments.save(payment);
            invoice.setTrangThai("DA_THANH_TOAN");
            invoiceRepo.save(invoice);
            session.setStatus("SUCCEEDED");
            outcome = "APPLIED";
        }
        if (session != null && "in".equals(request.transferType()) && !"APPLIED".equals(outcome)
                && !"SUCCEEDED".equals(session.getStatus())) session.setStatus("REQUIRES_REVIEW");
        // Receipt.isNew() forces INSERT even with an assigned ID; merge could overwrite a
        // receipt inserted between existsById and save. A collision rolls back the whole
        // transaction; a provider retry then sees the persisted receipt and returns success.
        events.saveAndFlush(new GiaoDichSePay(eventId, session, request.transferAmount(), request.gateway(),
                request.accountNumber(), request.content(), request.referenceCode(), outcome, now()));
    }

    private PaymentSessionResponse response(PhienThanhToan session, HoaDon invoice) {
        BigDecimal remaining = remaining(invoice);
        String status = session.getStatus();
        if ("PENDING".equals(status) && (!session.getExpiresAt().isAfter(now())
                || !canPay(invoice.getTrangThai(), remaining) || session.getAmount().compareTo(remaining) != 0)) status = "EXPIRED";
        String qr = "PENDING".equals(status) && config.isReady()
                && session.getEnvironment().equals(config.getEnvironment()) ?
                "https://vietqr.app/img?acc=" + encode(session.getAccountNumber()) + "&bank=" + encode(session.getBankCode())
                + "&amount=" + session.getAmount().toBigIntegerExact() + "&des=" + encode(session.getContent()) : null;
        return new PaymentSessionResponse(session.getId(), invoice.getMaHoaDon(), status, session.getEnvironment(),
                session.getAmount(), "VND", session.getContent(), session.getExpiresAt().toInstant(ZoneOffset.UTC),
                new PaymentSessionResponse.Receiver(session.getBankCode(), session.getBankName(), session.getAccountNumber(), session.getAccountName()),
                qr, invoice.getTrangThai(), remaining);
    }
    private HoaDon lockInvoice(Integer id) {
        HoaDon invoice = invoiceRepo.findForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hóa đơn"));
        // Authorization may already have loaded this entity before waiting on the lock.
        entityManager.refresh(invoice, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return invoice;
    }
    private BigDecimal remaining(HoaDon invoice) {
        BigDecimal paid = payments.findByHoaDonMaHoaDon(invoice.getMaHoaDon()).stream()
                .filter(p -> "THANH_CONG".equals(p.getTrangThai())).map(ThanhToan::getSoTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return invoice.getThanhTien().subtract(paid).max(BigDecimal.ZERO);
    }
    private boolean canPay(String status, BigDecimal remaining) {
        return ("CHUA_THANH_TOAN".equals(status) || "THANH_TOAN_MOT_PHAN".equals(status)) && remaining.signum() > 0;
    }
    private PaymentSessionResponse.Receiver receiver() {
        return new PaymentSessionResponse.Receiver(config.getBankCode(), config.getBankName(), config.getAccountNumber(), config.getAccountName());
    }
    private void requireReady() {
        if (!config.isReady()) throw new BadRequestException("Garage chưa cấu hình thanh toán ngân hàng");
    }
    private static LocalDateTime now() { return LocalDateTime.now(ZoneOffset.UTC); }
    private static String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
