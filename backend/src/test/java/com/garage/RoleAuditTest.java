package com.garage;

import com.garage.entity.NguoiDung;
import com.garage.entity.NguoiDungVaiTro;
import com.garage.entity.VaiTro;
import com.garage.repository.NguoiDungRepository;
import com.garage.repository.NguoiDungVaiTroRepository;
import com.garage.repository.VaiTroRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
public class RoleAuditTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private NguoiDungVaiTroRepository nguoiDungVaiTroRepository;

    @Test
    @Transactional
    @Commit
    public void executeRoleMigrationAndValidate() {
        System.out.println("=== 1. BEFORE MIGRATION: VAITRO ===");
        List<?> beforeRoles = entityManager.createNativeQuery("SELECT MaVaiTro, TenVaiTro, MoTa FROM VaiTro").getResultList();
        for (Object row : beforeRoles) {
            Object[] cols = (Object[]) row;
            System.out.println("MaVaiTro: " + cols[0] + " | TenVaiTro: '" + cols[1] + "' | MoTa: '" + cols[2] + "'");
        }

        System.out.println("\n=== 2. BEFORE MIGRATION: NGUOIDUNG_VAITRO ===");
        List<?> beforeMappings = entityManager.createNativeQuery(
            "SELECT u.MaNguoiDung, u.TenDangNhap, r.MaVaiTro, r.TenVaiTro " +
            "FROM NguoiDung_VaiTro uv " +
            "JOIN NguoiDung u ON uv.MaNguoiDung = u.MaNguoiDung " +
            "JOIN VaiTro r ON uv.MaVaiTro = r.MaVaiTro " +
            "ORDER BY u.MaNguoiDung"
        ).getResultList();
        for (Object row : beforeMappings) {
            Object[] cols = (Object[]) row;
            System.out.println("User " + cols[0] + " (" + cols[1] + ") -> Role " + cols[2] + " (" + cols[3] + ")");
        }

        System.out.println("\n=== 3. EXECUTING MIGRATION... ===");

        // Ensure 5 standard roles
        entityManager.createNativeQuery(
            "IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_ADMIN') " +
            "    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_ADMIN', N'Quản trị hệ thống'); " +
            "IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_MANAGER') " +
            "    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_MANAGER', N'Quản lý chi nhánh'); " +
            "IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_FRONT_DESK') " +
            "    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_FRONT_DESK', N'Nhân viên tiếp nhận'); " +
            "IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_TECHNICIAN') " +
            "    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_TECHNICIAN', N'Kỹ thuật viên'); " +
            "IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_CUSTOMER') " +
            "    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_CUSTOMER', N'Khách hàng');"
        ).executeUpdate();

        // Assign ROLE_ADMIN to 'admin' user if not assigned
        entityManager.createNativeQuery(
            "DECLARE @RoleAdminId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_ADMIN'); " +
            "DECLARE @AdminUserId INT = (SELECT MaNguoiDung FROM NguoiDung WHERE TenDangNhap = 'admin'); " +
            "IF @AdminUserId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = @AdminUserId AND MaVaiTro = @RoleAdminId) " +
            "BEGIN " +
            "    INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro) VALUES (@AdminUserId, @RoleAdminId); " +
            "END"
        ).executeUpdate();

        // Safe migration for SYSTEM_ADMIN -> ROLE_ADMIN
        entityManager.createNativeQuery(
            "DECLARE @RoleAdminId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_ADMIN'); " +
            "DECLARE @OldSystemAdminId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'SYSTEM_ADMIN'); " +
            "IF @OldSystemAdminId IS NOT NULL " +
            "BEGIN " +
            "    DELETE FROM NguoiDung_VaiTro WHERE MaVaiTro = @OldSystemAdminId AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleAdminId); " +
            "    UPDATE NguoiDung_VaiTro SET MaVaiTro = @RoleAdminId WHERE MaVaiTro = @OldSystemAdminId; " +
            "END"
        ).executeUpdate();

        // Safe migration for BRANCH_MANAGER -> ROLE_MANAGER
        entityManager.createNativeQuery(
            "DECLARE @RoleManagerId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_MANAGER'); " +
            "DECLARE @OldBranchManagerId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'BRANCH_MANAGER'); " +
            "IF @OldBranchManagerId IS NOT NULL " +
            "BEGIN " +
            "    DELETE FROM NguoiDung_VaiTro WHERE MaVaiTro = @OldBranchManagerId AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleManagerId); " +
            "    UPDATE NguoiDung_VaiTro SET MaVaiTro = @RoleManagerId WHERE MaVaiTro = @OldBranchManagerId; " +
            "END"
        ).executeUpdate();

        // Safe migration for RECEPTIONIST -> ROLE_FRONT_DESK
        entityManager.createNativeQuery(
            "DECLARE @RoleFrontDeskId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_FRONT_DESK'); " +
            "DECLARE @OldReceptionistId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'RECEPTIONIST'); " +
            "IF @OldReceptionistId IS NOT NULL " +
            "BEGIN " +
            "    DELETE FROM NguoiDung_VaiTro WHERE MaVaiTro = @OldReceptionistId AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleFrontDeskId); " +
            "    UPDATE NguoiDung_VaiTro SET MaVaiTro = @RoleFrontDeskId WHERE MaVaiTro = @OldReceptionistId; " +
            "END"
        ).executeUpdate();

        // Safe migration for TECHNICIAN -> ROLE_TECHNICIAN
        entityManager.createNativeQuery(
            "DECLARE @RoleTechnicianId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_TECHNICIAN'); " +
            "DECLARE @OldTechnicianId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'TECHNICIAN'); " +
            "IF @OldTechnicianId IS NOT NULL " +
            "BEGIN " +
            "    DELETE FROM NguoiDung_VaiTro WHERE MaVaiTro = @OldTechnicianId AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleTechnicianId); " +
            "    UPDATE NguoiDung_VaiTro SET MaVaiTro = @RoleTechnicianId WHERE MaVaiTro = @OldTechnicianId; " +
            "END"
        ).executeUpdate();

        // Safe migration for CUSTOMER -> ROLE_CUSTOMER
        entityManager.createNativeQuery(
            "DECLARE @RoleCustomerId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_CUSTOMER'); " +
            "DECLARE @OldCustomerId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'CUSTOMER'); " +
            "IF @OldCustomerId IS NOT NULL " +
            "BEGIN " +
            "    DELETE FROM NguoiDung_VaiTro WHERE MaVaiTro = @OldCustomerId AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleCustomerId); " +
            "    UPDATE NguoiDung_VaiTro SET MaVaiTro = @RoleCustomerId WHERE MaVaiTro = @OldCustomerId; " +
            "END"
        ).executeUpdate();

        // Delete 5 extra roles
        entityManager.createNativeQuery(
            "DELETE FROM VaiTro WHERE TenVaiTro IN ('SYSTEM_ADMIN', 'BRANCH_MANAGER', 'RECEPTIONIST', 'TECHNICIAN', 'CUSTOMER')"
        ).executeUpdate();

        System.out.println("\n=== 4. AFTER MIGRATION: VAITRO ===");
        List<?> afterRoles = entityManager.createNativeQuery("SELECT MaVaiTro, TenVaiTro, MoTa FROM VaiTro ORDER BY MaVaiTro").getResultList();
        for (Object row : afterRoles) {
            Object[] cols = (Object[]) row;
            System.out.println("MaVaiTro: " + cols[0] + " | TenVaiTro: '" + cols[1] + "' | MoTa: '" + cols[2] + "'");
        }
        System.out.println("Total roles count: " + afterRoles.size());

        System.out.println("\n=== 5. AFTER MIGRATION: NGUOIDUNG_VAITRO ===");
        List<?> afterMappings = entityManager.createNativeQuery(
            "SELECT u.MaNguoiDung, u.TenDangNhap, r.MaVaiTro, r.TenVaiTro " +
            "FROM NguoiDung_VaiTro uv " +
            "JOIN NguoiDung u ON uv.MaNguoiDung = u.MaNguoiDung " +
            "JOIN VaiTro r ON uv.MaVaiTro = r.MaVaiTro " +
            "ORDER BY u.MaNguoiDung, r.MaVaiTro"
        ).getResultList();
        for (Object row : afterMappings) {
            Object[] cols = (Object[]) row;
            System.out.println("User " + cols[0] + " (" + cols[1] + ") -> Role " + cols[2] + " (" + cols[3] + ")");
        }
        System.out.println("Total user-role mappings: " + afterMappings.size());

        System.out.println("\n=== 6. CHECK FOR ORPHANS OR EXTRA ROLES ===");
        List<?> extraRoleRefs = entityManager.createNativeQuery(
            "SELECT uv.MaNguoiDung, uv.MaVaiTro FROM NguoiDung_VaiTro uv WHERE uv.MaVaiTro NOT IN (SELECT MaVaiTro FROM VaiTro)"
        ).getResultList();
        System.out.println("Orphan user-role references: " + extraRoleRefs.size());
    }
}
