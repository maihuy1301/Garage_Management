package com.garage.service;

import com.garage.dto.RoleResponse;
import com.garage.entity.VaiTro;
import com.garage.repository.VaiTroRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final VaiTroRepository vaiTroRepository;

    public RoleService(VaiTroRepository vaiTroRepository) {
        this.vaiTroRepository = vaiTroRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return vaiTroRepository.findAll(Sort.by(Sort.Direction.ASC, "maVaiTro")).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RoleResponse mapToResponse(VaiTro role) {
        return new RoleResponse(
                role.getMaVaiTro(),
                role.getTenVaiTro(),
                role.getMoTa()
        );
    }
}
