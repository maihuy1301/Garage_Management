# PROJECT CONTEXT — Garage Management System

> **Ghi chú cho AI Agent**: File này tổng hợp các quyết định kỹ thuật và trạng thái triển khai thực tế. Đọc file này khi bắt đầu session mới để nắm ngữ cảnh mà không cần scan toàn bộ codebase.

## 1. Môi trường phát triển đã xác minh
- OS: Windows 11 Home (64-bit)
- Java: Oracle JDK `17.0.12`
- Maven: `Apache Maven 3.9.16`
- Node.js: `v24.15.0` | npm `11.12.1`
- Flutter / Dart: `D:\Downloadd\LapTrinhMobile\flutter`
- Git: `2.51.0`

## 2. Backend Module Status
- Framework: Spring Boot 3.2.5, Java 17.
- **Authentication**: Stateless JWT (JJWT 0.12.5), BCryptPasswordEncoder.
  - `POST /api/auth/login` → xác thực `tenDangNhap/email` + BCrypt + `trangThai`. Trả JWT với `roles` claim.
  - `GET /api/auth/me` → trả thông tin user hiện tại.
- **Authorization (RBAC)**: `@EnableMethodSecurity`, `@PreAuthorize("hasRole('...')")`.
  - Convention: `ROLE_SYSTEM_ADMIN`, `ROLE_BRANCH_MANAGER`, `ROLE_RECEPTIONIST`, `ROLE_TECHNICIAN`, `ROLE_CUSTOMER`.
  - Role source: `NguoiDung → NguoiDung_VaiTro → VaiTro` (DB only, never client).
  - Multi-role fully supported.
  - `JwtAuthenticationEntryPoint`: `401 Unauthorized` JSON.
  - `JwtAccessDeniedHandler`: `403 Forbidden` JSON.
- **Branch Authorization**: `BranchAuthorizationService`.
  - Branch source: `NguoiDung → NhanVien → ChiNhanh` (DB only, never client).
  - `SYSTEM_ADMIN` → global access, no branch restriction.
  - `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` → own branch only (403 if cross-branch).
  - `CUSTOMER` → denied from branch-staff endpoints (403 via RBAC).
- **Branch Master Data API**:
  - Base path: `/api/branches` (`GET /api/branches`, `GET /api/branches/{id}`).
  - Controller: `BranchController`, Service: `BranchService`, Repository: `ChiNhanhRepository.findByTrangThaiTrue()`.
  - DTO: `BranchResponse` (maChiNhanh, maChiNhanhCode, tenChiNhanh, diaChi, soDienThoai, email, trangThai, ngayTao).
  - Authorization: `@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'BRANCH_MANAGER', 'RECEPTIONIST', 'TECHNICIAN', 'CUSTOMER')")`.
- **User Management (TASK 06A)**:
  - Base path: `/api/admin/users` (SYSTEM_ADMIN only).
  - DTOs: `UserResponse`, `CreateUserRequest`, `UpdateUserRequest`, `UpdateUserStatusRequest`.
- **Employee Management (TASK 06B)**:
  - Base path: `/api/employees` (SYSTEM_ADMIN: global, BRANCH_MANAGER: own branch).
  - DTOs: `EmployeeResponse`, `CreateEmployeeRequest`, `UpdateEmployeeRequest`, `UpdateEmployeeStatusRequest`.
  - Relationship: `NguoiDung (1-1) NhanVien (N-1) ChiNhanh`.
- **Reports & Statistics Management (TASK 19)**:
  - Base paths: `/api/reports/dashboard`, `/api/reports/revenue`, `/api/reports/appointments`, `/api/reports/repair-orders`, `/api/reports/services`, `/api/reports/parts`, `/api/reports/inventory`, `/api/reports/technicians`, `/api/reports/branches`.
  - Metrics: Doanh thu thực tế (`DA_THANH_TOAN`), số tiền đã thanh toán (`ThanhToan.THANH_CONG`), số tiền chưa thanh toán, breakdown lịch hẹn theo trạng thái, breakdown phiếu sửa chữa theo 7 trạng thái chuẩn, top dịch vụ, top phụ tùng, cảnh báo tồn kho sắp hết, hiệu suất kỹ thuật viên, so sánh chi nhánh.
  - Money precision: Sử dụng `BigDecimal` chuẩn xác, không dùng float/double.
  - Access Control: `SYSTEM_ADMIN` toàn quyền xem mọi chi nhánh; `BRANCH_MANAGER` và nhân viên chỉ xem chi nhánh của mình (`403 Forbidden` nếu cố truy cập chi nhánh khác); `CUSTOMER` bị chặn hoàn toàn (`403 Forbidden`).
  - Validation: Kiểm tra khoảng thời gian `from <= to` (`400 Bad Request` nếu `from > to`).
  - DTOs: `DashboardResponse`, `RevenueReportResponse`, `RevenuePeriodResponse`, `AppointmentReportResponse`, `RepairOrderReportResponse`, `ServiceReportResponse`, `PartReportResponse`, `InventoryReportResponse`, `TechnicianReportResponse`, `BranchReportResponse`.
