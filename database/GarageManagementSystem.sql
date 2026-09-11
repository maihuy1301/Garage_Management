SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

create database GarageManagementSystem
go
use GarageManagementSystem

go

--ROLE
CREATE TABLE VaiTro (
    MaVaiTro INT IDENTITY(1,1) PRIMARY KEY,
    TenVaiTro VARCHAR(50) UNIQUE NOT NULL,
    MoTa NVARCHAR(255)
);

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

CREATE TABLE NguoiDung_VaiTro (
	MaNguoiDung INT NOT NULL,
    MaVaiTro INT NOT NULL,
    PRIMARY KEY (MaNguoiDung, MaVaiTro),
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
    FOREIGN KEY (MaVaiTro) REFERENCES VaiTro(MaVaiTro)
);

CREATE TABLE ChiNhanh (
	MaChiNhanh INT IDENTITY(1,1) PRIMARY KEY,
	TenChiNhanh NVARCHAR(150) NOT NULL,
    DiaChi NVARCHAR(255) NOT NULL,
	SoDienThoai VARCHAR(20),
    Email VARCHAR(100),
    TrangThai BIT DEFAULT 1,
    NgayTao DATETIME2 DEFAULT SYSDATETIME()

);

CREATE TABLE NhanVien (
    MaNhanVien INT IDENTITY(1,1) PRIMARY KEY,
	MaNguoiDung INT UNIQUE NOT NULL,
    MaChiNhanh INT NOT NULL,
    ChucVu NVARCHAR(100),
    NgayVaoLam DATE,
    TrangThai BIT DEFAULT 1,
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh)
);

CREATE TABLE KhachHang (
	MaKhachHang INT IDENTITY(1,1) PRIMARY KEY,
	MaNguoiDung INT UNIQUE NOT NULL,
    DiaChi NVARCHAR(255),
    NgaySinh DATE,
    FOREIGN KEY (MaNguoiDung) REFERENCES NguoiDung(MaNguoiDung)
);

CREATE TABLE HangXe (
    MaHangXe INT IDENTITY(1,1) PRIMARY KEY,
    TenHangXe NVARCHAR(255) NOT NULL,
    TrangThai BIT NOT NULL DEFAULT 1
);

CREATE TABLE ModelXe (
    MaModel INT IDENTITY(1,1) PRIMARY KEY,
    MaHangXe INT NOT NULL,
    TenModel NVARCHAR(100) NOT NULL,
    TrangThai BIT NOT NULL DEFAULT 1,

    FOREIGN KEY (MaHangXe) REFERENCES HangXe(MaHangXe),
    CONSTRAINT UQ_ModelXe_HangXe UNIQUE (MaHangXe, TenModel)
);

CREATE TABLE Xe (
    MaXe INT IDENTITY(1,1) PRIMARY KEY,
	MaKhachHang INT NOT NULL,
    BienSo VARCHAR(20) UNIQUE NOT NULL,
    MaModel INT, --note: Backend cần xử lí các model thuộc về HangXe nào!
    NamSanXuat INT,
    MauXe NVARCHAR(50),
    SoVIN VARCHAR(50), --Số khung xe
	SoKmHienTai INT DEFAULT 0,
    NgayTao DATETIME2 DEFAULT SYSDATETIME(),
    TrangThai BIT DEFAULT 1,
    FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
	FOREIGN KEY (MaModel) REFERENCES ModelXe(MaModel) 
);

CREATE TABLE LoaiDichVu (
	MaLoaiDichVu INT IDENTITY(1,1) PRIMARY KEY,
    TenLoai NVARCHAR(100) NOT NULL,
    MoTa NVARCHAR(255),
    TrangThai BIT DEFAULT 1
);

CREATE TABLE DichVu (
    MaDichVu INT IDENTITY(1,1) PRIMARY KEY,
    MaLoaiDichVu INT NOT NULL,
    TenDichVu NVARCHAR(150) NOT NULL,
    MoTa NVARCHAR(500),
    DonGia DECIMAL(18,2) NOT NULL,
    ThoiGianDuKien INT, --tính bằng giờ
    TrangThai BIT DEFAULT 1,
    FOREIGN KEY (MaLoaiDichVu) REFERENCES LoaiDichVu(MaLoaiDichVu)
);


