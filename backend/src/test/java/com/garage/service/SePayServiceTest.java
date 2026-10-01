package com.garage.service;

import com.garage.config.SePayProperties;
import com.garage.dto.*;
import com.garage.entity.*;
import com.garage.repository.*;
import com.garage.exception.BadRequestException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SePayServiceTest {
    @Mock InvoiceService invoices;
    @Mock HoaDonRepository invoiceRepo;
    @Mock ThanhToanRepository payments;
    @Mock PhienThanhToanRepository sessions;
    @Mock GiaoDichSePayRepository events;
    @Mock jakarta.persistence.EntityManager entityManager;
    SePayProperties config;
    SePayService service;
    HoaDon invoice;
    PhienThanhToan session;

    @BeforeEach void setup() {
        config = new SePayProperties(); config.setEnabled(true); config.setEnvironment("test");
        config.setWebhookKey("test-only-key-with-at-least-32-characters");
        config.setBankCode("TEST"); config.setBankName("TestBank");
        config.setAccountNumber("000000"); config.setAccountName("TEST ONLY");
        service = new SePayService(config, invoices, invoiceRepo, payments, sessions, events, entityManager);
        invoice = new HoaDon(); invoice.setMaHoaDon(1); invoice.setThanhTien(new BigDecimal("1190000"));
        invoice.setTrangThai("CHUA_THANH_TOAN");
        session = new PhienThanhToan(); session.setId("test-session"); session.setInvoice(invoice);
        session.setAmount(invoice.getThanhTien()); session.setContent("GARABC"); session.setStatus("PENDING");
        session.setEnvironment("test"); session.setBankCode("TEST"); session.setBankName("TestBank");
        session.setAccountNumber("000000"); session.setAccountName("TEST ONLY");
        session.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusMinutes(15));
    }
    void stubSession() {
        when(sessions.findInvoiceIdByContent("GARABC")).thenReturn(Optional.of(1));
        when(invoiceRepo.findForUpdate(1)).thenReturn(Optional.of(invoice));
        when(sessions.findByContent("GARABC")).thenReturn(Optional.of(session));
    }
    SePayWebhookRequest request(String amount) {
        return new SePayWebhookRequest(99L,"TestBank","000000","GARABC","GARABC payment","in",new BigDecimal(amount),"REF99");
    }
    @Test void authenticatedExactPaymentCreditsAndDuplicateDoesNot() {
        stubSession();
        when(events.existsById("SEPAY:test:99")).thenReturn(false, true);
        service.receiveWebhook(request("1190000"));
        service.receiveWebhook(request("1190000"));
        assertThat(invoice.getTrangThai()).isEqualTo("DA_THANH_TOAN");
        assertThat(session.getStatus()).isEqualTo("SUCCEEDED");
        verify(payments, times(1)).save(any());
        verify(events, times(1)).saveAndFlush(argThat(e -> "APPLIED".equals(e.getOutcome())));
    }
    @Test void wrongAmountIsDurableReviewNotPaid() {
        stubSession(); service.receiveWebhook(request("100"));
        assertThat(session.getStatus()).isEqualTo("REQUIRES_REVIEW");
        assertThat(invoice.getTrangThai()).isEqualTo("CHUA_THANH_TOAN");
        verify(payments, never()).save(any());
        verify(events).saveAndFlush(argThat(e -> "AMOUNT_MISMATCH".equals(e.getOutcome())));
    }
    @Test void latePaymentDoesNotDisappearOrMarkPaid() {
        stubSession(); session.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1));
        service.receiveWebhook(request("1190000"));
        verify(events).saveAndFlush(argThat(e -> "LATE_OR_CLOSED".equals(e.getOutcome())));
        verify(payments, never()).save(any());
    }
    @Test void cashAlreadyReceivedBlocksBankCredit() {
        stubSession(); invoice.setTrangThai("DA_THANH_TOAN");
        service.receiveWebhook(request("1190000"));
        verify(events).saveAndFlush(argThat(e -> "INVOICE_CLOSED".equals(e.getOutcome())));
        verify(payments, never()).save(any());
    }
    @Test void wrongReceiverNeverCredits() {
        stubSession();
        var r = request("1190000");
        service.receiveWebhook(new SePayWebhookRequest(r.id(),r.gateway(),"other",r.code(),r.content(),r.transferType(),r.transferAmount(),r.referenceCode()));
        verify(events).saveAndFlush(argThat(e -> "WRONG_RECEIVER".equals(e.getOutcome())));
        verify(payments, never()).save(any());
    }
    @Test void unknownCodeIsRecordedWithoutGuessingFromContent() {
        var r = request("1190000");
        service.receiveWebhook(new SePayWebhookRequest(r.id(),r.gateway(),r.accountNumber(),null,r.content(),r.transferType(),r.transferAmount(),r.referenceCode()));
        verify(events).saveAndFlush(argThat(e -> "UNKNOWN_CODE".equals(e.getOutcome())));
        verifyNoInteractions(invoiceRepo, payments);
    }
    @Test void outgoingIsRecordedButNotCredited() {
        var r = request("1190000");
        service.receiveWebhook(new SePayWebhookRequest(r.id(),r.gateway(),r.accountNumber(),null,r.content(),"out",r.transferAmount(),r.referenceCode()));
        verify(events).saveAndFlush(argThat(e -> "IGNORED_OUT".equals(e.getOutcome())));
        verifyNoInteractions(payments);
    }
    @Test void apiKeyRequiredAndDisabledFailsClosed() {
        assertThatThrownBy(() -> service.authenticateWebhook(null)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.authenticateWebhook("Bearer anything")).isInstanceOf(AccessDeniedException.class);
        assertThatCode(() -> service.authenticateWebhook("Apikey " + config.getWebhookKey())).doesNotThrowAnyException();
        config.setEnabled(false);
        assertThatThrownBy(() -> service.authenticateWebhook("Apikey " + config.getWebhookKey())).isInstanceOf(AccessDeniedException.class);
    }
    @Test void createReusesActiveSessionAndDoesNotAcceptClientAmount() {
        when(invoiceRepo.findForUpdate(1)).thenReturn(Optional.of(invoice));
        when(sessions.findByInvoiceMaHoaDonOrderByCreatedAtDesc(1)).thenReturn(List.of(session));
        var result = service.createSession(1);
        assertThat(result.sessionId()).isEqualTo(session.getId());
        assertThat(result.amount()).isEqualByComparingTo("1190000");
        verify(sessions, never()).save(any());
        verify(invoices).getInvoiceById(1);
    }
    @Test void newSessionSnapshotsReceiverAndReturnsEncodedQr() {
        when(invoiceRepo.findForUpdate(1)).thenReturn(Optional.of(invoice));
        var result = service.createSession(1);
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.transferContent()).matches("GAR[A-F0-9]{24}");
        assertThat(result.qrImageUrl()).startsWith("https://vietqr.app/img?").contains("amount=1190000");
        verify(sessions).save(any());
    }
    @Test void foreignCustomerOrBranchStopsBeforePersistence() {
        when(invoices.getInvoiceById(1)).thenThrow(new AccessDeniedException("Denied"));
        assertThatThrownBy(() -> service.createSession(1)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.getSession(1, session.getId())).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.options(1)).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(invoiceRepo, sessions, payments);
    }
    @Test void reviewBlocksAdditionalTransferSession() {
        when(invoiceRepo.findForUpdate(1)).thenReturn(Optional.of(invoice));
        session.setStatus("REQUIRES_REVIEW");
        when(sessions.findByInvoiceMaHoaDonOrderByCreatedAtDesc(1)).thenReturn(List.of(session));
        assertThatThrownBy(() -> service.createSession(1)).isInstanceOf(BadRequestException.class);
    }
    @Test void expiredStatusHidesQr() {
        when(sessions.findById(session.getId())).thenReturn(Optional.of(session));
        session.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1));
        var response = service.getSession(1, session.getId());
        assertThat(response.status()).isEqualTo("EXPIRED");
        assertThat(response.qrImageUrl()).isNull();
    }
    @Test void disabledConfigurationReturnsNoBankDetails() {
        config.setEnabled(false);
        InvoiceResponse response = new InvoiceResponse(); response.setTrangThai("CHUA_THANH_TOAN"); response.setConLai(BigDecimal.TEN);
        when(invoices.getInvoiceById(1)).thenReturn(response);
        var result = service.options(1);
        assertThat(result.available()).isFalse(); assertThat(result.receivers()).isEmpty();
    }
}