- **Chat / Realtime Conversation Management (TASK 18)**:
  - Base paths: `/api/conversations`, `/api/conversations/{id}`, `/api/conversations/{id}/messages`, `/api/conversations/{id}/read`.
  - Entities: `CuocHoiThoai`, `TinNhan`.
  - Workflow: Tạo cuộc hội thoại giữa Customer và Garage (gắn phiếu tiếp nhận nếu có) -> Gửi tin nhắn -> Lưu tin nhắn vào DB -> Push realtime event qua WebSocket (`/queue/chat`) đến người nhận -> Đánh dấu đã đọc.
  - Membership & Security: Enforce nghiêm ngặt quyền tham gia cuộc hội thoại. Customer chỉ xem/gửi trong hội thoại của mình (`403 Forbidden` đối với khách khác). Staff chỉ truy cập hội thoại thuộc chi nhánh mình hoặc được phân công. Sender luôn được resolve từ JWT SecurityContext (chống fake senderId).
  - Repositories: `CuocHoiThoaiRepository`, `TinNhanRepository`.
  - DTOs: `CreateConversationRequest`, `SendMessageRequest`, `ConversationResponse`, `ChatMessageResponse`.
- **WebSocket Realtime (TASK 17)**:
  - Base endpoint: `/ws` (STOMP over WebSocket & SockJS fallback).
  - Authentication: `WebSocketAuthChannelInterceptor` intercepts STOMP `CONNECT` frame, validates `Authorization: Bearer <JWT>` header, populates authenticated `Principal` in STOMP session. Unauthenticated or invalid JWT -> rejected.
  - Destinations:
    - User Private: `/user/queue/notifications` (Spring User Destination prefix `/user`, simple broker `/queue`).
    - Branch Topic: `/topic/branches/{branchCode}` (Enforces branch authorization check against `BranchAuthorizationService.isAllowedBranchByCode(branchCode)` on `SUBSCRIBE`).
  - Integration: `NotificationService` calls `WebSocketEventPublisher` to push `RealtimeEvent` to the recipient user destination; wrapped in try-catch to guarantee database persistence safety even if client is offline.
  - Classes: `WebSocketConfig`, `WebSocketAuthChannelInterceptor`, `WebSocketEventPublisher`, `RealtimeEvent`.
- **Notification Management (TASK 16)**:
  - Base paths: `/api/notifications`, `/api/notifications/{id}`, `/api/notifications/unread-count`, `/api/notifications/read-all`.
  - Entities: `ThongBao`.
  - Workflow: Tạo thông báo runtime theo các business event (Lịch hẹn, Tiếp nhận, Báo giá, Sửa chữa, Hóa đơn, Thanh toán) -> Người dùng xem danh sách thông báo của chính mình (sắp xếp mới nhất trước) -> Đánh dấu đã đọc (`daDoc = true`) / Đánh dấu tất cả đã đọc -> Đếm số lượng thông báo chưa đọc.
  - Ownership & Security: Mỗi thông báo thuộc về một người dùng cụ thể (`NguoiDung`). Người dùng chỉ được xem/sửa thông báo của chính mình (`403 Forbidden` khi cố truy cập thông báo của người khác).
  - Repositories: `ThongBaoRepository`.
  - DTOs: `NotificationResponse`, `UnreadCountResponse`.
- **Invoice & Payment Management (TASK 15)**:
  - Base paths: `/api/repair-orders/{repairOrderId}/invoice`, `/api/invoices`, `/api/invoices/{invoiceId}/payments`.
  - Entities: `HoaDon`, `HoaDon_DichVu`, `HoaDon_PhuTung`, `ThanhToan`.
  - Workflow: Tạo hóa đơn từ phiếu sửa chữa (`CHUA_THANH_TOAN`) -> Thu ngân thực hiện thanh toán (Full hoặc Partial Payment) -> Hóa đơn chuyển trạng thái sang `DA_THANH_TOAN` khi đã thanh toán đủ 100%.
  - Pricing & Subtotals: Server tự động tập hợp tất cả dịch vụ và phụ tùng từ `PhieuSuaChua_DichVu` và `PhieuSuaChua_PhuTung`, tính `tongTien = SUM(thanhTien dịch vụ) + SUM(thanhTien phụ tùng)`, `thanhTien = tongTien - giamGia + thue`. Client không thể override giá.
  - Payment Rules: Kiểm tra số tiền `soTien > 0`, không cho thanh toán vượt quá số tiền còn lại (`conLai`), chống idempotency lặp lại mã giao dịch `maGiaoDich`.
  - Duplicate Prevention: Mỗi `PhieuSuaChua` chỉ có duy nhất 1 hóa đơn (Unique constraint / DuplicateResourceException 409 Conflict).
  - Status Protection & Immutability: Không thể thanh toán hoặc sửa đổi hóa đơn đã `DA_THANH_TOAN` hoặc `HUY`.
  - Ownership & RBAC: Customer chỉ được xem hóa đơn và thanh toán thuộc xe của mình (`403 Forbidden` đối với xe khác, không được tự tạo hóa đơn). Staff tuân thủ `BranchAuthorizationService`.
  - Repositories: `HoaDonRepository`, `HoaDonDichVuRepository`, `HoaDonPhuTungRepository`, `ThanhToanRepository`.
  - DTOs: `CreateInvoiceRequest`, `InvoiceResponse`, `InvoiceServiceItemResponse`, `InvoicePartItemResponse`, `CreatePaymentRequest`, `PaymentResponse`.
