package com.garage.service;

import com.garage.config.SePayProperties;
import com.garage.dto.SePayWebhookRequest;
import com.garage.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

/** Opt-in local SQL Server verification. All session/payment changes roll back. */
@SpringBootTest(properties = {
        "payments.sepay.enabled=true", "payments.sepay.environment=test",
        "payments.sepay.webhook-key=test-only-key-with-at-least-32-characters",
        "payments.sepay.bank-code=TEST", "payments.sepay.bank-name=TestBank",
        "payments.sepay.account-number=000000", "payments.sepay.account-name=TEST ONLY"
})
@EnabledIfEnvironmentVariable(named = "SEPAY_LOCAL_INTEGRATION", matches = "true")
@Transactional
class SePayLocalIntegrationTest {
    @Autowired SePayService service;
    @Autowired InvoiceService invoices;
    @Autowired HoaDonRepository invoiceRepo;
    @Autowired ThanhToanRepository payments;
    @Autowired NguoiDungRepository users;
    @Autowired SePayProperties config;
    @Autowired GiaoDichSePayRepository receipts;
    @Autowired jakarta.persistence.EntityManager entityManager;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;

    @BeforeEach void loginFixtureCustomer() {
        var invoice = invoiceRepo.findByPhieuSuaChuaMaPhieuSuaChua(2002).orElseThrow();
        assertThat(invoice.getKhachHang().getMaKhachHang()).isEqualTo(1001);
        assertThat(invoice.getTrangThai()).isEqualTo("CHUA_THANH_TOAN");
        var username = users.findById(1001).orElseThrow().getTenDangNhap();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                username, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))));
    }
    @AfterEach void clearAuth() { SecurityContextHolder.clearContext(); }

    @Test void assignedProviderIdCannotOverwriteExistingReceipt() {
        service.receiveWebhook(new SePayWebhookRequest(999999998L, "TestBank", "000000", null,
                "UNMATCHED", "in", java.math.BigDecimal.TEN, "LOCAL-ROLLBACK-ONLY"));
        entityManager.clear();
        var duplicate = new com.garage.entity.GiaoDichSePay("SEPAY:test:999999998", null,
                java.math.BigDecimal.ONE, "TestBank", "000000", "changed", null, "APPLIED",
                java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
        assertThatThrownBy(() -> receipts.saveAndFlush(duplicate))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void invoiceLockSerializesIndependentTransactionsWithoutChangingData() throws Exception {
        Integer id = invoiceRepo.findByPhieuSuaChuaMaPhieuSuaChua(2002).orElseThrow().getMaHoaDon();
        var firstLocked = new java.util.concurrent.CountDownLatch(1);
        var secondStarted = new java.util.concurrent.CountDownLatch(1);
        var releaseFirst = new java.util.concurrent.CountDownLatch(1);
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        try {
            var first = executor.submit(() -> transaction.execute(status -> {
                status.setRollbackOnly(); invoiceRepo.findForUpdate(id).orElseThrow(); firstLocked.countDown();
                try { if (!releaseFirst.await(10, java.util.concurrent.TimeUnit.SECONDS)) throw new IllegalStateException("timeout"); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
                return true;
            }));
            assertThat(firstLocked.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> transaction.execute(status -> {
                status.setRollbackOnly(); secondStarted.countDown(); invoiceRepo.findForUpdate(id).orElseThrow(); return true;
            }));
            assertThat(secondStarted.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            assertThatThrownBy(() -> second.get(300, java.util.concurrent.TimeUnit.MILLISECONDS))
                    .isInstanceOf(java.util.concurrent.TimeoutException.class);
            releaseFirst.countDown();
            assertThat(first.get(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            assertThat(second.get(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        } finally { releaseFirst.countDown(); executor.shutdownNow(); }
    }
    @Test void realSqlRoundTripAndReplayAreAtomicAndRollback() {
        var id = invoiceRepo.findByPhieuSuaChuaMaPhieuSuaChua(2002).orElseThrow().getMaHoaDon();
        assertThat(service.options(id).available()).isTrue();
        var first = service.createSession(id);
        assertThat(service.createSession(id).sessionId()).isEqualTo(first.sessionId());
        service.authenticateWebhook("Apikey " + config.getWebhookKey());
        var webhook = new SePayWebhookRequest(999999999L, "TestBank", "000000", first.transferContent(),
                first.transferContent(), "in", first.amount(), "LOCAL-ROLLBACK-ONLY");
        service.receiveWebhook(webhook);
        service.receiveWebhook(webhook);
        var status = service.getSession(id, first.sessionId());
        assertThat(status.status()).isEqualTo("SUCCEEDED");
        assertThat(status.remainingAmount()).isZero();
        assertThat(invoices.getInvoiceById(id).getTrangThai()).isEqualTo("DA_THANH_TOAN");
        assertThat(payments.findByHoaDonMaHoaDon(id)).hasSize(1);
    }
}
