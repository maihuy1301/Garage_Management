package com.garage.security;

import com.garage.dto.RoleResponse;
import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.VaiTroRepository;
import com.garage.service.RoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RoleSystemEndToEndRegressionTest {

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        if (vaiTroRepository.count() == 0) {
            VaiTro r1 = new VaiTro(); r1.setTenVaiTro("ROLE_ADMIN"); vaiTroRepository.save(r1);
            VaiTro r2 = new VaiTro(); r2.setTenVaiTro("ROLE_MANAGER"); vaiTroRepository.save(r2);
            VaiTro r3 = new VaiTro(); r3.setTenVaiTro("ROLE_FRONT_DESK"); vaiTroRepository.save(r3);
            VaiTro r4 = new VaiTro(); r4.setTenVaiTro("ROLE_TECHNICIAN"); vaiTroRepository.save(r4);
            VaiTro r5 = new VaiTro(); r5.setTenVaiTro("ROLE_CUSTOMER"); vaiTroRepository.save(r5);
        }
        if (nguoiDungRepository.findByTenDangNhap("admin").isEmpty()) {
            NguoiDung admin = new NguoiDung();
            admin.setTenDangNhap("admin");
            admin.setHoTen("System Admin");
            admin.setEmail("admin@garage.com");
            admin.setMatKhauHash("$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO");
            admin.setMaPinHash("$2a$10$hashedPinValue");
            admin.setTrangThai(true);
            admin = nguoiDungRepository.save(admin);

            VaiTro adminRole = vaiTroRepository.findByTenVaiTro("ROLE_ADMIN").orElseThrow();
            nguoiDungVaiTroRepository.save(new NguoiDungVaiTro(admin, adminRole));
        }
    }

    @Test
    @DisplayName("1. Verify Database contains exactly 5 standard Roles and no obsolete roles")
    void testDatabaseRolesIntegrity() {
        List<VaiTro> allRoles = vaiTroRepository.findAll();
        assertThat(allRoles).hasSize(5);

        Set<String> roleNames = allRoles.stream()
                .map(VaiTro::getTenVaiTro)
                .collect(Collectors.toSet());

        Set<String> expectedRoles = Set.of(
                "ROLE_ADMIN",
                "ROLE_MANAGER",
                "ROLE_FRONT_DESK",
                "ROLE_TECHNICIAN",
                "ROLE_CUSTOMER"
        );

        assertThat(roleNames).isEqualTo(expectedRoles);

        // Verify none of the obsolete roles exist
        Set<String> obsoleteRoles = Set.of(
                "SYSTEM_ADMIN",
                "BRANCH_MANAGER",
                "RECEPTIONIST",
                "TECHNICIAN",
                "CUSTOMER",
                "ROLE_SYSTEM_ADMIN",
                "ROLE_BRANCH_MANAGER",
                "ROLE_RECEPTIONIST"
        );
        for (String obsolete : obsoleteRoles) {
            assertThat(roleNames).doesNotContain(obsolete);
        }
    }

    @Test
    @DisplayName("2. Verify NguoiDung_VaiTro integrity: admin has ROLE_ADMIN, no orphans")
    void testNguoiDungVaiTroIntegrity() {
        Optional<NguoiDung> adminOpt = nguoiDungRepository.findByTenDangNhap("admin");
        if (adminOpt.isPresent()) {
            NguoiDung admin = adminOpt.get();
            List<NguoiDungVaiTro> links = nguoiDungVaiTroRepository.findByNguoiDungMaNguoiDung(admin.getMaNguoiDung());
            assertThat(links).isNotEmpty();
            boolean hasAdminRole = links.stream()
                    .anyMatch(l -> "ROLE_ADMIN".equals(l.getVaiTro().getTenVaiTro()));
            assertThat(hasAdminRole).isTrue();
        }

        // Verify all links in NguoiDung_VaiTro point to existing valid roles
        List<NguoiDungVaiTro> allLinks = nguoiDungVaiTroRepository.findAll();
        List<Integer> validRoleIds = vaiTroRepository.findAll().stream()
                .map(VaiTro::getMaVaiTro)
                .toList();

        for (NguoiDungVaiTro link : allLinks) {
            assertThat(link.getVaiTro()).isNotNull();
            assertThat(validRoleIds).contains(link.getVaiTro().getMaVaiTro());
            assertThat(link.getVaiTro().getTenVaiTro()).startsWith("ROLE_");
        }
    }

    @Test
    @DisplayName("3. Verify CustomUserDetailsService yields clean GrantedAuthority (no ROLE_ROLE_*)")
    void testCustomUserDetailsServiceCleanAuthorities() {
        Optional<NguoiDung> adminOpt = nguoiDungRepository.findByTenDangNhap("admin");
        if (adminOpt.isPresent()) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");
            assertThat(userDetails).isNotNull();

            for (GrantedAuthority auth : userDetails.getAuthorities()) {
                String authority = auth.getAuthority();
                assertThat(authority).startsWith("ROLE_");
                assertThat(authority).doesNotStartWith("ROLE_ROLE_");
                assertThat(authority).isIn(
                        "ROLE_ADMIN",
                        "ROLE_MANAGER",
                        "ROLE_FRONT_DESK",
                        "ROLE_TECHNICIAN",
                        "ROLE_CUSTOMER"
                );
            }
        }
    }

    @Test
    @DisplayName("4. Verify JWT token generation and role claim resolution")
    void testJwtRoleClaims() {
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_MANAGER");
        String token = jwtService.generateToken("admin", roles);
        assertThat(token).isNotBlank();

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_ADMIN", "ROLE_MANAGER");
    }

    @Test
    @DisplayName("5. Verify RoleService.getAllRoles() returns exactly 5 standard roles")
    void testRoleServiceGetAllRoles() {
        List<RoleResponse> roleResponses = roleService.getAllRoles();
        assertThat(roleResponses).hasSize(5);

        List<String> roleNames = roleResponses.stream()
                .map(RoleResponse::getTenVaiTro)
                .toList();

        assertThat(roleNames).containsExactly(
                "ROLE_ADMIN",
                "ROLE_MANAGER",
                "ROLE_FRONT_DESK",
                "ROLE_TECHNICIAN",
                "ROLE_CUSTOMER"
        );
    }
}