- **Additional Quotation Management (TASK 14)**:
  - Base paths: `/api/repair-orders/{repairOrderId}/quotations`, `/api/quotations/{quotationId}`.
  - Entities: `BaoGiaPhatSinh`, `BaoGiaPhatSinh_DichVu`, `BaoGiaPhatSinh_PhuTung`.
  - Workflow: Tạo báo giá phát sinh khi sửa chữa (`CHO_KHACH_DUYET`) -> Khách hàng xem và duyệt (`DA_DUYET`) hoặc từ chối (`TU_CHOI`) -> Quản lý/nhân viên có thể hủy (`HUY`).
  - Server-side Price Calculation: Đơn giá dịch vụ được resolve từ `GiaDichVuChiNhanh`, đơn giá phụ tùng từ `PhuTung.giaBan`. Backend tự động tính `thanhTien = donGia * soLuong` và `tongTien = SUM(thanhTien)`. Client không thể override giá.
  - Status Protection & Immutability: Không thể duyệt/từ chối/hủy/chỉnh sửa báo giá đã ở trạng thái kết thúc (`DA_DUYET`, `TU_CHOI`, `HUY` -> 400 Bad Request). Không thể tạo báo giá cho phiếu đã `HUY` hoặc `HOAN_TAT`.
  - Ownership & RBAC: Customer chỉ được xem/duyệt/từ chối báo giá thuộc xe của chính mình (`NguoiDung -> KhachHang -> Xe -> PhieuTiepNhan -> PhieuSuaChua -> BaoGiaPhatSinh`). Customer khác truy cập -> 403 Forbidden. Staff tuân thủ `BranchAuthorizationService` và Technician Assignment.
  - Repositories: `BaoGiaPhatSinhRepository`, `BaoGiaPhatSinhDichVuRepository`, `BaoGiaPhatSinhPhuTungRepository`.
  - DTOs: `CreateQuotationRequest`, `QuotationServiceItemRequest`, `QuotationPartItemRequest`, `QuotationResponse`, `QuotationServiceItemResponse`, `QuotationPartItemResponse`.
- **Parts & Inventory Management (TASK 13)**:
  - Base paths: `/api/parts`, `/api/inventory`, `/api/repair-orders/{repairOrderId}/parts`.
  - Parts Catalog: Xem danh mục phụ tùng đang hoạt động (`PhuTung.trangThai = 1`).
  - Branch Inventory: Xem tồn kho phụ tùng theo chi nhánh (`TonKho`), hỗ trợ kiểm tra `BranchAuthorizationService` (SYSTEM_ADMIN: all, BRANCH_MANAGER/RECEPTIONIST/TECHNICIAN: own branch).
  - Add Part to Repair Order: Kiểm tra số lượng tồn kho `TonKho.soLuongTon >= request.soLuong`, kiểm tra trùng lặp (`DuplicateResourceException`), bảo vệ trạng thái kết thúc (`HOAN_TAT`, `HUY` -> 400), tự động trừ tồn kho và ghi nhật ký kiểm toán vào bảng `GiaoDichKho (loaiGiaoDich = 'XUAT_SUA_CHUA')`.
  - Pricing resolution: Server tự resolve đơn giá bán `part.getGiaBan()`, không tin tưởng đơn giá gửi từ client. Tự động tính `thanhTien = donGia * soLuong`.
  - Update Part Quantity: Tính `diff = newQty - oldQty`. Nếu tăng -> trừ thêm tồn kho; nếu giảm -> hoàn trả vào tồn kho; đồng thời ghi nhận `GiaoDichKho` tương ứng.
  - Delete Part: Hoàn trả 100% số lượng phụ tùng về tồn kho chi nhánh, ghi nhật ký `GiaoDichKho (loaiGiaoDich = 'HOAN_TRA')`, sau đó xóa bản ghi khỏi `PhieuSuaChua_PhuTung`.
  - RBAC: `SYSTEM_ADMIN` (all), `BRANCH_MANAGER` / `RECEPTIONIST` (own branch), `TECHNICIAN` (assigned orders only), `CUSTOMER` (403 Forbidden).
  - Repositories: `PhuTungRepository`, `TonKhoRepository`, `PhieuSuaChuaPhuTungRepository`, `GiaoDichKhoRepository`.
  - DTOs: `PartResponse`, `InventoryResponse`, `CreateRepairPartRequest`, `UpdateRepairPartRequest`, `RepairPartResponse`.
  - Relationships: `ChiNhanh (1-N) TonKho (N-1) PhuTung`, `PhieuSuaChua (1-N) PhieuSuaChua_PhuTung (N-1) PhuTung`, `GiaoDichKho (N-1) ChiNhanh, PhuTung, PhieuSuaChua`.