CREATE TABLE DatLich (
    MaDatLich INT IDENTITY(1,1) PRIMARY KEY,
	MaKhachHang INT NOT NULL,
	MaNhanVienXacNhan INT,
    MaXe INT NOT NULL,
    MaChiNhanh INT NOT NULL,
	ThoiGianHen DATETIME2 NOT NULL,
    TrangThai VARCHAR(30) NOT NULL DEFAULT 'CHO_XAC_NHAN',
    GhiChu NVARCHAR(500),
    NgayDat DATETIME2 DEFAULT SYSDATETIME(),
    FOREIGN KEY (MaKhachHang) REFERENCES KhachHang(MaKhachHang),
    FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh),
	FOREIGN KEY (MaNhanVienXacNhan) REFERENCES NhanVien(MaNhanVien)
);

--CHO_XAC_NHAN | DA_XAC_NHAN | DA_TIEP_NHAN | DANG_XU_LY | HOAN_TAT | HUY
-- Một lịch có thể chọn nhiều dịch vụ.
-- Mỗi dịch vụ chỉ được chọn 1 lần trong một lịch.
CREATE TABLE DatLich_DichVu (
    MaDatLich INT NOT NULL,
    MaDichVu INT NOT NULL,
    GhiChu NVARCHAR(255),
    PRIMARY KEY (MaDatLich, MaDichVu),
    FOREIGN KEY (MaDatLich) REFERENCES DatLich(MaDatLich),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);

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

CREATE TABLE PhieuSuaChua (
	MaPhieuSuaChua INT IDENTITY(1,1) PRIMARY KEY,
    MaTiepNhan INT NOT NULL,
    MaChiNhanh INT NOT NULL,
	MaPhieuCha INT NULL,
	ThoiGianBatDau DATETIME2,
	ThoiGianHoanTat DATETIME2,
    TrangThai VARCHAR(30) DEFAULT 'CHO_XU_LY',
    GhiChu NVARCHAR(1000),
    FOREIGN KEY (MaTiepNhan) REFERENCES PhieuTiepNhan(MaTiepNhan),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh),
	FOREIGN KEY (MaPhieuCha) REFERENCES PhieuSuaChua(MaPhieuSuaChua)
);
--CHO_XU_LY | DA_PHAN_CONG | DANG_SUA | CHO_KH_DUYET | TAM_DUNG | HOAN_TAT | HUY

CREATE TABLE PhieuSuaChua_DichVu (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuSuaChua INT NOT NULL,
    MaDichVu INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    ThanhTien AS (DonGia) PERSISTED,
    TrangThai VARCHAR(30) DEFAULT 'CHO_XU_LY',
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu)
);

CREATE TABLE PhanCong (
    MaPhanCong INT IDENTITY(1,1) PRIMARY KEY,
    MaPhieuSuaChua INT NOT NULL,
    MaQuanLy INT NOT NULL,
	MaNhanVienDuocPhanCong INT NOT NULL,
	ThoiGianPhanCong DATETIME2 DEFAULT SYSDATETIME(),
    TrangThai VARCHAR(30) DEFAULT 'DA_GIAO',
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    FOREIGN KEY (MaQuanLy) REFERENCES NhanVien(MaNhanVien),
	FOREIGN KEY (MaNhanVienDuocPhanCong) REFERENCES NhanVien(MaNhanVien)
);

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

