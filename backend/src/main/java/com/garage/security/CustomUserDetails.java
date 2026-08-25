package com.garage.security;

import com.garage.entity.NguoiDung;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final NguoiDung nguoiDung;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(NguoiDung nguoiDung) {
        this(nguoiDung, Collections.emptyList());
    }

    public CustomUserDetails(NguoiDung nguoiDung, Collection<? extends GrantedAuthority> authorities) {
        this.nguoiDung = nguoiDung;
        this.authorities = authorities != null ? authorities : Collections.emptyList();
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return nguoiDung.getMatKhauHash();
    }

    @Override
    public String getUsername() {
        return nguoiDung.getTenDangNhap();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(nguoiDung.getTrangThai());
    }
}