- **Repair Progress / Service Execution (TASK 12)**:
  - Base path: `/api/technician/repair-orders`.
  - `GET /api/technician/repair-orders`: Kỹ thuật viên xem danh sách phiếu sửa chữa được phân công cho mình (thông qua `PhanCong`).
  - `GET /api/technician/repair-orders/{id}` & `/items`: Kỹ thuật viên xem chi tiết và dịch vụ của phiếu sửa chữa mà mình phụ trách.
  - `PATCH /api/technician/repair-orders/{id}/progress`: Ghi nhận tiến độ sửa chữa vào bảng `TienDoSuaChua`, tự động chuyển trạng thái phiếu (`DANG_SUA`, `HOAN_TAT`, `TAM_DUNG`, `CHO_KH_DUYET`) và ghi nhận `thoiGianBatDau` / `thoiGianHoanTat`.
  - `PATCH /api/technician/repair-orders/{id}/items/{itemId}`: Cập nhật trạng thái hạng mục dịch vụ (`CHO_XU_LY`, `DANG_SUA`, `HOAN_TAT`, `HUY`).
  - Cross-technician protection: Kỹ thuật viên T1 không được truy cập/sửa phiếu của T2 dù cùng chi nhánh (403 Forbidden).
  - Status protection: Chặn cập nhật khi phiếu sửa chữa đã ở trạng thái `HOAN_TAT` hoặc `HUY` (400 Bad Request).
  - `TECHNICIAN`: Toàn quyền cập nhật trên các phiếu mình được phân công.
  - `RECEPTIONIST`, `CUSTOMER`: 403 Forbidden.
  - DTOs: `UpdateRepairProgressRequest`, `RepairProgressResponse`, `UpdateServiceItemStatusRequest`.
  - Relationship: `PhieuSuaChua (1-N) TienDoSuaChua (N-1) NhanVien`.
- **Technician Assignment (TASK 11)**:
  - Base path: `/api/repair-orders/{repairOrderId}/assignments`.
  - `POST /api/repair-orders/{repairOrderId}/assignments`: Phân công kỹ thuật viên cho phiếu sửa chữa.
  - Technician validation: Kiểm tra `NhanVien` tồn tại, tài khoản `NguoiDung` active, có vai trò `TECHNICIAN`, và phải cùng thuộc chi nhánh với `PhieuSuaChua` (403 Forbidden nếu khác branch).
  - Status synchronization: Khi phân công, nếu trạng thái phiếu đang là `CHO_XU_LY`, tự động chuyển sang `DA_PHAN_CONG`.
  - Duplicate check: Mỗi kỹ thuật viên chỉ được phân công 1 lần cho cùng 1 phiếu sửa chữa (409 Conflict).
  - Delete assignment: Hủy phân công, nếu không còn phân công nào và trạng thái là `DA_PHAN_CONG` -> tự động chuyển về `CHO_XU_LY`.
  - Status protection: Chặn phân công khi phiếu sửa chữa đã ở trạng thái `HOAN_TAT` hoặc `HUY` (400 Bad Request).
  - `SYSTEM_ADMIN`: Toàn quyền phân công, xem, xóa trên mọi chi nhánh.
  - `BRANCH_MANAGER`: Toàn quyền phân công, xem, xóa thuộc chi nhánh mình.
  - `RECEPTIONIST`, `TECHNICIAN`, `CUSTOMER`: 403 Forbidden.
  - DTOs: `CreateAssignmentRequest`, `AssignmentResponse`.
  - Relationship: `PhieuSuaChua (1-N) PhanCong (N-1) NhanVien`.
- **Service / Repair Items Management (TASK 11)**:
  - Base path: `/api/repair-orders/{repairOrderId}/items`.
  - `POST /api/repair-orders/{repairOrderId}/items`: Thêm dịch vụ vào phiếu sửa chữa.
  - Price resolution: Đơn giá được ưu tiên lấy tự động từ `GiaDichVuChiNhanh` của chi nhánh phiếu sửa chữa, hoặc đơn giá từ request nếu catalog chưa định nghĩa.
  - Calculation: `ThanhTien = DonGia * SoLuong` (được tính toán tự động trên Backend).
  - Duplicate prevention: Mỗi dịch vụ chỉ được xuất hiện tối đa 1 lần trong cùng một phiếu sửa chữa (409 Conflict).
  - Status protection: Chặn thêm/sửa/xóa dịch vụ khi phiếu sửa chữa đã ở trạng thái `HOAN_TAT` hoặc `HUY` (400 Bad Request).
  - `SYSTEM_ADMIN`: Toàn quyền xem, thêm, sửa, xóa trên mọi chi nhánh.
  - `BRANCH_MANAGER`: Toàn quyền xem, thêm, sửa, xóa thuộc chi nhánh mình.
  - `RECEPTIONIST`: Xem và thêm dịch vụ thuộc chi nhánh mình.
  - `TECHNICIAN`, `CUSTOMER`: 403 Forbidden.
  - DTOs: `CreateRepairItemRequest`, `UpdateRepairItemRequest`, `RepairItemResponse`.
  - Relationship: `PhieuSuaChua (1-N) PhieuSuaChua_DichVu (N-1) DichVu`.
