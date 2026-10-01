-- Additive migration approved on 2026-09-30.
-- Database: GarageManagementSystem. No existing rows/tables are changed.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID('dbo.PhienThanhToan', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhienThanhToan (
        MaPhien VARCHAR(36) NOT NULL PRIMARY KEY,
        MaHoaDon INT NOT NULL REFERENCES dbo.HoaDon(MaHoaDon),
        SoTien DECIMAL(18,2) NOT NULL CHECK (SoTien > 0),
        NoiDung VARCHAR(40) NOT NULL UNIQUE,
        MoiTruong VARCHAR(10) NOT NULL,
        MaNganHang VARCHAR(50) NOT NULL,
        TenNganHang NVARCHAR(100) NOT NULL,
        SoTaiKhoan VARCHAR(50) NOT NULL,
        TenChuTaiKhoan NVARCHAR(150) NOT NULL,
        TrangThai VARCHAR(30) NOT NULL,
        TaoLuc DATETIME2 NOT NULL,
        HetHanLuc DATETIME2 NOT NULL
    );
    CREATE INDEX IX_PhienThanhToan_HoaDon ON dbo.PhienThanhToan(MaHoaDon);
END;
IF OBJECT_ID('dbo.GiaoDichSePay', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.GiaoDichSePay (
        MaGiaoDich VARCHAR(100) NOT NULL PRIMARY KEY,
        MaPhien VARCHAR(36) NULL REFERENCES dbo.PhienThanhToan(MaPhien),
        SoTien DECIMAL(18,2) NOT NULL,
        MaNganHang VARCHAR(50) NOT NULL,
        SoTaiKhoan VARCHAR(50) NOT NULL,
        NoiDung NVARCHAR(1000) NOT NULL,
        MaThamChieu VARCHAR(100) NULL,
        KetQua VARCHAR(40) NOT NULL,
        NhanLuc DATETIME2 NOT NULL
    );
END;
COMMIT;