CREATE TABLE PhuTung (
    MaPhuTung INT IDENTITY(1,1) PRIMARY KEY,
    MaPhuTungCode VARCHAR(30) NOT NULL,
    TenPhuTung NVARCHAR(150) NOT NULL,
    DonViTinh NVARCHAR(30),
    GiaNhap DECIMAL(18,2),
    GiaBan DECIMAL(18,2),
    TrangThai BIT DEFAULT 1
);
CREATE TABLE PhieuSuaChua_PhuTung (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
	MaPhieuSuaChua INT NOT NULL,
    MaPhuTung INT NOT NULL,
	MaDichVuChiTiet INT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua),
    FOREIGN KEY (MaPhuTung) REFERENCES PhuTung(MaPhuTung),
	FOREIGN KEY (MaDichVuChiTiet) REFERENCES PhieuSuaChua_DichVu(MaChiTiet)
);
CREATE TABLE DichVu_PhuTung (
    MaDichVu INT NOT NULL,
    MaPhuTung INT NOT NULL,
	SoLuong INT, --Đại diện cho số lượng phụ tùng sử dụng
    PRIMARY KEY (MaDichVu, MaPhuTung),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu),
    FOREIGN KEY (MaPhuTung) REFERENCES PhuTung(MaPhuTung)
);

CREATE TABLE GiaoDichKho ( --Tự động lưu khi có giao dịch nhập xuất đối với phụ tùng
    MaGiaoDich INT IDENTITY(1,1) PRIMARY KEY,
	MaChiNhanh INT NOT NULL,
    MaPhuTung INT NOT NULL,
	LoaiGiaoDich VARCHAR(30) NOT NULL, --NHAP (MANAGER), XUAT(TECHNICIAN)
    SoLuong INT NOT NULL,
	MaPhieuSuaChua INT NULL,
    GhiChu NVARCHAR(500),
    ThoiGian DATETIME2 DEFAULT SYSDATETIME(),
	FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh),
    FOREIGN KEY (MaPhuTung) REFERENCES PhuTung(MaPhuTung),
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua)
);

CREATE TABLE TonKho (
    MaChiNhanh INT NOT NULL,
    MaPhuTung INT NOT NULL,
    SoLuongTon INT NOT NULL DEFAULT 0,
    SoLuongToiThieu INT NOT NULL DEFAULT 0,
    PRIMARY KEY (MaChiNhanh, MaPhuTung),
    FOREIGN KEY (MaChiNhanh) REFERENCES ChiNhanh(MaChiNhanh),
    FOREIGN KEY (MaPhuTung) REFERENCES PhuTung(MaPhuTung)
);

CREATE TABLE HoaDon (
    MaHoaDon INT IDENTITY(1,1) PRIMARY KEY,
	MaPhieuSuaChua INT,
	MaKhachHang INT,
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

CREATE TABLE HoaDon_DichVu (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaHoaDon INT NOT NULL,
    MaPhieuDichVu INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    ThanhTien AS (DonGia) PERSISTED,
    FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon),
    FOREIGN KEY (MaPhieuDichVu) REFERENCES PhieuSuaChua_DichVu(MaChiTiet)
);

CREATE TABLE HoaDon_PhuTung (
    MaChiTiet INT IDENTITY(1,1) PRIMARY KEY,
    MaHoaDon INT NOT NULL,
	MaDichVuChiTiet INT NULL,
    MaPhieuPhuTung INT NULL,
    SoLuong INT NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL,
    ThanhTien AS (SoLuong * DonGia) PERSISTED,
    FOREIGN KEY (MaHoaDon) REFERENCES HoaDon(MaHoaDon),
	FOREIGN KEY (MaDichVuChiTiet) REFERENCES HoaDon_DichVu(MaChiTiet),
    FOREIGN KEY (MaPhieuPhuTung) REFERENCES PhieuSuaChua_PhuTung(MaChiTiet)
	
);

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

CREATE TABLE NhacBaoDuong (
    MaNhac INT IDENTITY(1,1) PRIMARY KEY,
    MaXe INT NOT NULL,
    MaDichVu INT NULL,
    MaPhieuSuaChua INT NULL,
    NgayDuKien DATE,
    KmDuKien INT,
    TrangThai VARCHAR(30) DEFAULT 'CHO_NHAC',
    DaGuiThongBao BIT DEFAULT 0,
    FOREIGN KEY (MaXe) REFERENCES Xe(MaXe),
    FOREIGN KEY (MaDichVu) REFERENCES DichVu(MaDichVu),
    FOREIGN KEY (MaPhieuSuaChua) REFERENCES PhieuSuaChua(MaPhieuSuaChua)
);


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




