package com.garage.service;

import com.garage.entity.*;
import com.garage.repository.*;
import com.garage.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SupportBranchService {
    public record Suggestion(Integer id, String name, String address, long bookingCount,
                             Double distanceKm, String reason) {}
    public record Result(List<Suggestion> branches, String explanation) {}
    private final ChiNhanhRepository branches;
    private final DatLichRepository appointments;
    private final KhachHangRepository customers;
    private final NguoiDungRepository users;
    private final boolean enabled;
    private final Map<Integer, double[]> coordinates = new HashMap<>();

    public SupportBranchService(ChiNhanhRepository branches, DatLichRepository appointments,
            KhachHangRepository customers, NguoiDungRepository users,
            @Value("${garage.support.enabled:false}") boolean enabled,
            @Value("${GARAGE_SUPPORT_BRANCH_COORDINATES:}") String configured) {
        this.branches = branches; this.appointments = appointments;
        this.customers = customers; this.users = users; this.enabled = enabled;
        // Explicit, verified garage coordinates only: branchId:latitude:longitude;...
        for (String entry : configured.split(";")) {
            if (entry.isBlank()) continue;
            try {
                var parts = entry.trim().split(":");
                if (parts.length != 3) throw new IllegalArgumentException();
                int id = Integer.parseInt(parts[0]);
                double lat = Double.parseDouble(parts[1]), lon = Double.parseDouble(parts[2]);
                if (id <= 0 || !valid(lat, lon) || coordinates.containsKey(id)) throw new IllegalArgumentException();
                coordinates.put(id, new double[]{lat, lon});
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("GARAGE_SUPPORT_BRANCH_COORDINATES must use branchId:latitude:longitude;... with valid coordinates");
            }
        }
    }

    @Transactional(readOnly = true)
    public Result recommend(Double latitude, Double longitude) {
        if (!enabled) throw new BadRequestException("Chat hỗ trợ chưa được bật");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) throw new AccessDeniedException("Chỉ khách hàng");
        var user = users.findByTenDangNhapOrEmail(auth.getName(), auth.getName())
                .filter(u -> Boolean.TRUE.equals(u.getTrangThai())).orElseThrow(() -> new AccessDeniedException("Tài khoản không hoạt động"));
        return forCustomer(user.getMaNguoiDung(), latitude, longitude);
    }

    // Internal use by the bot worker. User identity comes from the persisted room, never the client.
    @Transactional(readOnly = true)
    public Result forCustomer(Integer userId, Double lat, Double lon) {
        if ((lat == null) != (lon == null) || (lat != null && !valid(lat, lon)))
            throw new BadRequestException("Vị trí không hợp lệ");
        var customer = customers.findByNguoiDungMaNguoiDung(userId)
                .orElseThrow(() -> new AccessDeniedException("Thiếu hồ sơ khách hàng"));
        var counts = new HashMap<Integer, Long>();
        var latest = new HashMap<Integer, LocalDateTime>();
        for (var a : appointments.findByKhachHangMaKhachHang(customer.getMaKhachHang())) {
            if (!Set.of("DA_XAC_NHAN", "DA_TIEP_NHAN", "HOAN_TAT").contains(Objects.toString(a.getTrangThai(), ""))) continue;
            int id = a.getChiNhanh().getMaChiNhanh();
            counts.merge(id, 1L, Long::sum);
            if (a.getThoiGianHen() != null) latest.merge(id, a.getThoiGianHen(), (x, y) -> x.isAfter(y) ? x : y);
        }
        var result = new ArrayList<Suggestion>();
        for (var b : branches.findByTrangThaiTrue()) {
            var point = coordinates.get(b.getMaChiNhanh());
            Double distance = lat != null && point != null ? distance(lat, lon, point[0], point[1]) : null;
            long count = counts.getOrDefault(b.getMaChiNhanh(), 0L);
            String reason = count > 0 ? "Bạn có " + count + " lịch đã xác nhận/tiếp nhận/hoàn tất tại đây" : "Chi nhánh đang hoạt động";
            result.add(new Suggestion(b.getMaChiNhanh(), b.getTenChiNhanh(), b.getDiaChi(), count, distance, reason));
        }
        result.sort(Comparator.comparing(Suggestion::distanceKm, Comparator.nullsLast(Double::compareTo))
                .thenComparing(Comparator.comparingLong(Suggestion::bookingCount).reversed())
                .thenComparing(s -> latest.getOrDefault(s.id(), LocalDateTime.MIN), Comparator.reverseOrder())
                .thenComparing(Suggestion::id));
        long mapped = result.stream().filter(s -> s.distanceKm() != null).count();
        String explanation = lat == null ? "Gợi ý theo lịch đặt đã xác nhận của bạn; bạn vẫn có thể chọn chi nhánh khác."
                : mapped == 0 ? "Gara chưa có tọa độ xác minh nên chưa tính được khoảng cách. Bạn có thể chọn theo địa chỉ hoặc lịch đặt trước đây."
                : "Sắp xếp theo khoảng cách đường thẳng, không phải quãng đường lái xe."
                    + (mapped < result.size() ? " Một số gara chưa có tọa độ, chưa thể xác định gần nhất trong toàn bộ hệ thống." : "");
        return new Result(List.copyOf(result), explanation);
    }
    private static boolean valid(double lat, double lon) { return Double.isFinite(lat) && Double.isFinite(lon) && Math.abs(lat) <= 90 && Math.abs(lon) <= 180; }
    static double distance(double a, double b, double c, double d) {
        double x = Math.sin(Math.toRadians(c-a)/2), y = Math.sin(Math.toRadians(d-b)/2);
        double h = x*x + Math.cos(Math.toRadians(a))*Math.cos(Math.toRadians(c))*y*y;
        return 6371.0088 * 2 * Math.asin(Math.sqrt(Math.min(1, Math.max(0, h))));
    }
}
