package com.garage.service;

import com.garage.entity.ThietBiPush;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.ThietBiPushRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
public class PushDeviceService {
    private final ThietBiPushRepository devices;
    private final NguoiDungRepository users;
    public PushDeviceService(ThietBiPushRepository devices, NguoiDungRepository users) {
        this.devices = devices;
        this.users = users;
    }

    @Transactional
    public void register(String token) {
        Integer userId = currentUserId();
        String hash = hash(token);
        ThietBiPush device = devices.findForUpdate(hash).orElseGet(ThietBiPush::new);
        device.setTokenHash(hash);
        device.setFcmToken(token);
        device.setMaNguoiDung(userId);
        device.setCapNhatLuc(LocalDateTime.now());
        devices.save(device);
    }

    @Transactional
    public void unregister(String token) {
        devices.deleteByTokenHashAndMaNguoiDung(hash(token), currentUserId());
    }

    @Transactional(readOnly = true)
    public List<ThietBiPush> forUser(Integer userId) {
        return devices.findByMaNguoiDungAndCapNhatLucAfter(userId, LocalDateTime.now().minusDays(60));
    }

    @Transactional
    public void removeInvalid(String token, Integer owner) {
        devices.deleteByTokenHashAndMaNguoiDung(hash(token), owner);
    }

    private Integer currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getAuthorities().stream().noneMatch(a -> "ROLE_CUSTOMER".equals(a.getAuthority()))) {
            throw new AccessDeniedException("Chỉ khách hàng được đăng ký thiết bị nhận thông báo");
        }
        return users.findByTenDangNhapOrEmail(auth.getName(), auth.getName())
                .filter(user -> !Boolean.FALSE.equals(user.getTrangThai()))
                .orElseThrow(() -> new AccessDeniedException("Tài khoản không hoạt động"))
                .getMaNguoiDung();
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
