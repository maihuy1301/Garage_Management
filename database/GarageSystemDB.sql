SET QUOTED_IDENTIFIER ON
GO
SET ANSI_NULLS ON
GO
create database GarageManagementSystem
GO
use GarageManagementSystem
GO


CREATE TABLE ChiNhanh (
    MaChiNhanh INT IDENTITY(1,1) PRIMARY KEY,
    MaChiNhanhCode VARCHAR(20) UNIQUE NOT NULL,
    TenChiNhanh NVARCHAR(150) NOT NULL,
    DiaChi NVARCHAR(255) NOT NULL,
    SoDienThoai VARCHAR(20),
    Email VARCHAR(100),
    TrangThai BIT DEFAULT 1,
    NgayTao DATETIME2 DEFAULT SYSDATETIME()
);
GO
--ROLE
CREATE TABLE VaiTro (
    MaVaiTro INT IDENTITY(1,1) PRIMARY KEY,
    TenVaiTro VARCHAR(50) UNIQUE NOT NULL,
    MoTa NVARCHAR(255)
);
GO
CREATE TABLE NguoiDung (
    MaNguoiDung INT IDENTITY(1,1) PRIMARY KEY,
    TenDangNhap VARCHAR(50) UNIQUE NOT NULL,
    MatKhauHash VARCHAR(255) NOT NULL,
    HoTen NVARCHAR(100) NOT NULL,
    Email VARCHAR(100),
    SoDienThoai VARCHAR(20),
    AnhDaiDien NVARCHAR(500),
    TrangThai BIT DEFAULT 1,
    NgayTao DATETIME2 DEFAULT SYSDATETIME()
);
GO
CREATE TABLE NguoiDung_VaiTro (
    MaNguoiDung INT NOT NULL,
    MaVaiTro INT NOT NULL,
    PRIMARY KEY (MaNguoiDung, MaVaiTro),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
    FOREIGN KEY (MaVaiTro) REFERENCES VaiTro(MaVaiTro)
);
GO
CREATE TABLE NhanVien (
    MaNhanVien INT IDENTITY(1,1) PRIMARY KEY,
    MaNguoiDung INT UNIQUE NOT NULL,
    MaChiNhanh INT NOT NULL,
    MaNhanVienCode VARCHAR(20) UNIQUE NOT NULL,
    ChucVu NVARCHAR(100),
    NgayVaoLam DATE,
    TrangThai BIT DEFAULT 1,
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh)
);
GO
CREATE TABLE KhachHang (
    MaKhachHang INT IDENTITY(1,1) PRIMARY KEY,
    MaNguoiDung INT UNIQUE NOT NULL,
    MaKhachHangCode VARCHAR(20) UNIQUE NOT NULL,
    DiaChi NVARCHAR(255),
    NgaySinh DATE,
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);
GO
CREATE TABLE Xe (
    MaXe INT IDENTITY(1,1) PRIMARY KEY,
    MaKhachHang INT NOT NULL,
    BienSo VARCHAR(20) UNIQUE NOT NULL,
    HangXe NVARCHAR(50),
    Model NVARCHAR(100),
    NamSanXuat INT,
    MauXe NVARCHAR(50),
    SoVIN VARCHAR(50),
    SoKmHienTai INT DEFAULT 0,
    NgayTao DATETIME2 DEFAULT SYSDATETIME(),
    TrangThai BIT DEFAULT 1,
    FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang)
);
GO
CREATE TABLE LoaiDichVu (
    MaLoaiDichVu INT IDENTITY(1,1) PRIMARY KEY,
    TenLoai NVARCHAR(100) NOT NULL,
    MoTa NVARCHAR(255),
    TrangThai BIT DEFAULT 1
);
GO
CREATE TABLE DichVu (
    MaDichVu INT IDENTITY(1,1) PRIMARY KEY,
    MaLoaiDichVu INT NOT NULL,
    TenDichVu NVARCHAR(150) NOT NULL,
    MoTa NVARCHAR(500),
    ThoiGianDuKien INT,
    TrangThai BIT DEFAULT 1,
    FOREIGN KEY (MaLoaiDichVu) REFERENCES LoaiDichVu(MaLoaiDichVu)
);
GO
CREATE TABLE GiaDichVuChiNhanh (
    MaGia INT IDENTITY(1,1) PRIMARY KEY,
    MaChiNhanh INT NOT NULL,
    MaDichVu INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    NgayApDung DATE NOT NULL,
    NgayKetThuc DATE,
    TrangThai BIT DEFAULT 1,
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu),
    CONSTRAINT UQ_GiaDichVu UNIQUE (MaChiNhanh, MaDichVu, NgayApDung)
);
GO
CREATE TABLE DatLich (
    MaDatLich INT IDENTITY(1,1) PRIMARY KEY,
    MaKhachHang INT NOT NULL,
    MaXe INT NOT NULL,
    MaChiNhanh INT NOT NULL,
    ThoiGianHen DATETIME2 NOT NULL,
    TrangThai VARCHAR(30) NOT NULL DEFAULT 'CHO_XAC_NHAN',
    GhiChu NVARCHAR(500),
    NgayDat DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh)
);
GO
--CHO_XAC_NHAN | DA_XAC_NHAN | DA_TIEP_NHAN | DANG_XU_LY | HOAN_TAT | HUY | KHONG_DEN


