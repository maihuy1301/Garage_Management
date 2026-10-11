package com.garage.service;

import com.garage.dto.RepairChatDtos.Send;
import com.garage.entity.RepairChatMessage;
import com.garage.exception.BadRequestException;
import com.garage.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

/** Opt-in SQL Server tests. Every message and simulated handover is rolled back. */
@SpringBootTest(properties = "GARAGE_REPAIR_CHAT_ENABLED=true")
@EnabledIfEnvironmentVariable(named = "REPAIR_CHAT_LOCAL_INTEGRATION", matches = "true")
@Transactional
class RepairChatLocalIntegrationTest {
    @Autowired RepairChatService chat;
    @Autowired PhieuSuaChuaRepository repairs;
    @Autowired PhieuTiepNhanRepository receptions;
    @Autowired NguoiDungRepository users;
    @Autowired RepairChatMessageRepository messages;
    @Autowired RepairChatRoomRepository rooms;
    @Autowired EntityManager entityManager;
    @Autowired NhanVienRepository employees;
    @Autowired PhanCongRepository assignments;
    @Autowired ThongBaoRepository notifications;
    private Integer orderId;
    private Integer userId;

    @BeforeEach void fixture() {
        var ids = entityManager.createNativeQuery("""
                SELECT TOP 1 p.MaPhieuSuaChua FROM PhieuSuaChua p
                JOIN PhieuTiepNhan r ON r.MaTiepNhan=p.MaTiepNhan
                WHERE r.TrangThai NOT IN ('DA_BAN_GIAO','HOAN_TAT','HUY')
                  AND p.TrangThai <> 'HUY'
                  AND EXISTS (SELECT 1 FROM PhanCong a WHERE a.MaPhieuSuaChua=p.MaPhieuSuaChua AND a.TrangThai='DA_DUYET')
                  AND NOT EXISTS (SELECT 1 FROM PhongChatSuaChua c WHERE c.MaTiepNhan=r.MaTiepNhan)
                ORDER BY p.MaPhieuSuaChua
                """).getResultList();
        assertThat(ids).as("Need one assigned local repair without an existing chat").isNotEmpty();
        orderId = ((Number) ids.get(0)).intValue();
        var adminIds = entityManager.createNativeQuery("""
                SELECT TOP 1 u.MaNguoiDung FROM NguoiDung u
                JOIN NguoiDung_VaiTro uv ON uv.MaNguoiDung=u.MaNguoiDung
                JOIN VaiTro v ON v.MaVaiTro=uv.MaVaiTro
                WHERE u.TrangThai=1 AND v.TenVaiTro='ROLE_ADMIN'
                """).getResultList();
        assertThat(adminIds).as("Need an active local admin").isNotEmpty();
        userId = ((Number) adminIds.get(0)).intValue();
        var user = users.findById(userId).orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                user.getTenDangNhap(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
    @AfterEach void clearAuth() { SecurityContextHolder.clearContext(); }

    @Test void unicodeRetryPaginationArchiveAndDeleteUseRealSql() {
        var first = chat.send(orderId, new Send("Kiểm tra phanh — tiếp tân xác nhận", "sql_test_first"));
        entityManager.flush(); entityManager.clear();
        var retry = chat.send(orderId, new Send("Kiểm tra phanh — tiếp tân xác nhận", "sql_test_first"));
        assertThat(retry.id()).isEqualTo(first.id());
        var second = chat.send(orderId, new Send("Đã kiểm tra xong", "sql_test_second"));
        entityManager.clear();
        var history = chat.history(orderId, null, null);
        assertThat(history.messages()).extracting(m -> m.content()).containsExactly(
                "Kiểm tra phanh — tiếp tân xác nhận", "Đã kiểm tra xong");
        assertThat(chat.history(orderId, second.id(), null).messages()).hasSize(1);
        assertThat(chat.history(orderId, null, first.id()).messages()).hasSize(1);
        var reception = repairs.findById(orderId).orElseThrow().getPhieuTiepNhan();
        reception.setTrangThai("DA_BAN_GIAO"); receptions.saveAndFlush(reception);
        assertThat(chat.history(orderId, null, null).room().canSend()).isFalse();
        chat.delete(orderId); entityManager.flush(); entityManager.clear();
        var deleted = chat.history(orderId, null, null);
        assertThat(deleted.room().state()).isEqualTo("DELETED"); assertThat(deleted.messages()).isEmpty();
        assertThat(messages.older(deleted.room().receptionId(), Long.MAX_VALUE, org.springframework.data.domain.PageRequest.of(0, 40))).isEmpty();
        assertThat(rooms.findById(deleted.room().receptionId()).orElseThrow().getDeletedBy()).isEqualTo(userId);
    }

    @Test void handoverPreventsNewMessage() {
        chat.send(orderId, new Send("Trước bàn giao", "sql_before_handover"));
        var reception = repairs.findById(orderId).orElseThrow().getPhieuTiepNhan();
        reception.setTrangThai("DA_BAN_GIAO"); receptions.saveAndFlush(reception); entityManager.clear();
        assertThatThrownBy(() -> chat.send(orderId, new Send("Sau bàn giao", "sql_after_handover")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test void technicianMessagePersistsOneNotificationPerReceptionistAndRetryDoesNotDuplicate() {
        var order = repairs.findById(orderId).orElseThrow();
        var staff = employees.findActiveReceptionists(order.getChiNhanh().getMaChiNhanh());
        assertThat(staff).as("Local fixture needs an active receptionist in the assigned branch").isNotEmpty();
        var counts = new java.util.HashMap<Integer, Integer>();
        staff.forEach(e -> counts.put(e.getNguoiDung().getMaNguoiDung(), notifications.findByNguoiDungMaNguoiDungOrderByNgayTaoDesc(e.getNguoiDung().getMaNguoiDung()).size()));
        var tech = assignments.findByPhieuSuaChuaMaPhieuSuaChuaAndTrangThai(orderId, "DA_DUYET").get(0).getNhanVienDuocPhanCong().getNguoiDung();
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_TECHNICIAN"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new com.garage.security.CustomUserDetails(tech, authorities), null, authorities));
        var sent = chat.send(orderId, new Send("SQL notification rollback only", "sql_notification_retry"));
        var retried = chat.send(orderId, new Send("SQL notification rollback only", "sql_notification_retry"));
        assertThat(retried.id()).isEqualTo(sent.id());
        entityManager.flush(); entityManager.clear();
        for (var id : counts.keySet()) {
            var found = notifications.findByNguoiDungMaNguoiDungOrderByNgayTaoDesc(id);
            assertThat(found).hasSize(counts.get(id) + 1);
            assertThat(found).anySatisfy(n -> {
                assertThat(n.getLoaiThongBao()).isEqualTo("REPAIR_CHAT_MESSAGE");
                assertThat(n.getNoiDung()).doesNotContain("SQL notification rollback only");
            });
        }
    }

    @Test void databaseUniqueConstraintRejectsDuplicateRetryKey() {
        chat.send(orderId, new Send("Tin gốc", "sql_unique_retry"));
        var duplicate = new RepairChatMessage();
        duplicate.setReceptionId(repairs.findById(orderId).orElseThrow().getPhieuTiepNhan().getMaTiepNhan());
        duplicate.setSender(users.findById(userId).orElseThrow()); duplicate.setSenderRole("ADMIN");
        duplicate.setClientId("sql_unique_retry"); duplicate.setContent("Nội dung trùng mã"); duplicate.setSentAt(LocalDateTime.now());
        assertThatThrownBy(() -> messages.saveAndFlush(duplicate)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
}
