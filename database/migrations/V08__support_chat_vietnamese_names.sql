-- Approved 2026-10-11. Apply after V05 to existing databases only.
-- customerId and agentId retain their names and NguoiDung foreign keys.
-- Fresh installs use GarageManagementSystem.sql. Restart backend with new mappings.
SET XACT_ABORT ON;
SET NOCOUNT ON;
IF DB_NAME() <> N'GarageManagementSystem' THROW 51001, 'Unexpected database.', 1;
BEGIN TRY
BEGIN TRANSACTION;
IF OBJECT_ID(N'dbo.SupportConversation', N'U') IS NULL OR OBJECT_ID(N'dbo.HoiThoaiHoTro') IS NOT NULL THROW 51002, 'Unexpected support schema; inspect before applying.', 1;
SELECT * INTO #SupportConversationBefore FROM dbo.SupportConversation WITH (TABLOCKX, HOLDLOCK);
IF OBJECT_ID(N'dbo.SupportMessage', N'U') IS NULL OR OBJECT_ID(N'dbo.TinNhanHoTro') IS NOT NULL THROW 51002, 'Unexpected support schema; inspect before applying.', 1;
SELECT * INTO #SupportMessageBefore FROM dbo.SupportMessage WITH (TABLOCKX, HOLDLOCK);
IF OBJECT_ID(N'dbo.SupportReadCursor', N'U') IS NULL OR OBJECT_ID(N'dbo.TrangThaiDocHoTro') IS NOT NULL THROW 51002, 'Unexpected support schema; inspect before applying.', 1;
SELECT * INTO #SupportReadCursorBefore FROM dbo.SupportReadCursor WITH (TABLOCKX, HOLDLOCK);
ALTER TABLE dbo.SupportConversation DROP CONSTRAINT CK_SupportConversation_Status, CK_SupportConversation_Agent;
ALTER TABLE dbo.SupportMessage DROP CONSTRAINT CK_SupportMessage_Sender;
ALTER TABLE dbo.SupportReadCursor DROP CONSTRAINT CK_SupportReadCursor_Positive;
EXEC sys.sp_rename N'dbo.SupportConversation', N'HoiThoaiHoTro', N'OBJECT';
EXEC sys.sp_rename N'dbo.HoiThoaiHoTro.id', N'MaHoiThoai', N'COLUMN';
EXEC sys.sp_rename N'dbo.HoiThoaiHoTro.branchId', N'MaChiNhanh', N'COLUMN';
EXEC sys.sp_rename N'dbo.HoiThoaiHoTro.status', N'TrangThai', N'COLUMN';
EXEC sys.sp_rename N'dbo.HoiThoaiHoTro.updatedAt', N'ThoiDiemCapNhat', N'COLUMN';
EXEC sys.sp_rename N'dbo.HoiThoaiHoTro.lastMessageId', N'MaTinNhanCuoi', N'COLUMN';
EXEC sys.sp_rename N'dbo.HoiThoaiHoTro.pendingBotMessageId', N'MaTinNhanChoBotXuLy', N'COLUMN';
EXEC sys.sp_rename N'dbo.SupportMessage', N'TinNhanHoTro', N'OBJECT';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.id', N'MaTinNhan', N'COLUMN';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.conversationId', N'MaHoiThoai', N'COLUMN';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.senderId', N'MaNguoiGui', N'COLUMN';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.senderType', N'LoaiNguoiGui', N'COLUMN';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.content', N'NoiDung', N'COLUMN';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.clientId', N'MaChongTrung', N'COLUMN';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.createdAt', N'ThoiDiemGui', N'COLUMN';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.appointmentId', N'MaDatLich', N'COLUMN';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.requestHash', N'MaBamYeuCau', N'COLUMN';
EXEC sys.sp_rename N'dbo.SupportReadCursor', N'TrangThaiDocHoTro', N'OBJECT';
EXEC sys.sp_rename N'dbo.TrangThaiDocHoTro.id', N'MaTrangThaiDoc', N'COLUMN';
EXEC sys.sp_rename N'dbo.TrangThaiDocHoTro.conversationId', N'MaHoiThoai', N'COLUMN';
EXEC sys.sp_rename N'dbo.TrangThaiDocHoTro.userId', N'MaNguoiDung', N'COLUMN';
EXEC sys.sp_rename N'dbo.TrangThaiDocHoTro.lastReadId', N'MaTinNhanDaDocCuoi', N'COLUMN';
EXEC sys.sp_rename N'dbo.UQ_SupportConversation_CustomerBranch', N'UQ_HoiThoaiHoTro_KhachChiNhanh', N'OBJECT';
EXEC sys.sp_rename N'dbo.HoiThoaiHoTro.IX_SupportConversation_BranchUpdated', N'IX_HoiThoaiHoTro_ChiNhanhCapNhat', N'INDEX';
EXEC sys.sp_rename N'dbo.UQ_SupportMessage_Client', N'UQ_TinNhanHoTro_ChongTrung', N'OBJECT';
EXEC sys.sp_rename N'dbo.TinNhanHoTro.IX_SupportMessage_History', N'IX_TinNhanHoTro_LichSu', N'INDEX';
EXEC sys.sp_rename N'dbo.UQ_SupportReadCursor_Member', N'UQ_TrangThaiDocHoTro_ThanhVien', N'OBJECT';
EXEC(N'ALTER TABLE dbo.HoiThoaiHoTro WITH CHECK ADD CONSTRAINT CK_HoiThoaiHoTro_TrangThai CHECK (TrangThai IN (''BOT'',''WAITING'',''HUMAN''));');
EXEC(N'ALTER TABLE dbo.HoiThoaiHoTro WITH CHECK ADD CONSTRAINT CK_HoiThoaiHoTro_NguoiHoTro CHECK ((TrangThai = ''HUMAN'' AND agentId IS NOT NULL) OR (TrangThai IN (''BOT'',''WAITING'') AND agentId IS NULL));');
EXEC(N'ALTER TABLE dbo.TinNhanHoTro WITH CHECK ADD CONSTRAINT CK_TinNhanHoTro_NguoiGui CHECK ((LoaiNguoiGui IN (''BOT'',''SYSTEM'') AND MaNguoiGui IS NULL) OR (LoaiNguoiGui IN (''CUSTOMER'',''STAFF'') AND MaNguoiGui IS NOT NULL));');
EXEC(N'ALTER TABLE dbo.TrangThaiDocHoTro WITH CHECK ADD CONSTRAINT CK_TrangThaiDocHoTro_MaTinNhan CHECK (MaTinNhanDaDocCuoi >= 0);');
EXEC(N'IF EXISTS (SELECT * FROM #SupportConversationBefore EXCEPT SELECT MaHoiThoai, customerId, MaChiNhanh, agentId, TrangThai, ThoiDiemCapNhat, MaTinNhanCuoi, MaTinNhanChoBotXuLy FROM dbo.HoiThoaiHoTro) OR EXISTS (SELECT MaHoiThoai, customerId, MaChiNhanh, agentId, TrangThai, ThoiDiemCapNhat, MaTinNhanCuoi, MaTinNhanChoBotXuLy FROM dbo.HoiThoaiHoTro EXCEPT SELECT * FROM #SupportConversationBefore) THROW 51003, ''Data preservation failed.'', 1; SELECT COUNT_BIG(*) AS HoiThoaiHoTro_RowsPreserved FROM dbo.HoiThoaiHoTro;');
EXEC(N'IF EXISTS (SELECT * FROM #SupportMessageBefore EXCEPT SELECT MaTinNhan, MaHoiThoai, MaNguoiGui, LoaiNguoiGui, NoiDung, MaChongTrung, ThoiDiemGui, MaDatLich, MaBamYeuCau FROM dbo.TinNhanHoTro) OR EXISTS (SELECT MaTinNhan, MaHoiThoai, MaNguoiGui, LoaiNguoiGui, NoiDung, MaChongTrung, ThoiDiemGui, MaDatLich, MaBamYeuCau FROM dbo.TinNhanHoTro EXCEPT SELECT * FROM #SupportMessageBefore) THROW 51003, ''Data preservation failed.'', 1; SELECT COUNT_BIG(*) AS TinNhanHoTro_RowsPreserved FROM dbo.TinNhanHoTro;');
EXEC(N'IF EXISTS (SELECT * FROM #SupportReadCursorBefore EXCEPT SELECT MaTrangThaiDoc, MaHoiThoai, MaNguoiDung, MaTinNhanDaDocCuoi FROM dbo.TrangThaiDocHoTro) OR EXISTS (SELECT MaTrangThaiDoc, MaHoiThoai, MaNguoiDung, MaTinNhanDaDocCuoi FROM dbo.TrangThaiDocHoTro EXCEPT SELECT * FROM #SupportReadCursorBefore) THROW 51003, ''Data preservation failed.'', 1; SELECT COUNT_BIG(*) AS TrangThaiDocHoTro_RowsPreserved FROM dbo.TrangThaiDocHoTro;');
COMMIT;
END TRY
BEGIN CATCH
IF @@TRANCOUNT > 0 ROLLBACK;
THROW;
END CATCH;