--Một lịch có thể chọn nhiều dịch vụ
CREATE TABLE DatLich_DichVu (
    MaDatLich INT NOT NULL,
    MaDichVu INT NOT NULL,
    SoLuong INT DEFAULT 1,
    GhiChu NVARCHAR(255),
    PRIMARY KEY (MaDatLich, MaDichVu),
    FOREIGN KEY (MaDatLich) REFERENCES DatLich(MaDatLich),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);
GO
CREATE TABLE PhieuTiepNhan (
    MaTiepNhan INT IDENTITY(1,1) PRIMARY KEY,
    MaDatLich INT NULL,
    MaXe INT NOT NULL,
    MaChiNhanh INT NOT NULL,
    MaNhanVienTiepNhan INT NOT NULL,
    ThoiGianTiepNhan DATETIME2 DEFAULT SYSDATETIME(),
    SoKm INT,
    TinhTrangNgoaiThat NVARCHAR(1000),
    YeuCauKhachHang NVARCHAR(1000),
    TrangThai VARCHAR(30) DEFAULT 'DA_TIEP_NHAN',
    FOREIGN KEY (MaDatLich) REFERENCES DatLich(MaDatLich),
    FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh),
    FOREIGN KEY (MaNhanVienTiepNhan) REFERENCES NhanVien(MaNhanVien)
);
GO
CREATE TABLE KiemTraXe (
    MaKiemTra INT IDENTITY(1,1) PRIMARY KEY,
    MaTiepNhan INT NOT NULL,
    NoiDungKiemTra NVARCHAR(500) NOT NULL,
    KetQua NVARCHAR(1000),
    MucDo VARCHAR(30), --(STATUS)
    NgayKiemTra DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (MaTiepNhan) REFERENCES PhieuTiepNhan(MaTiepNhan)
);
GO
CREATE TABLE PhieuSuaChua (
    MaPhieuSuaChua INT IDENTITY(1,1) PRIMARY KEY,
    MaTiepNhan INT NOT NULL,
    MaChiNhanh INT NOT NULL,
    ThoiGianBatDau DATETIME2,
    ThoiGianHoanTat DATETIME2,
    TrangThai VARCHAR(30) DEFAULT 'CHO_XU_LY',
    GhiChu NVARCHAR(1000),
    FOREIGN KEY (MaTiepNhan) REFERENCES PhieuTiepNhan(MaTiepNhan),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh)
);
GO
--CHO_XU_LY | DA_PHAN_CONG | DANG_SUA | CHO_KH_DUYET | TAM_DUNG | HOAN_TAT | HUY