- **Repair Order Management (TASK 10)**:
  - Base path: `/api/repair-orders`.
  - `POST /api/repair-orders`: Tạo phiếu sửa chữa từ phiếu tiếp nhận (`PhieuTiepNhan`). Tự động liên kết ChiNhanh từ PhieuTiepNhan.
  - Idempotency & Source validation: Mỗi PhieuTiepNhan chỉ được tạo tối đa 1 PhieuSuaChua (409 Conflict nếu trùng), chặn phiếu tiếp nhận đã hủy (400 Bad Request).
  - `SYSTEM_ADMIN`: Toàn quyền tạo, xem, cập nhật thông tin và trạng thái toàn hệ thống.
  - `BRANCH_MANAGER`: Tạo, xem, cập nhật thông tin và trạng thái thuộc chi nhánh mình (`BranchAuthorizationService.isAllowedBranch`).
  - `RECEPTIONIST`: Tạo và xem phiếu sửa chữa thuộc chi nhánh mình.
  - `TECHNICIAN`, `CUSTOMER`: 403 Forbidden.
  - Status management: `CHO_XU_LY`, `DA_PHAN_CONG`, `DANG_SUA`, `CHO_KH_DUYET`, `TAM_DUNG`, `HOAN_TAT`, `HUY`. Chặn chuyển trạng thái khi đã kết thúc (HOAN_TAT, HUY).
  - DTOs: `CreateRepairOrderRequest`, `UpdateRepairOrderRequest`, `UpdateRepairOrderStatusRequest`, `RepairOrderResponse`.
  - Relationship: `PhieuTiepNhan (1-1) PhieuSuaChua (N-1) ChiNhanh`.
- **Reception / Check-in (TASK 09)**:
  - Base path: `/api/reception`.
  - `POST /api/reception/check-in/{appointmentId}`: Check-in tiếp nhận xe từ lịch hẹn. Tạo `PhieuTiepNhan`, cập nhật `DatLich.trangThai -> DA_TIEP_NHAN`, cập nhật `Xe.soKmHienTai` trong 1 transaction.
  - `SYSTEM_ADMIN`: Toàn quyền check-in, xem phiếu tiếp nhận toàn hệ thống.
  - `RECEPTIONIST`: Check-in và xem phiếu tiếp nhận thuộc chi nhánh mình (`BranchAuthorizationService.isAllowedBranch`). Chặn cross-branch (403 Forbidden).
  - `BRANCH_MANAGER`: Xem danh sách & chi tiết phiếu tiếp nhận thuộc chi nhánh mình.
  - `TECHNICIAN`, `CUSTOMER`: 403 Forbidden.
  - Validations & Idempotency: Không cho tiếp nhận lịch hẹn đã hủy (400), không cho tiếp nhận trùng / duplicate phiếu (409), nhân viên tiếp nhận lấy tự động từ JWT SecurityContext.
  - DTOs: `CheckInRequest`, `ReceptionResponse`.
  - Relationship: `DatLich (1-1) PhieuTiepNhan (N-1) NhanVien, ChiNhanh, Xe`.
- **Appointment Management (TASK 08)**:
  - Base path: `/api/appointments`.
  - `SYSTEM_ADMIN` → danh sách toàn bộ, xem bất kỳ, tạo (có thể chỉ định `maKhachHang`), hủy bất kỳ.
  - `BRANCH_MANAGER`, `RECEPTIONIST` → danh sách thuộc chi nhánh mình (`BranchAuthorizationService.resolveUserBranchId`), xem/hủy lịch hẹn thuộc chi nhánh mình (403 nếu cross-branch).
  - `CUSTOMER` → danh sách lịch hẹn của mình, xem/hủy lịch hẹn của mình (403 nếu truy cập của khách khác). Tạo lịch hẹn: owner tự động trích xuất từ JWT, bắt buộc `Xe` thuộc quyền sở hữu của mình (403 nếu đặt xe người khác).
  - `TECHNICIAN` → 403 Forbidden.
  - Validation: thời gian hẹn không ở quá khứ (400), chi nhánh phải hoạt động (400), chống trùng lịch hẹn cho cùng xe tại cùng thời điểm (409).
  - Cancel status: chỉ cho phép hủy khi trạng thái là `CHO_XAC_NHAN` hoặc `DA_XAC_NHAN`. Trạng thái khác hoặc đã hủy trước đó trả về 400 Bad Request.
  - DTOs: `AppointmentResponse`, `CreateAppointmentRequest`.
  - Relationship: `NguoiDung (1-1) KhachHang (1-N) Xe (1-N) DatLich (N-1) ChiNhanh`.
