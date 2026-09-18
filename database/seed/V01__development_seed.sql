USE GarageManagementSystem;
GO

SET XACT_ABORT ON;
GO

BEGIN TRANSACTION;

DECLARE @PasswordHash VARCHAR(255) = '$2a$10$ClEFdX0R7SanUp/08zNDYOFUdflLGCXChrTo25Hwo2OZAD80z/NjO';

IF OBJECT_ID(N'dbo.GiaDichVuChiNhanh', N'U') IS NOT NULL
BEGIN
    THROW 51000, 'Development seed must not depend on legacy table GiaDichVuChiNhanh. Use DichVu.DonGia from GarageManagementSystem.sql.', 1;
END;

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

IF NOT EXISTS (SELECT 1 FROM ChiNhanh WHERE TenChiNhanh = N'Garage Central Chi Nhánh 1')
BEGIN
    INSERT INTO ChiNhanh (TenChiNhanh, DiaChi, SoDienThoai, Email, TrangThai)
    VALUES (N'Garage Central Chi Nhánh 1', N'123 Nguyễn Văn Linh, Quận 7, TP.HCM', '02873000001', 'branch1@autocare.local', 1);
END;

IF NOT EXISTS (SELECT 1 FROM ChiNhanh WHERE TenChiNhanh = N'Garage Chi Nhánh 2 Bình Thạnh')
BEGIN
    INSERT INTO ChiNhanh (TenChiNhanh, DiaChi, SoDienThoai, Email, TrangThai)
    VALUES (N'Garage Chi Nhánh 2 Bình Thạnh', N'45 Điện Biên Phủ, Bình Thạnh, TP.HCM', '02873000002', 'branch2@autocare.local', 1);
END;

DECLARE @Branch1 INT = (SELECT MaChiNhanh FROM ChiNhanh WHERE TenChiNhanh = N'Garage Central Chi Nhánh 1');
DECLARE @Branch2 INT = (SELECT MaChiNhanh FROM ChiNhanh WHERE TenChiNhanh = N'Garage Chi Nhánh 2 Bình Thạnh');

IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'admin')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('admin', @PasswordHash, N'Quản trị hệ thống', 'admin@autocare.local', '0900000001', 1);
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'manager')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('manager', @PasswordHash, N'Quản lý Chi Nhánh 1', 'manager@autocare.local', '0900000002', 1);
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'receptionist')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('receptionist', @PasswordHash, N'Nhân viên tiếp nhận Chi Nhánh 1', 'receptionist@autocare.local', '0900000003', 1);
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'technician')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('technician', @PasswordHash, N'Kỹ thuật viên Chi Nhánh 1', 'technician@autocare.local', '0900000004', 1);
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'manager2')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('manager2', @PasswordHash, N'Quản lý Chi Nhánh 2', 'manager2@autocare.local', '0900000005', 1);
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'technician2')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('technician2', @PasswordHash, N'Kỹ thuật viên Chi Nhánh 2', 'technician2@autocare.local', '0900000006', 1);
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'customer')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('customer', @PasswordHash, N'Khách hàng Nguyễn Văn A', 'customer@autocare.local', '0900000007', 1);
IF NOT EXISTS (SELECT 1 FROM NguoiDung WHERE TenDangNhap = 'customer2')
    INSERT INTO NguoiDung (TenDangNhap, MatKhauHash, HoTen, Email, SoDienThoai, TrangThai)
    VALUES ('customer2', @PasswordHash, N'Khách hàng Trần Thị B', 'customer2@autocare.local', '0900000008', 1);

INSERT INTO NguoiDung_VaiTro (MaNguoiDung, MaVaiTro)
SELECT u.MaNguoiDung, r.MaVaiTro
FROM (VALUES
    ('admin', 'ROLE_ADMIN'),
    ('manager', 'ROLE_MANAGER'),
    ('receptionist', 'ROLE_FRONT_DESK'),
    ('technician', 'ROLE_TECHNICIAN'),
    ('manager2', 'ROLE_MANAGER'),
    ('technician2', 'ROLE_TECHNICIAN'),
    ('customer', 'ROLE_CUSTOMER'),
    ('customer2', 'ROLE_CUSTOMER')
) AS seed(TenDangNhap, TenVaiTro)
JOIN NguoiDung u ON u.TenDangNhap = seed.TenDangNhap
JOIN VaiTro r ON r.TenVaiTro = seed.TenVaiTro
WHERE NOT EXISTS (
    SELECT 1
    FROM NguoiDung_VaiTro ur
    WHERE ur.MaNguoiDung = u.MaNguoiDung
      AND ur.MaVaiTro = r.MaVaiTro
);

IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'manager')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @Branch1, N'Quản lý chi nhánh', '2024-01-01', 1 FROM NguoiDung WHERE TenDangNhap = 'manager';
IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'receptionist')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @Branch1, N'Nhân viên tiếp nhận', '2024-01-05', 1 FROM NguoiDung WHERE TenDangNhap = 'receptionist';
IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'technician')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @Branch1, N'Kỹ thuật viên', '2024-01-10', 1 FROM NguoiDung WHERE TenDangNhap = 'technician';
IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'manager2')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @Branch2, N'Quản lý chi nhánh', '2024-02-01', 1 FROM NguoiDung WHERE TenDangNhap = 'manager2';
IF NOT EXISTS (SELECT 1 FROM NhanVien nv JOIN NguoiDung u ON u.MaNguoiDung = nv.MaNguoiDung WHERE u.TenDangNhap = 'technician2')
    INSERT INTO NhanVien (MaNguoiDung, MaChiNhanh, ChucVu, NgayVaoLam, TrangThai)
    SELECT MaNguoiDung, @Branch2, N'Kỹ thuật viên', '2024-02-10', 1 FROM NguoiDung WHERE TenDangNhap = 'technician2';

IF NOT EXISTS (SELECT 1 FROM KhachHang kh JOIN NguoiDung u ON u.MaNguoiDung = kh.MaNguoiDung WHERE u.TenDangNhap = 'customer')
    INSERT INTO KhachHang (MaNguoiDung, DiaChi, NgaySinh)
    SELECT MaNguoiDung, N'12 Lê Lợi, Quận 1, TP.HCM', '1992-05-15' FROM NguoiDung WHERE TenDangNhap = 'customer';
IF NOT EXISTS (SELECT 1 FROM KhachHang kh JOIN NguoiDung u ON u.MaNguoiDung = kh.MaNguoiDung WHERE u.TenDangNhap = 'customer2')
    INSERT INTO KhachHang (MaNguoiDung, DiaChi, NgaySinh)
    SELECT MaNguoiDung, N'88 Võ Văn Tần, Quận 3, TP.HCM', '1995-09-20' FROM NguoiDung WHERE TenDangNhap = 'customer2';

IF NOT EXISTS (SELECT 1 FROM HangXe WHERE TenHangXe = N'Toyota')
    INSERT INTO HangXe (TenHangXe, TrangThai) VALUES (N'Toyota', 1);
IF NOT EXISTS (SELECT 1 FROM HangXe WHERE TenHangXe = N'Honda')
    INSERT INTO HangXe (TenHangXe, TrangThai) VALUES (N'Honda', 1);

DECLARE @Toyota INT = (SELECT MaHangXe FROM HangXe WHERE TenHangXe = N'Toyota');
DECLARE @Honda INT = (SELECT MaHangXe FROM HangXe WHERE TenHangXe = N'Honda');

IF NOT EXISTS (SELECT 1 FROM ModelXe WHERE MaHangXe = @Toyota AND TenModel = N'Camry')
    INSERT INTO ModelXe (MaHangXe, TenModel, TrangThai) VALUES (@Toyota, N'Camry', 1);
IF NOT EXISTS (SELECT 1 FROM ModelXe WHERE MaHangXe = @Toyota AND TenModel = N'Vios')
    INSERT INTO ModelXe (MaHangXe, TenModel, TrangThai) VALUES (@Toyota, N'Vios', 1);
IF NOT EXISTS (SELECT 1 FROM ModelXe WHERE MaHangXe = @Honda AND TenModel = N'Civic')
    INSERT INTO ModelXe (MaHangXe, TenModel, TrangThai) VALUES (@Honda, N'Civic', 1);
IF NOT EXISTS (SELECT 1 FROM ModelXe WHERE MaHangXe = @Honda AND TenModel = N'City')
    INSERT INTO ModelXe (MaHangXe, TenModel, TrangThai) VALUES (@Honda, N'City', 1);

IF NOT EXISTS (SELECT 1 FROM Xe WHERE BienSo = '51A-11111')
    INSERT INTO Xe (MaKhachHang, BienSo, MaModel, NamSanXuat, MauXe, SoVIN, SoKmHienTai, TrangThai)
    SELECT kh.MaKhachHang, '51A-11111', mx.MaModel, 2020, N'Trắng', 'VINDEV00000000001', 45000, 1
    FROM KhachHang kh
    JOIN NguoiDung u ON u.MaNguoiDung = kh.MaNguoiDung
    JOIN ModelXe mx ON mx.MaHangXe = @Toyota AND mx.TenModel = N'Camry'
    WHERE u.TenDangNhap = 'customer';
IF NOT EXISTS (SELECT 1 FROM Xe WHERE BienSo = '51B-22222')
    INSERT INTO Xe (MaKhachHang, BienSo, MaModel, NamSanXuat, MauXe, SoVIN, SoKmHienTai, TrangThai)
    SELECT kh.MaKhachHang, '51B-22222', mx.MaModel, 2021, N'Đen', 'VINDEV00000000002', 32000, 1
    FROM KhachHang kh
    JOIN NguoiDung u ON u.MaNguoiDung = kh.MaNguoiDung
    JOIN ModelXe mx ON mx.MaHangXe = @Honda AND mx.TenModel = N'Civic'
    WHERE u.TenDangNhap = 'customer2';