CREATE TABLE PhieuSuaChua_DichVu (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuSuaChua INT NOT NULL,
    MaDichVu INT NOT NULL,
    SoLuong INT DEFAULT 1,
    DonGia DECIMAL(18,2) NOT NULL,
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    TrangThai VARCHAR(30) DEFAULT 'CHO_XU_LY',
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);
GO
CREATE TABLE PhanCong (
    MaPhanCong INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuSuaChua INT NOT NULL,
    MaNhanVien INT NOT NULL,
    VaiTroTrongCongViec NVARCHAR(100),
    ThoiGianPhanCong DATETIME2 DEFAULT SYSDATETIME(),
    TrangThai VARCHAR(30) DEFAULT 'DA_GIAO',
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien)
);
GO
CREATE TABLE TienDoSuaChua (
    MaTienDo INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuSuaChua INT NOT NULL,
    MaNhanVien INT NOT NULL,
    TrangThai VARCHAR(30) NOT NULL,
    PhanTramHoanThanh INT,
    MoTa NVARCHAR(1000),
    ThoiGian DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien)
);
GO
CREATE TABLE HinhAnhSuaChua (
    MaHinhAnh INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuSuaChua INT NOT NULL,
    MaNhanVien INT NOT NULL,
    DuongDanAnh NVARCHAR(500) NOT NULL,
    LoaiAnh VARCHAR(30),
    MoTa NVARCHAR(500),
    ThoiGianChup DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien)
);
GO
CREATE TABLE PhuTung (
    MaPhuTung INT IDENTITY(1,1) PRIMARY KEY,
    MaPhuTungCode VARCHAR(30) UNIQUE NOT NULL,
    TenPhuTung NVARCHAR(150) NOT NULL,
    DonViTinh NVARCHAR(30),
    GiaNhap DECIMAL(18,2),
    GiaBan DECIMAL(18,2),
    TrangThai BIT DEFAULT 1
);
GO
CREATE TABLE TonKho (
    MaChiNhanh INT NOT NULL,
    MaPhuTung INT NOT NULL,
    SoLuongTon INT DEFAULT 0,
    SoLuongToiThieu INT DEFAULT 0,
    PRIMARY KEY (MaChiNhanh, MaPhuTung),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh),
    FOREIGN KEY (MaPhuTung) REFERENCES PhuTung(MaPhuTung)
);
GO
CREATE TABLE GiaoDichKho (
    MaGiaoDich INT IDENTITY(1,1) PRIMARY KEY,
    MaChiNhanh INT NOT NULL,
    MaPhuTung INT NOT NULL,
    LoaiGiaoDich VARCHAR(30) NOT NULL,
    SoLuong INT NOT NULL,
    MaPhieuSuaChua INT NULL,
    GhiChu NVARCHAR(500),
    ThoiGian DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh),
    FOREIGN KEY (MaPhuTung) REFERENCES PhuTung(MaPhuTung),
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua)
);
GO
CREATE TABLE PhieuSuaChua_PhuTung (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuSuaChua INT NOT NULL,
    MaPhuTung INT NOT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    FOREIGN KEY (MaPhuTung) REFERENCES PhuTung(MaPhuTung)
);
GO
CREATE TABLE BaoGiaPhatSinh (
    MaBaoGia INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuSuaChua INT NOT NULL,
    LyDoPhatSinh NVARCHAR(1000),
    TongTien DECIMAL(18,2),
    TrangThai VARCHAR(30) DEFAULT 'CHO_KHACH_DUYET',
    ThoiGianTao DATETIME2 DEFAULT SYSDATETIME(),
    ThoiGianDuyet DATETIME2,
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua)
);
GO
CREATE TABLE BaoGiaPhatSinh_DichVu (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaBaoGia INT NOT NULL,
    MaDichVu INT NOT NULL,
    SoLuong INT DEFAULT 1,
    DonGia DECIMAL(18,2) NOT NULL,
    FOREIGN KEY (MaBaoGia) REFERENCES BaoGiaPhatSinh(MaBaoGia),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);
GO
CREATE TABLE BaoGiaPhatSinh_PhuTung (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaBaoGia INT NOT NULL,
    MaPhuTung INT NOT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    FOREIGN KEY (MaBaoGia) REFERENCES BaoGiaPhatSinh(MaBaoGia),
    FOREIGN KEY (MaPhuTung) REFERENCES PhuTung(MaPhuTung)
);
GO
CREATE TABLE HoaDon (
    MaHoaDon INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuSuaChua INT NOT NULL UNIQUE,
    MaKhachHang INT NOT NULL,
    MaChiNhanh INT NOT NULL,
    MaNhanVienThuNgan INT,
    TongTien DECIMAL(18,2) NOT NULL DEFAULT 0,
    GiamGia DECIMAL(18,2) DEFAULT 0,
    Thue DECIMAL(18,2) DEFAULT 0,
    ThanhTien DECIMAL(18,2) NOT NULL,
    TrangThai VARCHAR(30) DEFAULT 'CHUA_THANH_TOAN',
    NgayLap DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh),
    FOREIGN KEY (MaNhanVienThuNgan) REFERENCES NhanVien(MaNhanVien)
);
GO
CREATE TABLE HoaDon_DichVu (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaHoaDon INT NOT NULL,
    MaDichVu INT NOT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);
