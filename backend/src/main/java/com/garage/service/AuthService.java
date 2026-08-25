package com.garage.service;

import com.garage.dto.LoginRequest;
import com.garage.dto.LoginResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(NguoiDungRepository nguoiDungRepository,
                       NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        NguoiDung nguoiDung = nguoiDungRepository.findByTenDangNhapOrEmail(request.getTenDangNhap(), request.getTenDangNhap())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getMatKhau(), nguoiDung.getMatKhauHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!Boolean.TRUE.equals(nguoiDung.getTrangThai())) {
            throw new DisabledException("Tài khoản đã bị khóa hoặc ngưng hoạt động");
        }

        List<NguoiDungVaiTro> userRoles = nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(nguoiDung.getMaNguoiDung());
        List<String> roleNames = userRoles.stream()
                .map(vr -> vr.getVaiTro().getTenVaiTro())
                .collect(Collectors.toList());

        String token = jwtService.generateToken(nguoiDung.getTenDangNhap(), roleNames);
        return new LoginResponse(token, nguoiDung.getMaNguoiDung(), nguoiDung.getTenDangNhap(), nguoiDung.getHoTen());
    }
}
