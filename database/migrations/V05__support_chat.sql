-- Approved by user on 2026-10-02. Additive support chat migration.
-- Apply only to GarageManagementSystem; existing business data is preserved.
IF DB_NAME() <> N'GarageManagementSystem' THROW 51001, 'Unexpected database.', 1;
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF OBJECT_ID(N'dbo.SupportConversation', N'U') IS NOT NULL
   OR OBJECT_ID(N'dbo.SupportMessage', N'U') IS NOT NULL
   OR OBJECT_ID(N'dbo.SupportReadCursor', N'U') IS NOT NULL
BEGIN
    ROLLBACK TRANSACTION;
    THROW 51000, 'Support chat tables already exist. Inspect schema before applying.', 1;
END;
CREATE TABLE dbo.SupportConversation (
    id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    customerId INT NOT NULL REFERENCES dbo.NguoiDung(MaNguoiDung),
    branchId INT NOT NULL REFERENCES dbo.ChiNhanh(MaChiNhanh),
    agentId INT NULL REFERENCES dbo.NguoiDung(MaNguoiDung),
    status VARCHAR(20) NOT NULL,
    updatedAt DATETIME2 NOT NULL,
    lastMessageId BIGINT NULL,
    pendingBotMessageId BIGINT NULL,
    CONSTRAINT UQ_SupportConversation_CustomerBranch UNIQUE (customerId, branchId),
    CONSTRAINT CK_SupportConversation_Status CHECK (status IN ('BOT','WAITING','HUMAN')),
    CONSTRAINT CK_SupportConversation_Agent CHECK
       ((status = 'HUMAN' AND agentId IS NOT NULL) OR (status IN ('BOT','WAITING') AND agentId IS NULL))
);
CREATE INDEX IX_SupportConversation_BranchUpdated ON dbo.SupportConversation(branchId, updatedAt DESC);
CREATE TABLE dbo.SupportMessage (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    conversationId INT NOT NULL REFERENCES dbo.SupportConversation(id),
    senderId INT NULL REFERENCES dbo.NguoiDung(MaNguoiDung),
    senderType VARCHAR(12) NOT NULL,
    content NVARCHAR(2000) NOT NULL,
    clientId VARCHAR(100) NOT NULL,
    createdAt DATETIME2 NOT NULL,
    appointmentId INT NULL REFERENCES dbo.DatLich(MaDatLich),
    requestHash VARCHAR(64) NULL,
    CONSTRAINT UQ_SupportMessage_Client UNIQUE (conversationId, clientId),
    CONSTRAINT CK_SupportMessage_Sender CHECK
       ((senderType IN ('BOT','SYSTEM') AND senderId IS NULL) OR
        (senderType IN ('CUSTOMER','STAFF') AND senderId IS NOT NULL))
);
CREATE INDEX IX_SupportMessage_History ON dbo.SupportMessage(conversationId, id DESC);
CREATE TABLE dbo.SupportReadCursor (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    conversationId INT NOT NULL REFERENCES dbo.SupportConversation(id),
    userId INT NOT NULL REFERENCES dbo.NguoiDung(MaNguoiDung),
    lastReadId BIGINT NOT NULL,
    CONSTRAINT UQ_SupportReadCursor_Member UNIQUE (conversationId, userId),
    CONSTRAINT CK_SupportReadCursor_Positive CHECK (lastReadId >= 0)
);
COMMIT TRANSACTION;