- **Vehicle Management (TASK 07)**:
  - Base path: `/api/vehicles`.
  - `SYSTEM_ADMIN` → danh sách toàn bộ, CRUD bất kỳ xe. Khi tạo xe phải cấp `maKhachHang`.
  - `CUSTOMER` → chỉ thấy/sửa/xóa xe của chính mình. Owner tự động từ JWT — client không được gửi `customerId` để xác định owner.
  - Cross-customer: CUSTOMER A truy cập xe CUSTOMER B → 403 Forbidden (query thẳng theo owner — không filter Java).
  - DELETE: nếu xe đã có `DatLich`/`PhieuTiepNhan` → database constraint sẽ chặn → trả 400 Bad Request (không cascade delete).
  - DTOs: `VehicleResponse`, `CreateVehicleRequest`, `UpdateVehicleRequest`.
  - Relationship: `NguoiDung (1-1) KhachHang (1-N) Xe`.
- **Customer Management (TASK 06C)**:
  - Base path: `/api/customers`.
  - Self-service: `/api/customers/me` (CUSTOMER & SYSTEM_ADMIN).
  - Admin management: `/api/customers` (SYSTEM_ADMIN only).
  - Customer Ownership: CUSTOMER chỉ được xem/sửa profile của chính mình qua `JWT → NguoiDung → KhachHang`. Cố ý truy cập profile khách hàng khác bị trả về 403 Forbidden.
  - DTOs: `CustomerResponse`, `CreateCustomerRequest`, `UpdateCustomerRequest`, `UpdateCustomerStatusRequest`.
  - Relationship: `NguoiDung (1-1) KhachHang`.
- **Test suite**: `mvn test` → **76/76 tests PASS**.

## 3. Repositories
| Repository | Entity | Key Queries |
|---|---|---|
| `NguoiDungRepository` | `NguoiDung` | `findByTenDangNhap`, `findByEmail`, `findByTenDangNhapOrEmail`, `existsByTenDangNhap`, `existsByEmail` |
| `NguoiDungVaiTroRepository` | `NguoiDungVaiTro` | `findByNguoiDungMaNguoiDung`, `deleteByNguoiDungMaNguoiDung`, `countActiveUsersByRoleName` |
| `VaiTroRepository` | `VaiTro` | `findByTenVaiTro` |
| `ChiNhanhRepository` | `ChiNhanh` | `findByMaChiNhanhCode` |
| `NhanVienRepository` | `NhanVien` | `findByNguoiDungMaNguoiDung`, `findByMaNhanVienCode`, `findByChiNhanhMaChiNhanh`, `existsByMaNhanVienCode`, `existsByNguoiDungMaNguoiDung` |
| `KhachHangRepository` | `KhachHang` | `findByNguoiDungMaNguoiDung`, `findByMaKhachHangCode`, `existsByMaKhachHangCode`, `existsByNguoiDungMaNguoiDung` |
| `XeRepository` | `Xe` | `findByKhachHangMaKhachHang`, `findByMaXeAndKhachHangMaKhachHang`, `existsByBienSo`, `existsBySoVIN` |
| `DatLichRepository` | `DatLich` | `findByKhachHangMaKhachHang`, `findByChiNhanhMaChiNhanh`, `findByMaDatLichAndKhachHangMaKhachHang`, `existsByXeMaXeAndThoiGianHenAndTrangThaiNotIn` |
| `PhieuTiepNhanRepository` | `PhieuTiepNhan` | `findByChiNhanhMaChiNhanh`, `findByXeMaXe`, `existsByDatLichMaDatLich`, `findByDatLichMaDatLich` |
| `PhieuSuaChuaRepository` | `PhieuSuaChua` | `findByChiNhanhMaChiNhanh`, `findByPhieuTiepNhanMaTiepNhan`, `existsByPhieuTiepNhanMaTiepNhan` |
| `PhieuSuaChuaDichVuRepository` | `PhieuSuaChuaDichVu` | `findByPhieuSuaChuaMaPhieuSuaChua`, `existsByPhieuSuaChuaMaPhieuSuaChuaAndDichVuMaDichVu`, `findByMaChiTietAndPhieuSuaChuaMaPhieuSuaChua` |
| `PhanCongRepository` | `PhanCong` | `findByPhieuSuaChuaMaPhieuSuaChua`, `existsByPhieuSuaChuaMaPhieuSuaChuaAndNhanVienMaNhanVien`, `findByMaPhanCongAndPhieuSuaChuaMaPhieuSuaChua`, `countByPhieuSuaChuaMaPhieuSuaChua`, `findByNhanVienMaNhanVien` |
| `TienDoSuaChuaRepository` | `TienDoSuaChua` | `findByPhieuSuaChuaMaPhieuSuaChuaOrderByThoiGianDesc` |
| `DichVuRepository` | `DichVu` | `findByTrangThaiTrue` |
| `GiaDichVuChiNhanhRepository` | `GiaDichVuChiNhanh` | `findByChiNhanhMaChiNhanhAndDichVuMaDichVuAndTrangThaiTrue` |

