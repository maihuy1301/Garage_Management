-- Run explicitly against the existing Garage database after reviewing the target.
-- Additive and repeatable; does not recreate tables or remove business data.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID(N'dbo.ThietBiPush', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ThietBiPush (
        TokenHash CHAR(64) NOT NULL PRIMARY KEY,
        FcmToken VARCHAR(2048) NOT NULL,
        MaNguoiDung INT NOT NULL,
        CapNhatLuc DATETIME2 NOT NULL,
        FOREIGN KEY (MaNguoiDung) REFERENCES dbo.NguoiDung(MaNguoiDung)
    );
    CREATE INDEX IX_ThietBiPush_NguoiDung ON dbo.ThietBiPush(MaNguoiDung);
END;
IF OBJECT_ID(N'dbo.BanGiaoXe', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.BanGiaoXe (
        MaTiepNhan INT NOT NULL PRIMARY KEY,
        MaNguoiBanGiao INT NOT NULL,
        ThoiGianBanGiao DATETIME2 NOT NULL,
        GhiChu NVARCHAR(500) NULL,
        FOREIGN KEY (MaTiepNhan) REFERENCES dbo.PhieuTiepNhan(MaTiepNhan),
        FOREIGN KEY (MaNguoiBanGiao) REFERENCES dbo.NguoiDung(MaNguoiDung)
    );
END;
COMMIT TRANSACTION;
