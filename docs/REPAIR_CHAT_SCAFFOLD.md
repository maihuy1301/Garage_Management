# Khung chat theo lần sửa chữa — 07/10/2026

## Đổi tên SQL sang tiếng Việt — 11/10/2026

Người dùng duyệt đổi tên bảng và cột. Đã áp V07__repair_chat_vietnamese_names.sql vào GarageManagementSystem trong garage-sqlserver. RepairChatRoom → PhongChatSuaChua; RepairChatMessage → TinNhanSuaChua.

- PhongChatSuaChua: MaTiepNhan, ThoiDiemXoa, MaNguoiXoa.
- TinNhanSuaChua: MaTinNhan, MaTiepNhan, MaNguoiGui, VaiTroNguoiGui, MaChongTrung, NoiDung, ThoiDiemGui.
- Migration dùng transaction, khóa hai bảng và so sánh mọi giá trị trước/sau; giữ nguyên 2 phòng, 8 tin nhắn. Lần chạy đầu bị SQL Server chặn đổi tên cột thuộc CHECK và đã rollback; bản hoàn chỉnh tạo lại các CHECK trong cùng transaction, kiểm tra enabled/trusted đạt. FK, unique chống trùng, identity và index giữ chức năng; tên constraint/index khai báo cũng đổi sang tiếng Việt.
- Cập nhật GarageManagementSystem.sql cho cài mới; giữ V06 là lịch sử, database đã có V06 dùng V07. Local đã áp V07, không chạy lại migration hoặc toàn bộ schema.
- Cập nhật @Table/@Column/@JoinColumn trong hai entity và native SQL của RepairChatLocalIntegrationTest. Tên lớp/thuộc tính Java và JSON API giữ nguyên để web/mobile tương thích.
- 30 tests backend đạt: 15 service, 4 notifier, 7 controller, 4 SQL integration rollback. Kiểm tra gửi/đọc Unicode, retry, unique, phân trang, bàn giao, xóa và thông báo. Chưa kiểm thử E2E UI sau đổi tên. Không chạy/build Flutter, commit/push/deploy.
- Khởi động lại backend trước khi tiếp tục chat. Trong SSMS refresh Tables để thấy tên mới.

## Trạng thái

Ngày 09/10/2026, người dùng đã duyệt cập nhật và áp database local. Migration `database/migrations/V06__repair_chat.sql` đã áp thành công vào `GarageManagementSystem` trong container `garage-sqlserver`; schema chuẩn đã đồng bộ. `.env` local đã bật `GARAGE_REPAIR_CHAT_ENABLED=true`. Cần khởi động backend để nhận cấu hình. Chưa chạy thử E2E web/mobile.

Trước đó ngày 07/10 chỉ làm khung code và chờ database; yêu cầu ngày 09/10 thay thế việc trì hoãn này.

Đây là phạm vi mới thay cho đề xuất nhóm chat chi nhánh trước đó: chỉ chat trong lần sửa; chỉ chữ, không camera/upload; không triển khai lịch sử phiếu sửa chữa hay lịch sử bảo dưỡng.

## Cách mở

- Web: Phiếu sửa chữa → chi tiết → **Trao đổi sửa chữa**.
- Web: Lệnh sửa chữa → menu ba chấm của lệnh chính hoặc phát sinh → **Trao đổi sửa chữa** để mở trực tiếp, không cần thông báo mới.
- Mobile TECHNICIAN: Công việc → chi tiết phiếu → **Trao đổi với tiếp tân**.
- Chat hiển thị biển số, tên xe, mã phiếu đại diện, chi nhánh và ảnh hồ sơ xe nếu có khi API được bật. Khi chưa có ảnh dùng biểu tượng xe.
- Khách CUSTOMER không có điểm vào và bị backend chặn.

## Thiết kế

Một phòng theo `MaTiepNhan`, gom phiếu chính/phát sinh cùng lần sửa; xe quay lại tạo lần tiếp nhận mới nên có chat mới. Phòng được xác định từ hồ sơ sửa chữa hiện có, chưa cần bản ghi cho phòng trống; bản ghi được tạo khi gửi tin đầu tiên. Đây là khởi tạo lười, không có hook tự INSERT khi quản lý phân công.

Trạng thái suy ra ở server:

