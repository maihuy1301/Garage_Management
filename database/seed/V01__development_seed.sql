USE GarageManagementSystem;
GO

-- ============================================================
-- V01__development_seed.sql
-- Development seed data — DO NOT run in production.
-- Password for all accounts: Password123@
-- BCrypt hash: $2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO
-- ============================================================

-- 1. CHI NHANH
IF NOT EXISTS (SELECT 1 FROM ChiNhanh WHERE Email = 'central@garage.com')
    INSERT INTO ChiNhanh (TenChiNhanh, DiaChi, SoDienThoai, Email, TrangThai)
    VALUES (N'Garage Central Chi Nhánh 1', N'123 Nguyễn Văn Cừ, Quận 5, TP.HCM', '0901234567', 'central@garage.com', 1);

IF NOT EXISTS (SELECT 1 FROM ChiNhanh WHERE Email = 'binhthanh@garage.com')
    INSERT INTO ChiNhanh (TenChiNhanh, DiaChi, SoDienThoai, Email, TrangThai)
    VALUES (N'Garage Chi Nhánh 2 Bình Thạnh', N'456 Đinh Bộ Lĩnh, Bình Thạnh, TP.HCM', '0901234568', 'binhthanh@garage.com', 1);
GO

-- 2. VAI TRO
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

-- 3. NGUOI DUNG (CN001 accounts)
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'admin')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('admin', '$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO', N'Hệ Thống Admin', 'admin@garage.com', '0900000001', 1);

IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'manager')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('manager', '$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO', N'Nguyễn Văn Quản Lý', 'manager@garage.com', '0900000002', 1);

IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'receptionist')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('receptionist', '$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO', N'Trần Thị Tiếp Nhận', 'receptionist@garage.com', '0900000003', 1);

IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'technician')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('technician', '$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO', N'Lê Văn Thợ Máy', 'technician@garage.com', '0900000004', 1);

IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'customer')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('customer', '$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO', N'Phạm Văn Khách Hàng', 'customer@garage.com', '0900000005', 1);

-- CN002 branch manager (for cross-branch test)
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'manager2')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('manager2', '$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO', N'Trần Văn Quản Lý CN2', 'manager2@garage.com', '0900000006', 1);
GO

-- 4. NGUOI DUNG - VAI TRO
INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro)
SELECT u.MaNguoiDung, r.MaVaiTro FROM NguoiDung u, VaiTro r
WHERE u.TenDangNhap = 'admin' AND r.TenVaiTro = 'ROLE_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = u.MaNguoiDung AND MaVaiTro = r.MaVaiTro);

INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro)
SELECT u.MaNguoiDung, r.MaVaiTro FROM NguoiDung u, VaiTro r
WHERE u.TenDangNhap = 'manager' AND r.TenVaiTro = 'ROLE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = u.MaNguoiDung AND MaVaiTro = r.MaVaiTro);

INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro)
SELECT u.MaNguoiDung, r.MaVaiTro FROM NguoiDung u, VaiTro r
WHERE u.TenDangNhap = 'receptionist' AND r.TenVaiTro = 'ROLE_FRONT_DESK'
  AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = u.MaNguoiDung AND MaVaiTro = r.MaVaiTro);

INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro)
SELECT u.MaNguoiDung, r.MaVaiTro FROM NguoiDung u, VaiTro r
WHERE u.TenDangNhap = 'technician' AND r.TenVaiTro = 'ROLE_TECHNICIAN'
  AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = u.MaNguoiDung AND MaVaiTro = r.MaVaiTro);

INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro)
SELECT u.MaNguoiDung, r.MaVaiTro FROM NguoiDung u, VaiTro r
WHERE u.TenDangNhap = 'customer' AND r.TenVaiTro = 'ROLE_CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = u.MaNguoiDung AND MaVaiTro = r.MaVaiTro);

INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro)
SELECT u.MaNguoiDung, r.MaVaiTro FROM NguoiDung u, VaiTro r
WHERE u.TenDangNhap = 'manager2' AND r.TenVaiTro = 'ROLE_MANAGER'
  AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = u.MaNguoiDung AND MaVaiTro = r.MaVaiTro);
GO

