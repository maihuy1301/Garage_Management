package com.garage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garage.dto.SupportChatDtos.*;
import com.garage.entity.*;
import com.garage.repository.*;
import com.garage.exception.BadRequestException;
import org.junit.jupiter.api.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupportBookingDraftsTest {
    XeRepository vehicles = mock(XeRepository.class);
    ChiNhanhRepository branches = mock(ChiNhanhRepository.class);
    ServiceCatalogService catalog = mock(ServiceCatalogService.class);
    SupportBookingDrafts drafts = new SupportBookingDrafts(new ObjectMapper().findAndRegisterModules(),vehicles,branches,catalog);
    SupportConversation room = new SupportConversation();
    Xe car = new Xe();
    @BeforeEach void setup() {
        var user = new NguoiDung(); user.setMaNguoiDung(10); room.setCustomer(user);
        var customer = new KhachHang(); customer.setNguoiDung(user); car.setKhachHang(customer); car.setTrangThai(true); car.setBienSo("TEST-01");
        when(vehicles.findById(5)).thenReturn(Optional.of(car));
        var branch = new ChiNhanh(); branch.setTrangThai(true); branch.setTenChiNhanh("Gara thử nghiệm");
        when(branches.findById(1)).thenReturn(Optional.of(branch));
        when(catalog.getAllServices(true,null)).thenReturn(List.of());
    }
    BookingDraft complete() { return new BookingDraft(5,1,SupportBookingDrafts.now().plusDays(1),List.of(),"Kiểm tra xe"); }
    @Test void roundTripPreservesServerSummaryAndConfirmationRequest() {
        String content = drafts.prepare(room,new BotAnswer("AI claims booked",false,complete()));
        var stored = drafts.decode(content);
        assertTrue(stored.ready()); assertFalse(stored.text().contains("AI claims booked"));
        assertTrue(stored.text().contains("Chưa tạo lịch")); assertTrue(stored.text().contains("TEST-01"));
        assertEquals(5,drafts.request(stored).getMaXe()); assertNull(drafts.request(stored).getMaKhachHang());
    }
    @Test void missingTimeProducesQuestionNotConfirmableDraft() {
        var stored = drafts.decode(drafts.prepare(room,new BotAnswer("ready",false,new BookingDraft(5,1,null,List.of(),"Kiểm tra"))));
        assertFalse(stored.ready()); assertTrue(stored.text().contains("lúc mấy giờ"));
        assertThrows(BadRequestException.class,()->drafts.request(stored));
    }
    @Test void otherCustomersVehicleRejected() {
        var other = new NguoiDung(); other.setMaNguoiDung(99); car.getKhachHang().setNguoiDung(other);
        assertThrows(BadRequestException.class,()->drafts.prepare(room,new BotAnswer("",false,complete())));
    }
    @Test void expiredAndPastDatesRejected() {
        assertThrows(BadRequestException.class,()->drafts.request(new SupportBookingDrafts.Stored("",complete(),true,SupportBookingDrafts.now().minusSeconds(1))));
        assertThrows(BadRequestException.class,()->drafts.prepare(room,new BotAnswer("",false,new BookingDraft(5,1,SupportBookingDrafts.now().minusDays(1),List.of(),"Kiểm tra"))));
    }
    @Test void fabricatedServiceRejected() {
        assertThrows(BadRequestException.class,()->drafts.prepare(room,new BotAnswer("",false,new BookingDraft(5,1,SupportBookingDrafts.now().plusDays(1),List.of(999),"Kiểm tra"))));
    }
    @Test void onlyUnambiguousWholeMessageConfirms() {
        assertTrue(SupportBookingDrafts.confirms("Đồng ý!"));
        assertFalse(SupportBookingDrafts.confirms("Đồng ý nhưng đổi sang 10 giờ"));
        assertFalse(SupportBookingDrafts.confirms("Tôi chưa đồng ý"));
    }
}
