# Bàn giao cho nhóm web: thông báo tiến độ và bàn giao xe

Cập nhật: 2026-09-22. Code đã triển khai; migration **chưa chạy trên SQL Server**. Người dùng đã duyệt sửa schema và tạo migration, chưa duyệt áp dụng database đang chạy.

## 1. Phạm vi và thứ tự tích hợp

SQL Server vẫn là cơ sở dữ liệu nghiệp vụ. Firebase Cloud Messaging (FCM) chỉ chuyển thông báo từ backend đến Android, không thay SQL Server bằng Firestore.

1. Review migration ở mục 2, sao lưu và xác nhận đúng database trước khi người phụ trách DB thực thi.
2. Đồng bộ backend, web và mobile cùng phiên bản. Không chỉ lấy phần giao diện bàn giao.
3. Cấu hình FCM theo [FCM_SETUP.md](FCM_SETUP.md). Bàn giao và inbox REST không phụ thuộc việc bật FCM.
4. Khởi động lại backend/web. Người dùng dừng và chạy lại Flutter đầy đủ vì có thay đổi Android native.
5. Kiểm thử ma trận ở mục 7 trước demo.

## 2. Database đã sửa như thế nào?

Schema chuẩn thực tế của repo: `database/GarageManagementSystem.sql` (một số tài liệu cũ còn ghi `GarageSystemDB.sql`). Thêm hai bảng, không xóa bảng/cột hay chuyển dữ liệu cũ sang Firebase.

### ThietBiPush

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| TokenHash | CHAR(64), PK | SHA-256 của token, khóa ngắn để tra cứu/upsert |
| FcmToken | VARCHAR(2048), NOT NULL | Token gốc cần cho FCM gửi đến thiết bị |
| MaNguoiDung | INT, NOT NULL, FK NguoiDung | Chủ sở hữu hiện tại, backend lấy từ JWT |
| CapNhatLuc | DATETIME2, NOT NULL | Lần đăng ký/làm mới gần nhất |

Index `IX_ThietBiPush_NguoiDung` hỗ trợ tìm thiết bị của một tài khoản. Một người có nhiều thiết bị; một token chỉ thuộc một người tại một thời điểm. SHA-256 là khóa tra cứu, **không phải mã hóa token gốc**. Hạn chế quyền đọc bảng và không đưa token vào log/tài liệu.

### BanGiaoXe

| Cột | Kiểu | Ý nghĩa |
|---|---|---|
| MaTiepNhan | INT, PK, FK PhieuTiepNhan | Mỗi lần tiếp nhận chỉ có một lần bàn giao |
| MaNguoiBanGiao | INT, NOT NULL, FK NguoiDung | Nhân viên xác nhận, backend lấy từ JWT |
| ThoiGianBanGiao | DATETIME2, NOT NULL | Thời gian backend ghi nhận |
| GhiChu | NVARCHAR(500), NULL | Ghi chú tùy chọn, tối đa 500 ký tự |

Không dùng việc thanh toán hay hoàn tất sửa chữa để suy diễn rằng khách đã nhận xe. Bảng mới lưu chứng từ bàn giao riêng; không tự tạo audit cho dữ liệu cũ.

### Cách áp dụng cho database hiện hữu

File: `database/migrations/V03__push_devices_and_vehicle_handover.sql`.

Trong SSMS, chọn đúng server/database Garage, kiểm tra `SELECT DB_NAME();`, sao lưu theo quy trình của nhóm rồi thực thi **file migration**, không chạy lại toàn bộ schema khởi tạo. Script dùng transaction, `XACT_ABORT ON` và chỉ tạo bảng còn thiếu bằng `OBJECT_ID`; không xóa dữ liệu. Đây là script chạy thủ công, không có runner tự áp dụng V03 vào volume cũ.

Kiểm tra sau khi chạy:

```sql
SELECT DB_NAME() AS DatabaseDangChon;
SELECT OBJECT_ID(N'dbo.ThietBiPush', N'U') AS ThietBiPush,
       OBJECT_ID(N'dbo.BanGiaoXe', N'U') AS BanGiaoXe;
EXEC sp_help N'dbo.ThietBiPush';
EXEC sp_help N'dbo.BanGiaoXe';
```

Chạy lại script sẽ bỏ qua bảng đã tồn tại, **không sửa một bảng cùng tên nhưng sai cấu trúc**. Cần so sánh cột/khóa trước khi đưa vào môi trường chung. Không có rollback xóa bảng tự động vì có thể làm mất audit/token. Database mới dùng schema khởi tạo đã có hai bảng này.