-- 5. NHAN VIEN
DECLARE @MaChiNhanh1 INT = (SELECT TOP 1 MaChiNhanh FROM ChiNhanh WHERE Email = 'central@garage.com');
DECLARE @MaChiNhanh2 INT = (SELECT TOP 1 MaChiNhanh FROM ChiNhanh WHERE Email = 'binhthanh@garage.com');

IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'manager')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @MaChiNhanh1, N'Quản lý chi nhánh', '2024-01-01', 1
    FROM NguoiDung WHERE TenDangNhap = 'manager' AND @MaChiNhanh1 IS NOT NULL;

IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'receptionist')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @MaChiNhanh1, N'Nhân viên lễ tân', '2024-01-15', 1
    FROM NguoiDung WHERE TenDangNhap = 'receptionist' AND @MaChiNhanh1 IS NOT NULL;

IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'technician')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @MaChiNhanh1, N'Thợ máy chính', '2024-02-01', 1
    FROM NguoiDung WHERE TenDangNhap = 'technician' AND @MaChiNhanh1 IS NOT NULL;

-- CN002 manager (for cross-branch test)
IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'manager2')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @MaChiNhanh2, N'Quản lý chi nhánh 2', '2024-03-01', 1
    FROM NguoiDung WHERE TenDangNhap = 'manager2' AND @MaChiNhanh2 IS NOT NULL;

-- CN002 technician (for cross-branch technician assignment test)
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'technician2')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('technician2', '$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO', N'Lê Văn Kỹ Thuật 2', 'technician2@garage.com', '0900000008', 1);

INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro)
SELECT u.MaNguoiDung, r.MaVaiTro FROM NguoiDung u, VaiTro r
WHERE u.TenDangNhap = 'technician2' AND r.TenVaiTro = 'ROLE_TECHNICIAN'
  AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = u.MaNguoiDung AND MaVaiTro = r.MaVaiTro);

IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'technician2')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @MaChiNhanh2, N'Thợ máy chi nhánh 2', '2024-03-15', 1
    FROM NguoiDung WHERE TenDangNhap = 'technician2' AND @MaChiNhanh2 IS NOT NULL;
GO

-- 6. KHACH HANG
IF NOT EXISTS (SELECT 1 FROM KhachHang kh JOIN NguoiDung u ON u.MaNguoiDung = kh.MaNguoiDung WHERE u.TenDangNhap = 'customer')
    INSERT INTO KhachHang (MaNguoiDung, DiaChi, NgaySinh)
    SELECT MaNguoiDung, N'456 Lê Hồng Phong, Quận 10, TP.HCM', '1990-05-20'
    FROM NguoiDung WHERE TenDangNhap = 'customer';

-- Customer 2 (for cross-ownership test in TASK 07)
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'customer2')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('customer2', '$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO', N'Nguyễn Thị Khách Hàng 2', 'customer2@garage.com', '0900000007', 1);

INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro)
SELECT u.MaNguoiDung, r.MaVaiTro FROM NguoiDung u, VaiTro r
WHERE u.TenDangNhap = 'customer2' AND r.TenVaiTro = 'ROLE_CUSTOMER'
  AND NOT EXISTS (SELECT 1 FROM NguoiDung_VaiTro WHERE MaNguoiDung = u.MaNguoiDung AND MaVaiTro = r.MaVaiTro);

IF NOT EXISTS (SELECT 1 FROM KhachHang kh JOIN NguoiDung u ON u.MaNguoiDung = kh.MaNguoiDung WHERE u.TenDangNhap = 'customer2')
    INSERT INTO KhachHang (MaNguoiDung, DiaChi, NgaySinh)
    SELECT MaNguoiDung, N'789 Nguyễn Thị Minh Khai, Quận 3, TP.HCM', '1995-08-10'
    FROM NguoiDung WHERE TenDangNhap = 'customer2';
GO

-- 7. XE (seed vehicles for TASK 07 — Vehicle Management ownership test)
IF NOT EXISTS (SELECT 1 FROM Xe WHERE BienSo = '51A-11111')
    INSERT INTO Xe (MaKhachHang, BienSo, HangXe, Model, NamSanXuat, MauXe, SoVIN, SoKmHienTai, TrangThai)
    SELECT k.MaKhachHang, '51A-11111', N'Toyota', N'Camry', 2020, N'Đen', 'VIN-TOYOTA-001', 15000, 1
    FROM KhachHang k JOIN NguoiDung u ON u.MaNguoiDung = k.MaNguoiDung
    WHERE u.TenDangNhap = 'customer';