| Trạng thái | Hành vi |
| --- | --- |
| WAITING_ASSIGNMENT | Chưa có phân công DA_DUYET cho phiếu không hủy; chưa cho gửi |
| OPEN | Đã có phân công; trao đổi chữ |
| ARCHIVED | Tiếp nhận DA_BAN_GIAO hoặc trạng thái legacy HOAN_TAT; chỉ đọc |
| CLOSED | Lần tiếp nhận/các phiếu đều hủy; chỉ đọc, không mở quyền xóa dựa trên hủy |
| DELETED | Tin đã xóa vĩnh viễn, không cho tạo lại chat cùng lần sửa |

Phiếu sửa chữa HOAN_TAT chưa khóa chat nếu tiếp nhận chưa bàn giao. Không có trang Lưu trữ riêng: chat lưu trữ mở ngay từ chi tiết phiếu cũ theo quyền hiện tại.

## API

Tất cả response JSON dùng `ApiResponse`, trừ ảnh binary.

| Method | Endpoint | Mục đích |
| --- | --- | --- |
| GET | /api/repair-chat/capabilities | `{enabled:false}` khi chưa bật; không truy cập bảng chat |
| GET | /api/repair-chat/orders/{id}/messages | Metadata xe/phòng + 40 tin gần nhất, thứ tự tăng dần |
| GET | .../messages?before={id} | 40 tin cũ hơn; `hasMore` để tải tiếp |
| GET | .../messages?after={id} | 40 tin mới hơn; không dùng cùng before |
| POST | .../messages | `{content,clientId}`; 1–2000 ký tự, retry key 8–64 ký tự chữ/số/gạch ngang/gạch dưới |
| GET | /api/repair-chat/orders/{id}/vehicle-image | Ảnh hồ sơ xe, kiểm tra quyền chat trước khi đọc; no-store |
| DELETE | /api/repair-chat/orders/{id} | Xóa tin cho mọi người, chỉ sau bàn giao; FRONT_DESK/ADMIN |

Server tự tìm xe/chi nhánh/tài khoản gửi từ JWT và phiếu, không nhận owner/role/branch từ client. Thợ phải có phân công DA_DUYET trên ít nhất một phiếu cùng lần tiếp nhận, cùng chi nhánh; bị gỡ phân công thì mất quyền đọc/gửi/ảnh. MANAGER cùng chi nhánh có thể đọc/gửi nhưng không xóa. ADMIN được truy cập toàn hệ thống.

Xóa giữ một marker tối thiểu `receptionId/deletedAt/deletedBy`, không giữ nội dung. Không xóa phiếu, hóa đơn, báo giá hoặc ảnh hồ sơ xe. UI web yêu cầu xác nhận xóa vĩnh viễn, không có nút xóa dành cho thợ. Database backup ngoài ứng dụng không thuộc phạm vi thao tác xóa này.

## Đồng bộ và lưu trữ

- Hai entity `RepairChatRoom`, `RepairChatMessage`; migration chính thức V06 đã duyệt/áp local ngày 09/10/2026. Bản đề xuất cũ trong Work Sessions chỉ để tham khảo lịch sử.
- Chỉ mục `(receptionId,id)` phục vụ phân trang; unique `(receptionId,senderId,clientId)` chống gửi trùng.
- Gửi/xóa dùng khóa tiếp nhận cùng cơ chế bàn giao. Đã chạy truy vấn trên SQL Server thật; chưa kiểm thử tranh chấp giữa nhiều transaction đồng thời.
- Nội dung chat tải tin mới định kỳ 5 giây khi màn hoạt động. Mỗi lần lấy tối đa 40 tin; lô tiếp theo tiếp tục từ ID mới nhất.
- Thông báo cho tiếp tân: tin mới từ TECHNICIAN tạo `REPAIR_CHAT_MESSAGE` trong bảng `ThongBao` hiện hữu cho tiếp tân đang hoạt động cùng chi nhánh. Gửi lại cùng clientId không tạo thêm thông báo. WebSocket riêng `/user/queue/notifications` phát sau commit qua NotificationService; chuông web có badge/toast và tải bù REST mỗi 10 giây khi trang hiện. Không thêm schema/migration.
- Bấm thông báo đánh dấu thông báo đó đã đọc và mở `/app/repair-orders/:id/chat`, được bảo vệ bằng role và quyền backend. Badge đếm thông báo chưa đọc, không phải trạng thái đọc của từng phòng. Không sao chép nội dung tin chat vào thông báo để tránh giữ nội dung sau xóa vĩnh viễn. Tin cũ trước thay đổi này không tự sinh lại thông báo.
- Mobile xóa dữ liệu phiên khi logout/đổi tài khoản, bỏ phản hồi cũ; web remount theo user/phiếu. 401/403/404 xóa tin đã hiển thị. Client khác nhận trạng thái xóa/bàn giao ở lần tải tiếp theo.
- API mặc định tắt qua `@Value("${GARAGE_REPAIR_CHAT_ENABLED:false}")`. `.env` local đã bật true sau khi áp V06; mẫu cấu hình giữ false cho môi trường chưa có bảng. `ddl-auto=none` hiện hữu giữ nguyên.