IF NOT EXISTS (SELECT 1 FROM LoaiDichVu WHERE TenLoai = N'Bảo dưỡng')
    INSERT INTO LoaiDichVu (TenLoai, MoTa, TrangThai) VALUES (N'Bảo dưỡng', N'Dịch vụ bảo dưỡng định kỳ', 1);
IF NOT EXISTS (SELECT 1 FROM LoaiDichVu WHERE TenLoai = N'Sửa chữa')
    INSERT INTO LoaiDichVu (TenLoai, MoTa, TrangThai) VALUES (N'Sửa chữa', N'Dịch vụ sửa chữa và kiểm tra lỗi', 1);

DECLARE @BaoDuong INT = (SELECT MaLoaiDichVu FROM LoaiDichVu WHERE TenLoai = N'Bảo dưỡng');
DECLARE @SuaChua INT = (SELECT MaLoaiDichVu FROM LoaiDichVu WHERE TenLoai = N'Sửa chữa');

IF NOT EXISTS (SELECT 1 FROM DichVu WHERE TenDichVu = N'Thay dầu động cơ')
    INSERT INTO DichVu (MaLoaiDichVu, TenDichVu, MoTa, DonGia, ThoiGianDuKien, TrangThai)
    VALUES (@BaoDuong, N'Thay dầu động cơ', N'Thay dầu và kiểm tra lọc dầu cơ bản', 250000.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE TenDichVu = N'Kiểm tra phanh')
    INSERT INTO DichVu (MaLoaiDichVu, TenDichVu, MoTa, DonGia, ThoiGianDuKien, TrangThai)
    VALUES (@BaoDuong, N'Kiểm tra phanh', N'Kiểm tra má phanh, dầu phanh và hệ thống phanh', 180000.00, 1, 1);
IF NOT EXISTS (SELECT 1 FROM DichVu WHERE TenDichVu = N'Chẩn đoán lỗi động cơ')
    INSERT INTO DichVu (MaLoaiDichVu, TenDichVu, MoTa, DonGia, ThoiGianDuKien, TrangThai)
    VALUES (@SuaChua, N'Chẩn đoán lỗi động cơ', N'Đọc lỗi OBD và kiểm tra tình trạng động cơ', 350000.00, 2, 1);

IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE MaPhuTungCode = 'OIL-5W30')
    INSERT INTO PhuTung (MaPhuTungCode, TenPhuTung, DonViTinh, GiaNhap, GiaBan, TrangThai)
    VALUES ('OIL-5W30', N'Dầu động cơ 5W-30', N'Chai', 120000.00, 180000.00, 1);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE MaPhuTungCode = 'FILTER-OIL')
    INSERT INTO PhuTung (MaPhuTungCode, TenPhuTung, DonViTinh, GiaNhap, GiaBan, TrangThai)
    VALUES ('FILTER-OIL', N'Lọc dầu động cơ', N'Cái', 65000.00, 120000.00, 1);
IF NOT EXISTS (SELECT 1 FROM PhuTung WHERE MaPhuTungCode = 'BRAKE-PAD')
    INSERT INTO PhuTung (MaPhuTungCode, TenPhuTung, DonViTinh, GiaNhap, GiaBan, TrangThai)
    VALUES ('BRAKE-PAD', N'Bố thắng trước', N'Bộ', 450000.00, 680000.00, 1);

INSERT INTO TonKho (MaChiNhanh, MaPhuTung, SoLuongTon, SoLuongToiThieu)
SELECT cn.MaChiNhanh, pt.MaPhuTung, seed.SoLuongTon, seed.SoLuongToiThieu
FROM ChiNhanh cn
CROSS JOIN (
    SELECT MaPhuTung, 40 AS SoLuongTon, 10 AS SoLuongToiThieu FROM PhuTung WHERE MaPhuTungCode = 'OIL-5W30'
    UNION ALL
    SELECT MaPhuTung, 25, 5 FROM PhuTung WHERE MaPhuTungCode = 'FILTER-OIL'
    UNION ALL
    SELECT MaPhuTung, 12, 4 FROM PhuTung WHERE MaPhuTungCode = 'BRAKE-PAD'
) seed
JOIN PhuTung pt ON pt.MaPhuTung = seed.MaPhuTung
WHERE cn.TenChiNhanh IN (N'Garage Central Chi Nhánh 1', N'Garage Chi Nhánh 2 Bình Thạnh')
  AND NOT EXISTS (
      SELECT 1
      FROM TonKho tk
      WHERE tk.MaChiNhanh = cn.MaChiNhanh
        AND tk.MaPhuTung = pt.MaPhuTung
  );

COMMIT TRANSACTION;
GO
