-- Approved 2026-10-11. Run after V06 on existing databases.
-- Fresh installs use GarageManagementSystem.sql (already Vietnamese names).
-- Restart backend with updated mappings after this migration.
SET XACT_ABORT ON;
IF DB_NAME() <> N'GarageManagementSystem' THROW 51001, 'Unexpected database.', 1;
BEGIN TRY
 BEGIN TRANSACTION;
 IF OBJECT_ID(N'dbo.RepairChatRoom', N'U') IS NULL OR OBJECT_ID(N'dbo.RepairChatMessage', N'U') IS NULL
    THROW 51002, 'Expected V06 tables missing; migration may already be applied.', 1;
 IF OBJECT_ID(N'dbo.PhongChatSuaChua') IS NOT NULL OR OBJECT_ID(N'dbo.TinNhanSuaChua') IS NOT NULL
    THROW 51003, 'Target table already exists; inspect schema.', 1;
 SELECT * INTO #RoomsBefore FROM dbo.RepairChatRoom WITH (TABLOCKX, HOLDLOCK);
 SELECT * INTO #MessagesBefore FROM dbo.RepairChatMessage WITH (TABLOCKX, HOLDLOCK);
 ALTER TABLE dbo.RepairChatRoom DROP CONSTRAINT CK_RepairChatRoom_DeleteAudit;
 ALTER TABLE dbo.RepairChatMessage DROP CONSTRAINT CK_RepairChatMessage_Role, CK_RepairChatMessage_Content;
 EXEC sys.sp_rename N'dbo.RepairChatRoom', N'PhongChatSuaChua', N'OBJECT';
 EXEC sys.sp_rename N'dbo.RepairChatMessage', N'TinNhanSuaChua', N'OBJECT';
 EXEC sys.sp_rename N'dbo.PhongChatSuaChua.receptionId', N'MaTiepNhan', N'COLUMN';
 EXEC sys.sp_rename N'dbo.PhongChatSuaChua.deletedAt', N'ThoiDiemXoa', N'COLUMN';
 EXEC sys.sp_rename N'dbo.PhongChatSuaChua.deletedBy', N'MaNguoiXoa', N'COLUMN';
 EXEC sys.sp_rename N'dbo.TinNhanSuaChua.id', N'MaTinNhan', N'COLUMN';
 EXEC sys.sp_rename N'dbo.TinNhanSuaChua.receptionId', N'MaTiepNhan', N'COLUMN';
 EXEC sys.sp_rename N'dbo.TinNhanSuaChua.senderId', N'MaNguoiGui', N'COLUMN';
 EXEC sys.sp_rename N'dbo.TinNhanSuaChua.senderRole', N'VaiTroNguoiGui', N'COLUMN';
 EXEC sys.sp_rename N'dbo.TinNhanSuaChua.clientId', N'MaChongTrung', N'COLUMN';
 EXEC sys.sp_rename N'dbo.TinNhanSuaChua.content', N'NoiDung', N'COLUMN';
 EXEC sys.sp_rename N'dbo.TinNhanSuaChua.sentAt', N'ThoiDiemGui', N'COLUMN';
 EXEC sys.sp_rename N'dbo.FK_RepairChatRoom_Reception', N'FK_PhongChatSuaChua_TiepNhan', N'OBJECT';
 EXEC sys.sp_rename N'dbo.FK_RepairChatRoom_DeletedBy', N'FK_PhongChatSuaChua_NguoiXoa', N'OBJECT';
 EXEC sys.sp_rename N'dbo.FK_RepairChatMessage_Room', N'FK_TinNhanSuaChua_Phong', N'OBJECT';
 EXEC sys.sp_rename N'dbo.FK_RepairChatMessage_Sender', N'FK_TinNhanSuaChua_NguoiGui', N'OBJECT';
 EXEC sys.sp_rename N'dbo.UQ_RepairChatMessage_Retry', N'UQ_TinNhanSuaChua_ChongTrung', N'OBJECT';
 EXEC sys.sp_rename N'dbo.TinNhanSuaChua.IX_RepairChatMessage_History', N'IX_TinNhanSuaChua_LichSu', N'INDEX';
 EXEC(N'ALTER TABLE dbo.PhongChatSuaChua WITH CHECK ADD CONSTRAINT CK_PhongChatSuaChua_ThongTinXoa CHECK ((ThoiDiemXoa IS NULL AND MaNguoiXoa IS NULL) OR (ThoiDiemXoa IS NOT NULL AND MaNguoiXoa IS NOT NULL));
 ALTER TABLE dbo.TinNhanSuaChua WITH CHECK ADD CONSTRAINT CK_TinNhanSuaChua_VaiTro CHECK (VaiTroNguoiGui IN (''ADMIN'',''MANAGER'',''FRONT_DESK'',''TECHNICIAN''));
 ALTER TABLE dbo.TinNhanSuaChua WITH CHECK ADD CONSTRAINT CK_TinNhanSuaChua_NoiDung CHECK (LEN(LTRIM(RTRIM(NoiDung))) > 0);');
 -- Compile verification only after column renames; compare every stored value.
 EXEC(N'
 IF EXISTS (SELECT * FROM #RoomsBefore EXCEPT SELECT MaTiepNhan, ThoiDiemXoa, MaNguoiXoa FROM dbo.PhongChatSuaChua)
 OR EXISTS (SELECT MaTiepNhan, ThoiDiemXoa, MaNguoiXoa FROM dbo.PhongChatSuaChua EXCEPT SELECT * FROM #RoomsBefore)
 OR EXISTS (SELECT * FROM #MessagesBefore EXCEPT SELECT MaTinNhan, MaTiepNhan, MaNguoiGui, VaiTroNguoiGui, MaChongTrung, NoiDung, ThoiDiemGui FROM dbo.TinNhanSuaChua)
 OR EXISTS (SELECT MaTinNhan, MaTiepNhan, MaNguoiGui, VaiTroNguoiGui, MaChongTrung, NoiDung, ThoiDiemGui FROM dbo.TinNhanSuaChua EXCEPT SELECT * FROM #MessagesBefore)
 THROW 51004, ''Data verification failed.'', 1;
 SELECT (SELECT COUNT_BIG(*) FROM dbo.PhongChatSuaChua) AS RoomsPreserved,
        (SELECT COUNT_BIG(*) FROM dbo.TinNhanSuaChua) AS MessagesPreserved;
 ');
 COMMIT;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT > 0 ROLLBACK;
 THROW;
END CATCH;