## 4. Database
- Schema: `database/GarageSystemDB.sql` (32+ tables — single source of truth. **KHÔNG sửa**).
- Seed: `database/seed/V01__development_seed.sql`.
  - 2 branches: `CN001`, `CN002`.
  - 5 roles: `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN`, `CUSTOMER`.
  - 7 dev accounts (BCrypt `Password123@`): `admin`, `manager` (CN001), `receptionist` (CN001), `technician` (CN001), `manager2` (CN002), `customer` (KH001), `customer2` (KH002).
  - 2 seed vehicles: `51A-11111` (KH001/customer), `51B-22222` (KH002/customer2).
  - 2 seed appointments: Appointment 1 (`KH001` - `51A-11111` - `CN001`), Appointment 2 (`KH002` - `51B-22222` - `CN002`).

## 5. Nhật ký tiến độ
- **TASK 01** (COMPLETED): Project Foundation.
- **TASK 02** (COMPLETED): Database Mapping — 35 JPA Entities.
- **TASK 03** (COMPLETED): Authentication / JWT.
- **TASK 03.1** (COMPLETED): Development Seed Data & Auth Validation.
- **TASK 04** (COMPLETED): Authorization / RBAC.
- **TASK 05** (COMPLETED): Branch Authorization.
- **TASK 06A** (COMPLETED): User Management (`NguoiDung`).
- **TASK 06B** (COMPLETED): Employee Management (`NhanVien`).
- **TASK 06C** (COMPLETED): Customer Management (`KhachHang`) — 76/76 tests PASS.
- **TASK 07** (COMPLETED): Vehicle Management (`Xe`) — 101/101 tests PASS.
- **TASK 08** (COMPLETED): Appointment Management (`DatLich`) — 132/132 tests PASS.
- **TASK 09** (COMPLETED): Reception / Check-in (`PhieuTiepNhan`) — 151/151 tests PASS.
- **TASK 10** (COMPLETED): Repair Order Management (`PhieuSuaChua`) — 174/174 tests PASS.
- **TASK 11** (COMPLETED): Technician Assignment (`PhanCong`) — 215/215 tests PASS.
- **EXTRA TASK** (COMPLETED): Service / Repair Items Management (`PhieuSuaChua_DichVu`) — 195/195 tests PASS.
- **TASK 12** (COMPLETED): Repair Progress / Service Execution (`TienDoSuaChua`) — 234/234 tests PASS.
- **TASK 13** (COMPLETED): Parts / Inventory Management (`PhuTung`, `TonKho`, `PhieuSuaChua_PhuTung`, `GiaoDichKho`) — 263/263 tests PASS.
- **TASK 14** (COMPLETED): Additional Quotation (`BaoGiaPhatSinh`, `BaoGiaPhatSinh_DichVu`, `BaoGiaPhatSinh_PhuTung`) — 280/280 tests PASS.
- **TASK 15** (COMPLETED): Invoice / Payment (`HoaDon`, `HoaDon_DichVu`, `HoaDon_PhuTung`, `ThanhToan`) — 298/298 tests PASS.
- **TASK 16** (COMPLETED): Notification Management (`ThongBao`) — 313/313 tests PASS.
- **TASK 17** (COMPLETED): WebSocket Realtime (`WebSocketConfig`, `WebSocketAuthChannelInterceptor`, `WebSocketEventPublisher`) — 326/326 tests PASS.
- **TASK 18** (COMPLETED): Chat / Realtime Conversation (`CuocHoiThoai`, `TinNhan`) — 341/341 tests PASS.
- **TASK 19** (COMPLETED): Reports & Statistics (`ReportService`, `ReportController`) — 357/357 tests PASS.
- **FRONTEND TASK 01** (COMPLETED): Frontend Foundation (React 18, TypeScript, Vite, Router, JWT Auth, API Client, Layout Foundation) — PASS.
- **FRONTEND TASK 02** (COMPLETED): Authentication UI & User Experience (AutoCare Theme, Login UX, Show/Hide Password, User Dropdown, Role Navigation, Dashboard KPIs) — PASS.
- **FRONTEND TASK 03** (COMPLETED): Dashboard & Real Data Integration (Role-Specific Dashboards for Admin, Manager, Receptionist, Technician, Customer; Backend `/api/reports/*`, `/api/vehicles`, `/api/appointments`) — PASS.
- **FRONTEND TASK 05** (COMPLETED): User / Employee / Customer Management (Full CRUD interfaces, dynamic filters, detail & form modals, status toggles, branch constraints, RoleGuard protection for `/app/employees`, `/app/customers`, `/app/users`) — PASS.
- **FRONTEND TASK 06** (COMPLETED): Vehicle Management (Full CRUD interfaces, customer ownership isolation, license plate & VIN validation, detail & form & delete modals, RoleGuard protection for `/app/vehicles` for `ROLE_ADMIN` & `ROLE_CUSTOMER`) — PASS.
- **INFRASTRUCTURE TASK 01** (COMPLETED): Docker Development Environment (Docker Compose orchestration, multi-stage Spring Boot Dockerfile, React/Vite dev Dockerfile, MS SQL Server 2022 container with automatic DB schema + seed initialization via entrypoint script, named volume persistence `garage-sqlserver-data`, bridge network `garage-network`) — PASS.