IF NOT EXISTS (SELECT 1 FROM Xe WHERE BienSo = '51B-22222')
    INSERT INTO Xe (MaKhachHang, BienSo, HangXe, Model, NamSanXuat, MauXe, SoVIN, SoKmHienTai, TrangThai)
    SELECT k.MaKhachHang, '51B-22222', N'Honda', N'Civic', 2021, N'Trắng', 'VIN-HONDA-001', 8000, 1
    FROM KhachHang k JOIN NguoiDung u ON u.MaNguoiDung = k.MaNguoiDung
    WHERE u.TenDangNhap = 'customer2';
GO

-- 8. DAT LICH (seed appointments for TASK 08 — Appointment Management ownership & branch test)
DECLARE @MaKH1 INT = (SELECT TOP 1 k.MaKhachHang FROM KhachHang k JOIN NguoiDung u ON u.MaNguoiDung = k.MaNguoiDung WHERE u.TenDangNhap = 'customer');
DECLARE @MaKH2 INT = (SELECT TOP 1 k.MaKhachHang FROM KhachHang k JOIN NguoiDung u ON u.MaNguoiDung = k.MaNguoiDung WHERE u.TenDangNhap = 'customer2');
DECLARE @MaXe1 INT = (SELECT MaXe FROM Xe WHERE BienSo = '51A-11111');
DECLARE @MaXe2 INT = (SELECT MaXe FROM Xe WHERE BienSo = '51B-22222');
DECLARE @MaCN1 INT = (SELECT TOP 1 MaChiNhanh FROM ChiNhanh WHERE Email = 'central@garage.com');
DECLARE @MaCN2 INT = (SELECT TOP 1 MaChiNhanh FROM ChiNhanh WHERE Email = 'binhthanh@garage.com');

IF NOT EXISTS (SELECT 1 FROM DatLich WHERE MaKhachHang = @MaKH1 AND MaXe = @MaXe1)
    INSERT INTO DatLich (MaKhachHang, MaXe, MaChiNhanh, ThoiGianHen, TrangThai, GhiChu)
    VALUES (@MaKH1, @MaXe1, @MaCN1, DATEADD(DAY, 1, SYSDATETIME()), 'CHO_XAC_NHAN', N'Bảo dưỡng định kỳ 15,000km');

IF NOT EXISTS (SELECT 1 FROM DatLich WHERE MaKhachHang = @MaKH2 AND MaXe = @MaXe2)
    INSERT INTO DatLich (MaKhachHang, MaXe, MaChiNhanh, ThoiGianHen, TrangThai, GhiChu)
    VALUES (@MaKH2, @MaXe2, @MaCN2, DATEADD(DAY, 2, SYSDATETIME()), 'CHO_XAC_NHAN', N'Kiểm tra định kỳ & thay dầu');
GO

-- 9. DICH VU THEO CHI NHANH (seed catalog for TASK 11 — Service / Repair Items Management)
IF NOT EXISTS (SELECT 1 FROM LoaiDichVu WHERE TenLoai = N'Bảo Dưỡng Định Kỳ')
    INSERT INTO LoaiDichVu (TenLoai, MoTa, TrangThai)
    VALUES (N'Bảo Dưỡng Định Kỳ', N'Các gói bảo dưỡng định kỳ cho ô tô', 1);

DECLARE @MaLoaiBD INT = (SELECT TOP 1 MaLoaiDichVu FROM LoaiDichVu WHERE TenLoai = N'Bảo Dưỡng Định Kỳ');
DECLARE @MaChiNhanh1 INT = (SELECT TOP 1 MaChiNhanh FROM ChiNhanh WHERE Email = 'central@garage.com');
DECLARE @MaChiNhanh2 INT = (SELECT TOP 1 MaChiNhanh FROM ChiNhanh WHERE Email = 'binhthanh@garage.com');