GO
CREATE TABLE HoaDon_PhuTung (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaHoaDon INT NOT NULL,
    MaPhuTung INT NOT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon),
    FOREIGN KEY (MaPhuTung) REFERENCES PhuTung(MaPhuTung)
);
GO
CREATE TABLE ThanhToan (
    MaThanhToan INT IDENTITY(1,1) PRIMARY KEY,
    MaHoaDon INT NOT NULL,
    SoTien DECIMAL(18,2) NOT NULL,
    PhuongThuc VARCHAR(30) NOT NULL,
    MaGiaoDich VARCHAR(100),
    ThoiGianThanhToan DATETIME2 DEFAULT SYSDATETIME(),
    TrangThai VARCHAR(30) DEFAULT 'THANH_CONG',
    FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon)
);
GO
CREATE TABLE DanhGia (
    MaDanhGia INT IDENTITY(1,1) PRIMARY KEY,
    MaKhachHang INT NOT NULL,
    MaPhieuSuaChua INT NOT NULL UNIQUE,
    SoSao INT NOT NULL,
    NoiDung NVARCHAR(1000),
    NgayDanhGia DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    CONSTRAINT CK_DanhGia_SoSao CHECK (SoSao BETWEEN 1 AND 5)
);
GO
CREATE TABLE NhacBaoDuong (
    MaNhac INT IDENTITY(1,1) PRIMARY KEY,
    MaXe INT NOT NULL,
    MaDichVu INT NULL,
    NgayDuKien DATE,
    KmDuKien INT,
    TrangThai VARCHAR(30) DEFAULT 'CHO_NHAC',
    DaGuiThongBao BIT DEFAULT 0,
    FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);
GO
CREATE TABLE ThongBao (
    MaThongBao INT IDENTITY(1,1) PRIMARY KEY,
    MaNguoiDung INT NOT NULL,
    TieuDe NVARCHAR(200) NOT NULL,
    NoiDung NVARCHAR(1000),
    LoaiThongBao VARCHAR(50),
    MaThamChieu INT NULL,
    DaDoc BIT DEFAULT 0,
    NgayTao DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);
GO
CREATE TABLE CuocHoiThoai (
    MaCuocHoiThoai INT IDENTITY(1,1) PRIMARY KEY,
    
    MaKhachHang INT NOT NULL,
    MaNhanVien INT NULL, -- Có thể NULL lúc đầu, nhân viên nào vào chat thì gán ID sau
    
    MaTiepNhan INT NULL, -- Tham chiếu tới phiếu tiếp nhận (nếu chat về đợt sửa chữa này)
    
    TrangThai VARCHAR(30) DEFAULT 'DANG_MO', -- 'DANG_MO', 'DA_DONG'
    NgayTao DATETIME2 DEFAULT SYSDATETIME(),
    NgayCapNhatCuoi DATETIME2 DEFAULT SYSDATETIME(),

    FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    FOREIGN KEY (MaNhanVien) REFERENCES NhanVien(MaNhanVien),
    FOREIGN KEY (MaTiepNhan) REFERENCES PhieuTiepNhan(MaTiepNhan)
);
GO
CREATE TABLE TinNhan (
    MaTinNhan INT IDENTITY(1,1) PRIMARY KEY,
    
    MaCuocHoiThoai INT NOT NULL,
    
    MaNguoiGui INT NOT NULL, -- MaNguoiDung của người gửi (Khách hoặc Nhân viên)
    
    NoiDung NVARCHAR(2000),
    DuongDanTep NVARCHAR(500), -- Nếu muốn gửi kèm hình ảnh/file báo giá
    
    DaDoc BIT DEFAULT 0,
    ThoiGianGui DATETIME2 DEFAULT SYSDATETIME(),

    FOREIGN KEY (MaCuocHoiThoai) REFERENCES CuocHoiThoai(MaCuocHoiThoai),
    FOREIGN KEY (MaNguoiGui) REFERENCES NguoiDung(MaNguoiDung)
);
GO
 
 SELECT * FROM NguoiDung