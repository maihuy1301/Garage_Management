package com.garage;

import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.VaiTroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RoleAuditTest {

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @Test
    @Transactional
    public void executeRoleMigrationAndValidate() {
        // Ensure 5 standard roles
        Set<String> standardRoles = Set.of(
                "ROLE_ADMIN", "ROLE_MANAGER", "ROLE_FRONT_DESK", "ROLE_TECHNICIAN", "ROLE_CUSTOMER"
        );

        for (String roleName : standardRoles) {
            if (vaiTroRepository.findByTenVaiTro(roleName).isEmpty()) {
                VaiTro r = new VaiTro();
                r.setTenVaiTro(roleName);
                r.setMoTa(roleName);
                vaiTroRepository.save(r);
            }
        }

        List<VaiTro> allRoles = vaiTroRepository.findAll();
        assertThat(allRoles).isNotEmpty();
    }
}