IF @MaLoaiBD IS NOT NULL AND @MaChiNhanh1 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM DichVu WHERE MaChiNhanh = @MaChiNhanh1 AND TenDichVu = N'Thay dầu động cơ và lọc dầu')
    INSERT INTO DichVu (MaLoaiDichVu, MaChiNhanh, TenDichVu, MoTa, DonGia, ThoiGianDuKien, TrangThai)
    VALUES (@MaLoaiBD, @MaChiNhanh1, N'Thay dầu động cơ và lọc dầu', N'Thay dầu nhớt động cơ chính hãng và cốc lọc', 250000.00, 45, 1);

IF @MaLoaiBD IS NOT NULL AND @MaChiNhanh1 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM DichVu WHERE MaChiNhanh = @MaChiNhanh1 AND TenDichVu = N'Kiểm tra và vệ sinh hệ thống phanh')
    INSERT INTO DichVu (MaLoaiDichVu, MaChiNhanh, TenDichVu, MoTa, DonGia, ThoiGianDuKien, TrangThai)
    VALUES (@MaLoaiBD, @MaChiNhanh1, N'Kiểm tra và vệ sinh hệ thống phanh', N'Bảo dưỡng má phanh, đĩa phanh và dầu phanh', 350000.00, 60, 1);

IF @MaLoaiBD IS NOT NULL AND @MaChiNhanh2 IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM DichVu WHERE MaChiNhanh = @MaChiNhanh2 AND TenDichVu = N'Thay dầu động cơ và lọc dầu')
    INSERT INTO DichVu (MaLoaiDichVu, MaChiNhanh, TenDichVu, MoTa, DonGia, ThoiGianDuKien, TrangThai)
    VALUES (@MaLoaiBD, @MaChiNhanh2, N'Thay dầu động cơ và lọc dầu', N'Thay dầu nhớt động cơ chính hãng và cốc lọc', 260000.00, 45, 1);
GO

-- 10. PHU TUNG & TON KHO (seed catalog and inventory for TASK 13 — Parts / Inventory Management)
DECLARE @MaBranch1 INT = (SELECT TOP 1 MaChiNhanh FROM ChiNhanh WHERE Email = 'central@garage.com');
DECLARE @MaBranch2 INT = (SELECT TOP 1 MaChiNhanh FROM ChiNhanh WHERE Email = 'binhthanh@garage.com');

IF @MaBranch1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM PhuTung WHERE MaPhuTungCode = 'PT001')
    INSERT INTO PhuTung (MaChiNhanh, MaPhuTungCode, TenPhuTung, DonViTinh, GiaNhap, GiaBan, TrangThai)
    VALUES (@MaBranch1, 'PT001', N'Lọc dầu động cơ Toyota/Honda', N'Cái', 80000.00, 150000.00, 1);

IF @MaBranch1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM PhuTung WHERE MaPhuTungCode = 'PT002')
    INSERT INTO PhuTung (MaChiNhanh, MaPhuTungCode, TenPhuTung, DonViTinh, GiaNhap, GiaBan, TrangThai)
    VALUES (@MaBranch1, 'PT002', N'Bộ má phanh trước', N'Bộ', 300000.00, 550000.00, 1);

IF @MaBranch1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM PhuTung WHERE MaPhuTungCode = 'PT003')
    INSERT INTO PhuTung (MaChiNhanh, MaPhuTungCode, TenPhuTung, DonViTinh, GiaNhap, GiaBan, TrangThai)
    VALUES (@MaBranch1, 'PT003', N'Dầu nhớt động cơ Castrol 5W-30 (Can 4L)', N'Can', 450000.00, 680000.00, 1);

IF @MaBranch2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM PhuTung WHERE MaPhuTungCode = 'PT101')
    INSERT INTO PhuTung (MaChiNhanh, MaPhuTungCode, TenPhuTung, DonViTinh, GiaNhap, GiaBan, TrangThai)
    VALUES (@MaBranch2, 'PT101', N'Lọc dầu động cơ Toyota/Honda', N'Cái', 80000.00, 150000.00, 1);

IF @MaBranch2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM PhuTung WHERE MaPhuTungCode = 'PT102')
    INSERT INTO PhuTung (MaChiNhanh, MaPhuTungCode, TenPhuTung, DonViTinh, GiaNhap, GiaBan, TrangThai)
    VALUES (@MaBranch2, 'PT102', N'Bộ má phanh trước', N'Bộ', 300000.00, 550000.00, 1);

