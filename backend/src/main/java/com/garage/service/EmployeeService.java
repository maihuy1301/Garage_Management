package com.garage.service;

import com.garage.dto.*;
import com.garage.entity.ChiNhanh;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.NhanVien;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.ChiNhanhRepository;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.NhanVienRepository;
import com.garage.security.BranchAuthorizationService;
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
public class EmployeeService {

    private final NhanVienRepository nhanVienRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final ChiNhanhRepository chiNhanhRepository;
    private final NguoiDungVaiTroRepository nguoiDungVaiTroRepository;
    private final BranchAuthorizationService branchAuthorizationService;

    public EmployeeService(NhanVienRepository nhanVienRepository,
                           NguoiDungRepository nguoiDungRepository,
                           ChiNhanhRepository chiNhanhRepository,
                           NguoiDungVaiTroRepository nguoiDungVaiTroRepository,
                           BranchAuthorizationService branchAuthorizationService) {
        this.nhanVienRepository = nhanVienRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.chiNhanhRepository = chiNhanhRepository;
        this.nguoiDungVaiTroRepository = nguoiDungVaiTroRepository;
        this.branchAuthorizationService = branchAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isSystemAdmin(auth)) {
            return nhanVienRepository.findAll().stream()
                    .map(this::mapToEmployeeResponse)
                    .collect(Collectors.toList());
        }

        // Branch manager: query only employees of manager's branch
        Optional<Integer> branchIdOpt = branchAuthorizationService.resolveUserBranchId(auth);
        if (branchIdOpt.isEmpty()) {
            return Collections.emptyList();
        }

        return nhanVienRepository.findByChiNhanhMaChiNhanh(branchIdOpt.get()).stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Integer id) {
        NhanVien employee = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));

        validateBranchAccess(employee.getChiNhanh().getMaChiNhanh());
        return mapToEmployeeResponse(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        validateBranchAccess(request.getMaChiNhanh());

        if (nhanVienRepository.existsByMaNhanVienCode(request.getMaNhanVienCode())) {
            throw new DuplicateResourceException("Mã nhân viên '" + request.getMaNhanVienCode() + "' đã tồn tại");
        }

        if (nhanVienRepository.existsByNguoiDungMaNguoiDung(request.getMaNguoiDung())) {
            throw new DuplicateResourceException("Tài khoản người dùng ID " + request.getMaNguoiDung() + " đã được liên kết với nhân viên khác");
        }

        NguoiDung user = nguoiDungRepository.findById(request.getMaNguoiDung())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getMaNguoiDung()));

        ChiNhanh branch = chiNhanhRepository.findById(request.getMaChiNhanh())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + request.getMaChiNhanh()));

        NhanVien employee = new NhanVien();
        employee.setMaNhanVienCode(request.getMaNhanVienCode());
        employee.setNguoiDung(user);
        employee.setChiNhanh(branch);
        employee.setChucVu(request.getChucVu());
        employee.setNgayVaoLam(request.getNgayVaoLam());
        employee.setTrangThai(true);

        NhanVien saved = nhanVienRepository.save(employee);
        return mapToEmployeeResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Integer id, UpdateEmployeeRequest request) {
        NhanVien employee = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));

        validateBranchAccess(employee.getChiNhanh().getMaChiNhanh());

        if (request.getMaChiNhanh() != null && !request.getMaChiNhanh().equals(employee.getChiNhanh().getMaChiNhanh())) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (!isSystemAdmin(auth)) {
                throw new AccessDeniedException("Forbidden: Quản lý chi nhánh không được phép chuyển nhân viên sang chi nhánh khác");
            }
            ChiNhanh newBranch = chiNhanhRepository.findById(request.getMaChiNhanh())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + request.getMaChiNhanh()));
            employee.setChiNhanh(newBranch);
        }

        if (request.getChucVu() != null) {
            employee.setChucVu(request.getChucVu());
        }
        if (request.getNgayVaoLam() != null) {
            employee.setNgayVaoLam(request.getNgayVaoLam());
        }

        NhanVien updated = nhanVienRepository.save(employee);
        return mapToEmployeeResponse(updated);
    }

    @Transactional
    public EmployeeResponse updateEmployeeStatus(Integer id, UpdateEmployeeStatusRequest request) {
        NhanVien employee = nhanVienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + id));

        validateBranchAccess(employee.getChiNhanh().getMaChiNhanh());

        employee.setTrangThai(request.getTrangThai());
        NhanVien updated = nhanVienRepository.save(employee);
        return mapToEmployeeResponse(updated);
    }

    // --- Helpers ---

    private void validateBranchAccess(Integer branchId) {
        if (!branchAuthorizationService.isAllowedBranch(branchId)) {
            throw new AccessDeniedException("Forbidden: Bạn không có quyền truy cập dữ liệu nhân viên thuộc chi nhánh này");
        }
    }

    private boolean isSystemAdmin(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private EmployeeResponse mapToEmployeeResponse(NhanVien nv) {
        NguoiDung user = nv.getNguoiDung();
        ChiNhanh branch = nv.getChiNhanh();

        List<String> roles = Collections.emptyList();
        if (user != null) {
            List<NguoiDungVaiTro> links = nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(user.getMaNguoiDung());
            roles = links.stream()
                    .map(l -> l.getVaiTro().getTenVaiTro())
                    .collect(Collectors.toList());
        }

        return new EmployeeResponse(
                nv.getMaNhanVien(),
                nv.getMaNhanVienCode(),
                nv.getChucVu(),
                nv.getNgayVaoLam(),
                nv.getTrangThai(),
                user != null ? user.getMaNguoiDung() : null,
                user != null ? user.getTenDangNhap() : null,
                user != null ? user.getHoTen() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getSoDienThoai() : null,
                roles,
                branch != null ? branch.getMaChiNhanh() : null,
                branch != null ? branch.getMaChiNhanhCode() : null,
                branch != null ? branch.getTenChiNhanh() : null
        );
    }
}
