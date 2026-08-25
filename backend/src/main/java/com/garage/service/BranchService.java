package com.garage.service;

import com.garage.dto.BranchResponse;
import com.garage.entity.ChiNhanh;
import com.garage.exception.ResourceNotFoundException;
import com.garage.repository.ChiNhanhRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BranchService {

    private final ChiNhanhRepository chiNhanhRepository;

    public BranchService(ChiNhanhRepository chiNhanhRepository) {
        this.chiNhanhRepository = chiNhanhRepository;
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
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

    private BranchResponse mapToResponse(ChiNhanh b) {
        return new BranchResponse(
                b.getMaChiNhanh(),
                b.getMaChiNhanhCode(),
                b.getTenChiNhanh(),
                b.getDiaChi(),
                b.getSoDienThoai(),
                b.getEmail(),
                b.getTrangThai(),
                b.getNgayTao()
        );
    }
}
