USE GarageManagementSystem;
GO

-- ============================================================
-- V02__restore_original_roles.sql
-- Migration to restore 5 original roles and remove 5 extra roles.
-- ============================================================

-- 1. Ensure 5 standard roles exist
IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_ADMIN')
    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_ADMIN', N'Quản trị hệ thống');

IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_MANAGER')
    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_MANAGER', N'Quản lý chi nhánh');

IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_FRONT_DESK')
    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_FRONT_DESK', N'Nhân viên tiếp nhận');

IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_TECHNICIAN')
    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_TECHNICIAN', N'Kỹ thuật viên');

IF NOT EXISTS (SELECT 1 FROM VaiTro WHERE TenVaiTro = 'ROLE_CUSTOMER')
    INSERT INTO VaiTro (TenVaiTro, MoTa) VALUES ('ROLE_CUSTOMER', N'Khách hàng');
GO

-- 2. Resolve Role IDs by TenVaiTro
DECLARE @RoleAdminId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_ADMIN');
DECLARE @RoleManagerId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_MANAGER');
DECLARE @RoleFrontDeskId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_FRONT_DESK');
DECLARE @RoleTechnicianId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_TECHNICIAN');
DECLARE @RoleCustomerId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'ROLE_CUSTOMER');

DECLARE @OldSystemAdminId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'SYSTEM_ADMIN');
DECLARE @OldBranchManagerId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'BRANCH_MANAGER');
DECLARE @OldReceptionistId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'RECEPTIONIST');
DECLARE @OldTechnicianId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'TECHNICIAN');
DECLARE @OldCustomerId INT = (SELECT MaVaiTro FROM VaiTro WHERE TenVaiTro = 'CUSTOMER');

-- 3. Assign ROLE_ADMIN to 'admin' user if not assigned
DECLARE @AdminUserId INT = (SELECT MaNguoiDung FROM NguoiDung WHERE TenDangNhap = 'admin');
IF @AdminUserId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = @AdminUserId AND MaVaiTro = @RoleAdminId)
BEGIN
    INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro) VALUES (@AdminUserId, @RoleAdminId);
END

-- 4. Safe migration for user-roles from old roles to standard roles
-- A. SYSTEM_ADMIN -> ROLE_ADMIN
IF @OldSystemAdminId IS NOT NULL
BEGIN
    DELETE FROM NguoiDung_VaiTro
    WHERE MaVaiTro = @OldSystemAdminId
      AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleAdminId);

    UPDATE NguoiDung_VaiTro
    SET MaVaiTro = @RoleAdminId
    WHERE MaVaiTro = @OldSystemAdminId;
END

-- B. BRANCH_MANAGER -> ROLE_MANAGER
IF @OldBranchManagerId IS NOT NULL
BEGIN
    DELETE FROM NguoiDung_VaiTro
    WHERE MaVaiTro = @OldBranchManagerId
      AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleManagerId);

    UPDATE NguoiDung_VaiTro
    SET MaVaiTro = @RoleManagerId
    WHERE MaVaiTro = @OldBranchManagerId;
END

-- C. RECEPTIONIST -> ROLE_FRONT_DESK
IF @OldReceptionistId IS NOT NULL
BEGIN
    DELETE FROM NguoiDung_VaiTro
    WHERE MaVaiTro = @OldReceptionistId
      AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleFrontDeskId);

    UPDATE NguoiDung_VaiTro
    SET MaVaiTro = @RoleFrontDeskId
    WHERE MaVaiTro = @OldReceptionistId;
END

-- D. TECHNICIAN -> ROLE_TECHNICIAN
IF @OldTechnicianId IS NOT NULL
BEGIN
    DELETE FROM NguoiDung_VaiTro
    WHERE MaVaiTro = @OldTechnicianId
      AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleTechnicianId);

    UPDATE NguoiDung_VaiTro
    SET MaVaiTro = @RoleTechnicianId
    WHERE MaVaiTro = @OldTechnicianId;
END

-- E. CUSTOMER -> ROLE_CUSTOMER
IF @OldCustomerId IS NOT NULL
BEGIN
    DELETE FROM NguoiDung_VaiTro
    WHERE MaVaiTro = @OldCustomerId
      AND MaNguoiDung IN (SELECT MaNguoiDung FROM NguoiDung_VaiTro WHERE MaVaiTro = @RoleCustomerId);

    UPDATE NguoiDung_VaiTro
    SET MaVaiTro = @RoleCustomerId
    WHERE MaVaiTro = @OldCustomerId;
END

-- 5. Delete the 5 extra roles from VaiTro
DELETE FROM VaiTro WHERE TenVaiTro IN ('SYSTEM_ADMIN', 'BRANCH_MANAGER', 'RECEPTIONIST', 'TECHNICIAN', 'CUSTOMER');
GO
