package com.garage.service;

import com.garage.dto.BranchResponse;
import com.garage.dto.CreateBranchRequest;
import com.garage.dto.UpdateBranchRequest;
import com.garage.dto.UpdateBranchStatusRequest;
import com.garage.entity.ChiNhanh;
import com.garage.exception.BadRequestException;
import com.garage.exception.DuplicateResourceException;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private final ChiNhanhRepository chiNhanhRepository;
    private final NhanVienRepository nhanVienRepository;
    private final DatLichRepository datLichRepository;
    private final PhieuTiepNhanRepository phieuTiepNhanRepository;
    private final PhieuSuaChuaRepository phieuSuaChuaRepository;
    private final HoaDonRepository hoaDonRepository;
    private final TonKhoRepository tonKhoRepository;

    public BranchService(ChiNhanhRepository chiNhanhRepository,
                         NhanVienRepository nhanVienRepository,
                         DatLichRepository datLichRepository,
                         PhieuTiepNhanRepository phieuTiepNhanRepository,
                         PhieuSuaChuaRepository phieuSuaChuaRepository,
                         HoaDonRepository hoaDonRepository,
                         TonKhoRepository tonKhoRepository) {
        this.chiNhanhRepository = chiNhanhRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.datLichRepository = datLichRepository;
        this.phieuTiepNhanRepository = phieuTiepNhanRepository;
        this.phieuSuaChuaRepository = phieuSuaChuaRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.tonKhoRepository = tonKhoRepository;
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        return getAllBranches(false);
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches(boolean includeInactive) {
        if (includeInactive && isSystemAdmin()) {
            return chiNhanhRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return chiNhanhRepository.findByTrangThaiTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(Integer id) {
        ChiNhanh branch = chiNhanhRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));
        return mapToResponse(branch);
    }

    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        if (chiNhanhRepository.existsByTenChiNhanh(request.getTenChiNhanh())) {
            throw new DuplicateResourceException("Tên chi nhánh '" + request.getTenChiNhanh() + "' đã tồn tại trong hệ thống");
        }

        ChiNhanh branch = new ChiNhanh();
        branch.setTenChiNhanh(request.getTenChiNhanh());
        branch.setDiaChi(request.getDiaChi());
        branch.setSoDienThoai(request.getSoDienThoai());
        branch.setEmail(request.getEmail());
        branch.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);

        ChiNhanh saved = chiNhanhRepository.save(branch);
        return mapToResponse(saved);
    }

    @Transactional
    public BranchResponse updateBranch(Integer id, UpdateBranchRequest request) {
        ChiNhanh branch = chiNhanhRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));

        if (chiNhanhRepository.existsByTenChiNhanhAndMaChiNhanhNot(request.getTenChiNhanh(), id)) {
            throw new DuplicateResourceException("Tên chi nhánh '" + request.getTenChiNhanh() + "' đã được sử dụng bởi chi nhánh khác");
        }

        branch.setTenChiNhanh(request.getTenChiNhanh());
        branch.setDiaChi(request.getDiaChi());
        branch.setSoDienThoai(request.getSoDienThoai());
        branch.setEmail(request.getEmail());
        if (request.getTrangThai() != null) {
            branch.setTrangThai(request.getTrangThai());
        }

        ChiNhanh updated = chiNhanhRepository.save(branch);
        return mapToResponse(updated);
    }

    @Transactional
    public BranchResponse updateBranchStatus(Integer id, UpdateBranchStatusRequest request) {
        ChiNhanh branch = chiNhanhRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));

        branch.setTrangThai(request.getTrangThai());
        ChiNhanh updated = chiNhanhRepository.save(branch);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteBranch(Integer id) {
        ChiNhanh branch = chiNhanhRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chi nhánh với ID: " + id));

        if (nhanVienRepository.existsByChiNhanhMaChiNhanh(id)
                || datLichRepository.existsByChiNhanhMaChiNhanh(id)
                || phieuTiepNhanRepository.existsByChiNhanhMaChiNhanh(id)
                || phieuSuaChuaRepository.existsByChiNhanhMaChiNhanh(id)
                || hoaDonRepository.existsByChiNhanhMaChiNhanh(id)
                || tonKhoRepository.existsByIdMaChiNhanh(id)) {
            throw new BadRequestException("Chi nhánh đang có dữ liệu ràng buộc liên quan (nhân viên, lịch hẹn, phiếu sửa chữa, hóa đơn, tồn kho...), không thể xóa cứng. Vui lòng chuyển trạng thái sang Ngưng hoạt động (Deactivate).");
        }

        chiNhanhRepository.delete(branch);
    }

    private boolean isSystemAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private BranchResponse mapToResponse(ChiNhanh b) {
        return new BranchResponse(
                b.getMaChiNhanh(),
                b.getTenChiNhanh(),
                b.getDiaChi(),
                b.getSoDienThoai(),
                b.getEmail(),
                b.getTrangThai(),
                b.getNgayTao()
        );
    }
}