## 6. Frontend Status & Architecture
- **Framework & Build**: React 18, TypeScript, Vite 5 (`frontend/`).
- **Ports & Endpoints**:
  - Frontend Port: `3001` (`http://localhost:3001/`)
  - Backend Port: `8080`
  - REST API Base URL: `http://localhost:8080/api`
  - WebSocket URL: `http://localhost:8080/ws`
- **Design System**: AutoCare Multi-Branch (`AUTO-GARAGE-UI-SUGGESTION/autocare_multi_branch/DESIGN.md`), Inter font, Material Symbols Outlined, Deep Blue (`#00236f`/`#1e3a8a`), Industrial Slate (`#334155`), Action Orange (`#fd761a`), clean surface backgrounds.
- **Authentication & Security**:
  - `ITokenStorage` (default: `localStorage`) + safe JWT claims parser.
  - `AuthContext` + `useAuth()` hook managing login, logout, user profile hydration via `GET /api/auth/me`.
  - `ProtectedRoute` enforcing authentication redirect to `/login`.
  - `RoleGuard` enforcing role-based permissions (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_FRONT_DESK`, `ROLE_TECHNICIAN`, `ROLE_CUSTOMER`) redirecting to `/unauthorized`.
- **Management Modules**:
  - **Vehicle Management (FE06)** (`/app/vehicles`): Connected to `/api/vehicles`. Role-aware CRUD (Admin: global system vehicles + assign customer; Customer: own vehicles only). Search, brand and status filters, Detail modal, Create/Edit modals with plate/VIN validation, Delete modal, KPI cards.
  - **Employee Management** (`/app/employees`): Connected to `/api/employees`. Search, branch & role filters, Create/Edit modals with branch rules (Branch Managers cannot transfer branches), Status modal, KPI metrics.
  - **Customer Management** (`/app/customers`): Connected to `/api/customers` and `/api/vehicles`. Search, status filters, Create/Edit modals, Customer vehicles list, Status modal.
  - **User & Permissions Management** (`/app/users`): Connected to `/api/admin/users`. Multi-role selection, username & BCrypt password creation, SYSTEM_ADMIN safety constraints, status toggles.
- **Dashboards & Real Data**:
  - `reportService` & `customerDashboardService` connected to live Spring Boot APIs.
  - Role-specific dashboard views (`AdminDashboard`, `ManagerDashboard`, `ReceptionistDashboard`, `TechnicianDashboard`, `CustomerDashboard`).
  - Skeleton loading state (`DashboardSkeleton`) & graceful error retry without layout jumping.
- **Navigation & Layout**:
  - Fixed 240px Industrial Slate Sidebar with role-based navigation filtering.
  - Top Navigation Header with system title, search bar, notification bell with unread dot, and user profile dropdown with logout.
  - Responsive layout with mobile drawer toggle and overlay backdrop.

## 7. Docker Infrastructure & LAN Networking Status
- **Docker Compose**: `docker-compose.yml` defining `garage-frontend` (:3001), `garage-backend` (:8080), `garage-sqlserver` (:1433), connected via `garage-network`.
- **Database Service**: `mcr.microsoft.com/mssql/server:2022-latest` with `entrypoint.sh` executing `GarageSystemDB.sql` + `V01__development_seed.sql` + `V02__restore_original_roles.sql` only on first run if DB does not exist. Client devices in LAN do not require local SQL Server.
- **Backend Service**: Multi-stage `maven:3.9-eclipse-temurin-17` builder and `eclipse-temurin:17-jre-jammy` runner. Configurable `CORS_ALLOWED_ORIGIN_PATTERNS` supporting `*` and LAN host origins.
- **Frontend Service**: `node:20-alpine` running Vite dev server bound to `0.0.0.0:3001` with dynamic hostname resolution in `env.ts` (`http://<HOST_IP>:8080/api` and `ws://<HOST_IP>:8080/ws`) for cross-device LAN development.
- **Secrets & Configuration**: `.env.example` at root, `.gitignore` protecting `.env`.
- **Persistence**: Named volume `garage-sqlserver-data` keeps database changes across container restarts.

## 8. Nguyên tắc cốt lõi
- Không triển khai business feature chưa được yêu cầu.
- Không sửa schema SQL Server nếu chưa được phê duyệt.
- Backend là authority cuối cùng — không tin role/branch/ownership từ client.
- Cập nhật `README.md` và `docs/PROJECT_CONTEXT.md` sau mỗi task.


