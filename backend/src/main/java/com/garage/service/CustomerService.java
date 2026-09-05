package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.KhachHang;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.KhachHangRepository;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final KhachHangRepository khachHangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    public CustomerService(KhachHangRepository khachHangRepository,
                           NguoiDungRepository nguoiDungRepository,
                           NguoiDungVaiTroRepository nguoiDungVaiTroRepository) {
        this.khachHangRepository = khachHangRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCurrentCustomerProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        NguoiDung user = getAuthenticatedUser(auth);

        KhachHang customer = khachHangRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin hồ sơ khách hàng cho tài khoản hiện tại"));

        return mapToCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCurrentCustomerProfile(UpdateCustomerRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        NguoiDung user = getAuthenticatedUser(auth);

        KhachHang customer = khachHangRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin hồ sơ khách hàng cho tài khoản hiện tại"));

        return applyCustomerUpdate(customer, request);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return khachHangRepository.findAll().stream()
                .map(this::mapToCustomerResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Integer id) {
        KhachHang customer = khachHangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + id));

        validateOwnership(customer);
        return mapToCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (khachHangRepository.existsByNguoiDungMaNguoiDung(request.getMaNguoiDung())) {
            throw new DuplicateResourceException("Tài khoản người dùng ID " + request.getMaNguoiDung() + " đã được liên kết với khách hàng khác");
        }

        NguoiDung user = nguoiDungRepository.findById(request.getMaNguoiDung())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getMaNguoiDung()));

        KhachHang customer = new KhachHang();
        customer.setNguoiDung(user);
        customer.setDiaChi(request.getDiaChi());
        customer.setNgaySinh(request.getNgaySinh());

        KhachHang saved = khachHangRepository.save(customer);
        return mapToCustomerResponse(saved);
    }

    @Transactional
    public CustomerResponse updateCustomer(Integer id, UpdateCustomerRequest request) {
        KhachHang customer = khachHangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + id));

        validateOwnership(customer);
        return applyCustomerUpdate(customer, request);
    }

    @Transactional
    public CustomerResponse updateCustomerStatus(Integer id, UpdateCustomerStatusRequest request) {
        KhachHang customer = khachHangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + id));

        NguoiDung user = customer.getNguoiDung();
        if (user != null) {
            user.setTrangThai(request.getTrangThai());
            nguoiDungRepository.save(user);
        }

        return mapToCustomerResponse(customer);
    }

    // --- Private Helpers ---

    private void validateOwnership(KhachHang customer) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isSystemAdmin(auth)) {
            return;
        }

        NguoiDung authenticatedUser = getAuthenticatedUser(auth);
        if (customer.getNguoiDung() == null ||
                !customer.getNguoiDung().getMaNguoiDung().equals(authenticatedUser.getMaNguoiDung())) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập thông tin của khách hàng khác");
        }
    }

    private CustomerResponse applyCustomerUpdate(KhachHang customer, UpdateCustomerRequest request) {
        if (request.getDiaChi() != null) {
            customer.setDiaChi(request.getDiaChi());
        }
        if (request.getNgaySinh() != null) {
            customer.setNgaySinh(request.getNgaySinh());
        }

        NguoiDung user = customer.getNguoiDung();
        if (user != null) {
            if (request.getHoTen() != null && !request.getHoTen().isBlank()) {
                user.setHoTen(request.getHoTen());
            }
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                Optional<NguoiDung> existingEmail = nguoiDungRepository.findByEmail(request.getEmail());
                if (existingEmail.isPresent() && !existingEmail.get().getMaNguoiDung().equals(user.getMaNguoiDung())) {
                    throw new DuplicateResourceException("Email '" + request.getEmail() + "' đã được sử dụng");
                }
                user.setEmail(request.getEmail());
            }
            if (request.getSoDienThoai() != null) {
                user.setSoDienThoai(request.getSoDienThoai());
            }
            if (request.getAnhDaiDien() != null) {
                user.setAnhDaiDien(request.getAnhDaiDien());
            }
            nguoiDungRepository.save(user);
        }

        KhachHang updated = khachHangRepository.save(customer);
        return mapToCustomerResponse(updated);
    }

    private NguoiDung getAuthenticatedUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Forbidden: Yêu cầu xác thực tài khoản");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getNguoiDung();
        }

        String username = auth.getName();
        return nguoiDungRepository.findByTenDangNhapOrEmail(username, username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin tài khoản người dùng: " + username));
    }

    private boolean isSystemAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private CustomerResponse mapToCustomerResponse(KhachHang kh) {
        NguoiDung user = kh.getNguoiDung();

        List<String> roles = Collections.emptyList();
        if (user != null) {
            List<NguoiDungVaiTro> links = nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung());
            roles = links.stream()
                    .map(l -> l.getVaiTro().getTenVaiTro())
                    .collect(Collectors.toList());
        }

        return new CustomerResponse(
                kh.getMaKhachHang(),
                kh.getDiaChi(),
                kh.getNgaySinh(),
                user != null ? user.getMaNguoiDung() : null,
                user != null ? user.getTenDangNhap() : null,
                user != null ? user.getHoTen() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getSoDienThoai() : null,
                user != null ? user.getAnhDaiDien() : null,
                user != null ? user.getTrangThai() : null,
                user != null ? user.getNgayTao() : null,
                roles
        );
    }
}