## 3. Trạng thái nghiệp vụ

| Đối tượng | Trạng thái/mốc | Ý nghĩa |
|---|---|---|
| DatLich | DA_XAC_NHAN | Cửa hàng đã xác nhận lịch |
| DatLich | DA_TIEP_NHAN | Xe đã vào garage |
| PhieuSuaChua | HOAN_TAT | Công việc sửa chữa hoàn tất; phiếu chính gửi lời mời đến kiểm tra, thanh toán và nhận xe |
| HoaDon | DA_THANH_TOAN | Hóa đơn đã được thanh toán đầy đủ |
| PhieuTiepNhan | DA_BAN_GIAO (mới) | Nhân viên đã xác nhận giao xe thực tế |
| DatLich | HOAN_TAT | Endpoint bàn giao đặt lịch liên quan về hoàn tất |

Phiếu con hoàn tất chỉ báo công việc bổ sung hoàn tất, không gửi lời mời nhận toàn bộ xe. Luồng cũ có thể vẫn cho cập nhật `DatLich.HOAN_TAT` độc lập: không dùng trạng thái lịch hẹn làm bằng chứng bàn giao, luôn đọc `BanGiaoXe` qua API.

Điều kiện bàn giao do backend quyết định: phiếu tiếp nhận chưa hủy/kết thúc; có ít nhất một phiếu sửa chữa không hủy; **tất cả** phiếu sửa chữa không hủy (gồm phiếu con) phải `HOAN_TAT`, có hóa đơn và hóa đơn phải `DA_THANH_TOAN`. Legacy reception `HOAN_TAT` không được tạo thêm bàn giao. Muốn xử lý dữ liệu legacy cần quy trình riêng, không tự backfill.

## 4. API cho bên web

Tất cả request dùng `Authorization: Bearer <JWT>`, response giữ envelope `ApiResponse`: `success`, `message`, `data`.

### GET /api/reception/{id}/handover

Quyền: `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_FRONT_DESK`; backend kiểm tra phạm vi chi nhánh.

```json
{
  "success": true,
  "message": "Thông tin bàn giao xe",
  "data": {
    "eligible": true,
    "reason": null,
    "thoiGianBanGiao": null,
    "tenNguoiBanGiao": null,
    "ghiChu": null
  }
}
```

Nếu chưa đủ điều kiện: `eligible=false`, `reason` chứa lý do. Nếu đã bàn giao: `eligible=false`, `thoiGianBanGiao` có giá trị cùng người thực hiện và ghi chú. Không được hiểu mọi `eligible=false` là đã bàn giao. Thời gian hiện là LocalDateTime của backend, không có offset; các máy demo nên cùng múi giờ Asia/Ho_Chi_Minh.

### POST /api/reception/{id}/handover

Body tùy chọn: `{"ghiChu":"Đã đối chiếu và giao chìa khóa cho khách"}`. Không gửi nhân viên, thời gian, customer hoặc branch từ client.

Response 200 có cùng cấu trúc `data` như GET, với thông tin audit. Backend khóa phiếu tiếp nhận, kiểm tra lại điều kiện, ghi audit + cập nhật trạng thái + lưu thông báo trong cùng transaction. Request lặp trả audit cũ, không tạo thêm thông báo và không sửa ghi chú cũ. Luồng tạo phiếu sửa chữa cũng khóa cùng phiếu tiếp nhận và từ chối mở mới sau bàn giao.

| HTTP | Bên web xử lý |
|---|---|
| 200 | Hiển thị audit, cập nhật chi tiết/danh sách tiếp nhận |
| 400 | Hiển thị `message`: không đủ điều kiện hoặc ghi chú quá dài |
| 401 | Luồng hết phiên/đăng nhập hiện hành |
| 403 | Không có role hoặc không thuộc chi nhánh được phép |
| 404 | Phiếu tiếp nhận không tồn tại |

GET chỉ hỗ trợ hiển thị; POST vẫn kiểm tra lại vì trạng thái có thể thay đổi. Không tự cập nhật `DA_BAN_GIAO` chỉ ở client và không trực tiếp INSERT SQL từ web.

### Các file web đã triển khai

