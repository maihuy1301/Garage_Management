package com.garage.security;

import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    public CustomUserDetailsService(NguoiDungRepository nguoiDungRepository,
                                    NguoiDungVaiTroRepository nguoiDungVaiTroRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NguoiDung nguoiDung = nguoiDungRepository.findByTenDangNhapOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại: " + username));

        List<NguoiDungVaiTro> userRoles = nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(nguoiDung.getMaNguoiDung());
        List<GrantedAuthority> authorities = userRoles.stream()
                .map(vr -> {
                    String roleName = vr.getVaiTro().getTenVaiTro();
                    String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                    return new SimpleGrantedAuthority(authority);
                })
                .collect(Collectors.toList());

        return new CustomUserDetails(nguoiDung, authorities);
    }
}
