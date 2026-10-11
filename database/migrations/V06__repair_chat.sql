-- Approved by user on 2026-10-09. Additive repair chat migration.
IF DB_NAME() <> N'GarageManagementSystem' THROW 51001, 'Unexpected database.', 1;
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID(N'dbo.RepairChatRoom', N'U') IS NOT NULL
   OR OBJECT_ID(N'dbo.RepairChatMessage', N'U') IS NOT NULL
BEGIN
    ROLLBACK TRANSACTION;
    THROW 51002, 'Repair chat tables already exist. Inspect schema before applying.', 1;
END;

CREATE TABLE dbo.RepairChatRoom (
    receptionId INT NOT NULL PRIMARY KEY,
    deletedAt DATETIME2 NULL,
    deletedBy INT NULL,
    CONSTRAINT FK_RepairChatRoom_Reception FOREIGN KEY (receptionId) REFERENCES dbo.PhieuTiepNhan(MaTiepNhan),
    CONSTRAINT FK_RepairChatRoom_DeletedBy FOREIGN KEY (deletedBy) REFERENCES dbo.NguoiDung(MaNguoiDung),
    CONSTRAINT CK_RepairChatRoom_DeleteAudit CHECK (
        (deletedAt IS NULL AND deletedBy IS NULL) OR (deletedAt IS NOT NULL AND deletedBy IS NOT NULL))
);

CREATE TABLE dbo.RepairChatMessage (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    receptionId INT NOT NULL,
    senderId INT NOT NULL,
    senderRole VARCHAR(20) NOT NULL,
    clientId VARCHAR(64) NOT NULL,
    content NVARCHAR(2000) NOT NULL,
    sentAt DATETIME2 NOT NULL,
    CONSTRAINT FK_RepairChatMessage_Room FOREIGN KEY (receptionId) REFERENCES dbo.RepairChatRoom(receptionId),
    CONSTRAINT FK_RepairChatMessage_Sender FOREIGN KEY (senderId) REFERENCES dbo.NguoiDung(MaNguoiDung),
    CONSTRAINT CK_RepairChatMessage_Role CHECK (senderRole IN ('ADMIN','MANAGER','FRONT_DESK','TECHNICIAN')),
    CONSTRAINT CK_RepairChatMessage_Content CHECK (LEN(LTRIM(RTRIM(content))) > 0),
    CONSTRAINT UQ_RepairChatMessage_Retry UNIQUE (receptionId, senderId, clientId)
);
CREATE INDEX IX_RepairChatMessage_History ON dbo.RepairChatMessage(receptionId, id);

COMMIT;
-- Xoa vinh vien: DELETE cac tin cua phong; giu marker receptionId/deletedAt/deletedBy
-- de audit va ngan tai tao phong cu. Khong xoa anh ho so xe/chung tu.