IF @MaBranch2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM PhuTung WHERE MaPhuTungCode = 'PT103')
    INSERT INTO PhuTung (MaChiNhanh, MaPhuTungCode, TenPhuTung, DonViTinh, GiaNhap, GiaBan, TrangThai)
    VALUES (@MaBranch2, 'PT103', N'Dầu nhớt động cơ Castrol 5W-30 (Can 4L)', N'Can', 450000.00, 680000.00, 1);

DECLARE @MaPT1 INT = (SELECT TOP 1 MaPhuTung FROM PhuTung WHERE MaPhuTungCode = 'PT001' AND MaChiNhanh = @MaBranch1);
DECLARE @MaPT2 INT = (SELECT TOP 1 MaPhuTung FROM PhuTung WHERE MaPhuTungCode = 'PT002' AND MaChiNhanh = @MaBranch1);
DECLARE @MaPT3 INT = (SELECT TOP 1 MaPhuTung FROM PhuTung WHERE MaPhuTungCode = 'PT003' AND MaChiNhanh = @MaBranch1);
DECLARE @MaPT101 INT = (SELECT TOP 1 MaPhuTung FROM PhuTung WHERE MaPhuTungCode = 'PT101' AND MaChiNhanh = @MaBranch2);
DECLARE @MaPT102 INT = (SELECT TOP 1 MaPhuTung FROM PhuTung WHERE MaPhuTungCode = 'PT102' AND MaChiNhanh = @MaBranch2);
DECLARE @MaPT103 INT = (SELECT TOP 1 MaPhuTung FROM PhuTung WHERE MaPhuTungCode = 'PT103' AND MaChiNhanh = @MaBranch2);

-- Tồn kho CN001
IF @MaPT1 IS NOT NULL AND @MaBranch1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM TonKho WHERE MaChiNhanh = @MaBranch1 AND MaPhuTung = @MaPT1)
    INSERT INTO TonKho (MaChiNhanh, MaPhuTung, SoLuongTon, SoLuongToiThieu) VALUES (@MaBranch1, @MaPT1, 10, 2);

IF @MaPT2 IS NOT NULL AND @MaBranch1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM TonKho WHERE MaChiNhanh = @MaBranch1 AND MaPhuTung = @MaPT2)
    INSERT INTO TonKho (MaChiNhanh, MaPhuTung, SoLuongTon, SoLuongToiThieu) VALUES (@MaBranch1, @MaPT2, 5, 1);

IF @MaPT3 IS NOT NULL AND @MaBranch1 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM TonKho WHERE MaChiNhanh = @MaBranch1 AND MaPhuTung = @MaPT3)
    INSERT INTO TonKho (MaChiNhanh, MaPhuTung, SoLuongTon, SoLuongToiThieu) VALUES (@MaBranch1, @MaPT3, 8, 2);

-- Tồn kho CN002
IF @MaPT101 IS NOT NULL AND @MaBranch2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM TonKho WHERE MaChiNhanh = @MaBranch2 AND MaPhuTung = @MaPT101)
    INSERT INTO TonKho (MaChiNhanh, MaPhuTung, SoLuongTon, SoLuongToiThieu) VALUES (@MaBranch2, @MaPT101, 6, 2);

IF @MaPT102 IS NOT NULL AND @MaBranch2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM TonKho WHERE MaChiNhanh = @MaBranch2 AND MaPhuTung = @MaPT102)
    INSERT INTO TonKho (MaChiNhanh, MaPhuTung, SoLuongTon, SoLuongToiThieu) VALUES (@MaBranch2, @MaPT102, 4, 1);

IF @MaPT103 IS NOT NULL AND @MaBranch2 IS NOT NULL AND NOT EXISTS (SELECT 1 FROM TonKho WHERE MaChiNhanh = @MaBranch2 AND MaPhuTung = @MaPT103)
    INSERT INTO TonKho (MaChiNhanh, MaPhuTung, SoLuongTon, SoLuongToiThieu) VALUES (@MaBranch2, @MaPT103, 5, 2);
GO