## Kiểm tra ngày 09/10/2026

- Bổ sung thông báo: 36 targeted backend tests (15 service, 4 notifier, 7 controller, 10 notification service) và 4 SQL integration rollback đạt; kiểm tra chọn tiếp tân cùng chi nhánh, bỏ tài khoản ngừng hoạt động, retry không trùng. Web build và 3 STOMP tests đạt. Chưa kiểm thử E2E popup trên trình duyệt với mobile thật.
- Chạy thử thông báo: khởi động lại backend, tải lại web tiếp tân, gửi tin mới từ mobile thợ. Chuông → thông báo xe → mở trao đổi. `/app/chat` vẫn là hỗ trợ khách hàng; không phải phòng sửa chữa.

- `REPAIR_CHAT_LOCAL_INTEGRATION=true` + `mvn -q "-Dtest=RepairChatLocalIntegrationTest,RepairChatServiceTest,RepairChatControllerTest" test`: 25 tests đạt, không skip; 3 SQL integration thực sự chạy.
- SQL integration kiểm tra ghi/đọc chữ Việt, retry không trùng, unique constraint từ chối trùng mã, phân trang before/after, chat đóng sau bàn giao, xóa tin và giữ marker. Test dùng transaction rollback, không để lại tin hay thay đổi trạng thái nghiệp vụ.
- Kiểm tra sau test: 2 bảng chat trống, số dòng toàn bộ bảng cũ bằng trước migration. Đã xác minh các index tồn tại. Chưa kiểm thử tải, cạnh tranh transaction hoặc E2E giữa web/mobile.

## Kiểm tra khung code ngày 07/10/2026

- Backend: `mvn -q "-Dtest=RepairChatServiceTest,RepairChatControllerTest,VehicleImageServiceTest,VehicleHandoverServiceTest" test` — 34 tests đạt; có 22 tests chat mới, kiểm tra RBAC/branch/assignment, trạng thái đóng/xóa, retry và phân trang.
- Mobile: `flutter test test/features/repair_chat test/features/technician test/features/invoices/invoice_routes_test.dart` — 25 tests đạt; 11 tests chat mới gồm giả lập mạng/logout/phân trang và widget ở 390×844.
- `flutter analyze` sạch. Không chạy `flutter run` hoặc `flutter build`.
- `npm run build` đạt, cảnh báo bundle >500 kB. `npm run lint` chưa chạy được vì repo thiếu ESLint config.
- Chưa SQL integration, E2E giữa hai thiết bị, kiểm thử tải, hoặc kiểm tra web bằng trình duyệt. Không dùng kết quả mock để khẳng định hiệu năng database.

## Chạy thử local

Database đang chạy và V06 đã áp; không chạy lại migration hoặc toàn bộ schema. Khởi động backend theo cách thường dùng (ví dụ `mvn spring-boot:run` từ backend/), web và app do người dùng chạy. Chọn cùng lần sửa có phân công DA_DUYET: tiếp tân mở tab chat trong chi tiết phiếu; thợ mở công việc tương ứng → Trao đổi với tiếp tân. Tin cập nhật khi màn mở, khoảng 5 giây/lần. Hoàn tất sửa chưa đóng chat; bàn giao mới chuyển lưu trữ, tiếp tân mới có quyền xóa vĩnh viễn.