- `frontend/src/types/reception.types.ts`: thêm `DA_BAN_GIAO`, nhãn trạng thái, `HandoverResponse`.
- `frontend/src/features/reception/services/reception.service.ts`: `getHandover` và `handover`, dùng API client và endpoint reception tập trung.
- `frontend/src/features/reception/components/HandoverPanel.tsx`: tải điều kiện, lý do bị chặn, thử lại, ghi chú, checkbox xác nhận giao xe thực tế, nút gửi và audit sau thành công.
- `frontend/src/features/reception/components/ReceptionDetailModal.tsx`: panel trong vùng cuộn; ẩn tạo phiếu sửa chữa khi đã bàn giao.
- `frontend/src/features/reception/pages/ReceptionPage.tsx`: bộ lọc/đếm trạng thái mới, callback tải lại danh sách và cập nhật chi tiết.

Vị trí thao tác: web tiếp nhận xe -> mở chi tiết -> phần **Bàn giao xe**. Nhóm web không cần gọi FCM trực tiếp, không lưu service-account vào frontend và không cần tự sinh thông báo khi bấm nút.

## 5. API thiết bị dành cho mobile

`POST /api/notifications/devices` và `DELETE /api/notifications/devices`, body `{"token":"<FCM_TOKEN>"}`, chỉ CUSTOMER; thành công trả 200 với `data=null`. Không có API liệt kê token công khai. Server xác định người dùng từ JWT; xóa luôn kèm owner để không xóa đăng ký của người khác.

Mobile đăng ký sau khi có quyền, đăng ký lại khi token thay đổi/resume, ngắt đăng ký khi logout. Nếu cả hủy đăng ký ở backend và xóa token FCM đều thất bại thì giữ phiên và báo khách thử lại. Backend chỉ gửi đến token cập nhật trong 60 ngày gần nhất, bỏ qua token cũ hơn (không tự xóa theo lịch), xóa token khi FCM trả `UNREGISTERED`.

## 6. Luồng thông báo

Business service -> `CustomerProgressNotifier`/`VehicleHandoverService` -> `NotificationService` -> lưu SQL -> commit -> STOMP và FCM async. Không gửi trước commit để tránh báo một thay đổi đã rollback. FCM lỗi không rollback nghiệp vụ; khách vẫn có inbox REST.

Foreground: monitor app nhận STOMP/REST, hiển thị local alert cho mục mới. Background/terminated bình thường: Android hiển thị notification payload của FCM. Bấm mở inbox khi account khớp; resume tải bù, không tạo thêm local alert cho lô thông báo nền. Nội dung lock screen FCM là thông báo chung để hạn chế lộ dữ liệu xe; chi tiết giai đoạn và lời mời nhận xe nằm trong inbox sau đăng nhập.

## 7. Ma trận nghiệm thu và giới hạn

| Tình huống | Kết quả mong đợi |
|---|---|
| Web xác nhận, tiếp nhận, phân công, cập nhật sửa chữa | Khách đúng xe nhận mục mới; tài khoản khác không nhận |
| Phiếu chính hoàn tất | Có lời mời đến garage nhận xe |
| Phiếu con chưa hoàn tất hoặc hóa đơn chưa đủ tiền | Bàn giao bị chặn ở backend |
| Đủ điều kiện, nhân viên đúng chi nhánh xác nhận | Audit một dòng, reception DA_BAN_GIAO, thông báo đã bàn giao |
| Bấm/gửi POST lặp | Cùng audit, không thêm thông báo |
| CUSTOMER/TECHNICIAN hoặc nhân viên khác chi nhánh gọi bàn giao | Bị từ chối |
| App nền/đóng bình thường, có mạng/quyền/FCM hợp lệ | Có push hệ thống; bấm vào mở đúng inbox |
| Đăng xuất/đổi tài khoản | Hủy/rebind token; không mở inbox tài khoản cũ |

Đã kiểm tra 119 targeted backend tests (112 service + 7 MVC role/validation), 71 Flutter tests, analyze sạch và build web. Panel web được smoke-test với API giả lập ở 1280/390px: bị chặn, lỗi/thử lại, xác nhận, thành công, không tràn ngang. Chưa kiểm tra UI cùng backend/database thật. Lint web bị chặn vì repo thiếu ESLint config; build còn cảnh báo bundle >500 KB. Chưa áp migration, chưa kiểm tra SQL locking/concurrency trên DB thật, chưa kiểm thử FCM thật hoặc native Android; chưa có credentials. Push hiện là best-effort, chưa có durable outbox/retry sau khi process backend chết; gửi thành công tới FCM không đồng nghĩa thiết bị đã hiển thị. Không hỗ trợ iOS/APNs trong đợt này.
