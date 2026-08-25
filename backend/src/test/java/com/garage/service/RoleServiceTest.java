package com.garage.service;

import com.garage.dto.RoleResponse;
import com.garage.entity.VaiTro;
import com.garage.repository.VaiTroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private VaiTroRepository vaiTroRepository;

    @InjectMocks
    private RoleService roleService;

    private VaiTro createRole(Integer id, String name, String desc) {
        VaiTro role = new VaiTro();
        role.setMaVaiTro(id);
        role.setTenVaiTro(name);
        role.setMoTa(desc);
        return role;
    }

    @Test
    void getAllRoles_returnsMappedRoleResponses() {
        VaiTro r1 = createRole(1, "ROLE_ADMIN", "Quản trị viên toàn hệ thống");
        VaiTro r2 = createRole(2, "ROLE_MANAGER", "Quản lý chi nhánh");
        VaiTro r3 = createRole(3, "ROLE_FRONT_DESK", "Nhân viên tiếp nhận / lễ tân");
        VaiTro r4 = createRole(4, "ROLE_TECHNICIAN", "Kỹ thuật viên sửa chữa");
        VaiTro r5 = createRole(5, "ROLE_CUSTOMER", "Khách hàng");

        when(vaiTroRepository.findAll(any(Sort.class))).thenReturn(List.of(r1, r2, r3, r4, r5));

        List<RoleResponse> result = roleService.getAllRoles();

        assertThat(result).hasSize(5);
        assertThat(result.get(0).getTenVaiTro()).isEqualTo("ROLE_ADMIN");
        assertThat(result.get(1).getTenVaiTro()).isEqualTo("ROLE_MANAGER");
        assertThat(result.get(2).getTenVaiTro()).isEqualTo("ROLE_FRONT_DESK");
        assertThat(result.get(3).getTenVaiTro()).isEqualTo("ROLE_TECHNICIAN");
        assertThat(result.get(4).getTenVaiTro()).isEqualTo("ROLE_CUSTOMER");
    }
}
