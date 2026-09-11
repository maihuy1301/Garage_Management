# Garage Management System (Hệ Thống Quản Lý Garage Ô Tô Đa Chi Nhánh)

## 1. Project Overview
Hệ thống phần mềm quản lý việc đặt lịch và dịch vụ sửa chữa xe ô tô dành cho Garage đa chi nhánh.
- **Web App (React + Vite)**: Dành cho `ADMIN`, `MANAGER`, `RECEPTIONIST`.
- **Mobile App (Flutter + Dart)**: Dành cho `TECHNICIAN`, `CUSTOMER`.
- **Backend Modules Completed**: Auth, RBAC, Branch Authorization, User Management, Employee Management, Customer Management, Vehicle Management, Appointment Management, Reception / Check-in, Repair Order Management, Service / Repair Items Management, Technician Assignment, Repair Progress / Service Execution, Parts / Inventory Management, Additional Quotation, Invoice / Payment, Notification, WebSocket Realtime, Chat, Reports & Statistics.

## 2. Tech Stack
- **Backend**: Spring Boot 3.2.5, Java 17, Maven, Spring Web, Spring Data JPA, Spring Security, Spring Validation, JJWT 0.12.5, SQL Server Driver.
- **Frontend**: React 18, Vite 5, TypeScript, React Router 6, Axios, Modern Dark/Light Design System.
- **Mobile**: Flutter, Dart.
- **Database**: Microsoft SQL Server.

## 3. Project Structure
```text
AUTO_GARAGE/
├── backend/src/main/java/com/garage/
│   ├── controller/       # AuthController, RbacTestController, BranchTestController, BranchController,
│   │                     # AdminUserController (TASK 06A), EmployeeController (TASK 06B),
│   │                     # CustomerController (TASK 06C), VehicleController (TASK 07),
│   │                     # AppointmentController (TASK 08), ReceptionController (TASK 09),
│   │                     # RepairOrderController (TASK 10), RepairItemController (TASK 11),
│   │                     # TechnicianAssignmentController (TASK 12), TechnicianExecutionController (TASK 12 Execution),
│   │                     # PartController (TASK 13), InventoryController (TASK 13), RepairPartController (TASK 13),
│   │                     # QuotationController (TASK 14), InvoiceController (TASK 15), NotificationController (TASK 16),
│   │                     # ChatController (TASK 18), ReportController (TASK 19)
│   ├── dto/              # ApiResponse, LoginRequest, LoginResponse, BranchResponse, User DTOs, Employee DTOs, Customer DTOs, Vehicle DTOs, Appointment DTOs, Reception DTOs, RepairOrder DTOs, RepairItem DTOs, Assignment DTOs, RepairProgress DTOs, Part DTOs, Inventory DTOs, RepairPart DTOs, Quotation DTOs, Invoice DTOs, Payment DTOs, Notification DTOs, Chat DTOs, Report DTOs (TASK 19)
│   ├── entity/           # 35 JPA Entities
│   ├── exception/        # GlobalExceptionHandler, ResourceNotFoundException,
│   │                     # DuplicateResourceException, BadRequestException
│   ├── repository/       # NguoiDungRepository, NguoiDungVaiTroRepository,
│   │                     # NhanVienRepository, ChiNhanhRepository, VaiTroRepository,
│   │                     # KhachHangRepository (TASK 06C), XeRepository (TASK 07),
│   │                     # DatLichRepository (TASK 08), PhieuTiepNhanRepository (TASK 09),
│   │                     # PhieuSuaChuaRepository (TASK 10), PhieuSuaChuaDichVuRepository (TASK 11),
│   │                     # DichVuRepository (TASK 11), GiaDichVuChiNhanhRepository (TASK 11),
│   │                     # PhanCongRepository (TASK 12), TienDoSuaChuaRepository (TASK 12 Execution),
│   │                     # PhuTungRepository (TASK 13), TonKhoRepository (TASK 13), PhieuSuaChuaPhuTungRepository (TASK 13), GiaoDichKhoRepository (TASK 13),
│   │                     # BaoGiaPhatSinhRepository (TASK 14), BaoGiaPhatSinhDichVuRepository (TASK 14), BaoGiaPhatSinhPhuTungRepository (TASK 14),
│   │                     # HoaDonRepository (TASK 15), HoaDonDichVuRepository (TASK 15), HoaDonPhuTungRepository (TASK 15), ThanhToanRepository (TASK 15),
│   │                     # ThongBaoRepository (TASK 16), CuocHoiThoaiRepository (TASK 18), TinNhanRepository (TASK 18)
│   ├── security/         # JwtService, JwtAuthenticationFilter,
│   │                     # JwtAuthenticationEntryPoint, JwtAccessDeniedHandler,
│   │                     # CustomUserDetails, CustomUserDetailsService, SecurityConfig,
│   │                     # BranchAuthorizationService
│   ├── service/          # AuthService, UserService, EmployeeService, CustomerService (TASK 06C), VehicleService (TASK 07), AppointmentService (TASK 08), ReceptionService (TASK 09), RepairOrderService (TASK 10), RepairItemService (TASK 11), TechnicianAssignmentService (TASK 12), TechnicianExecutionService (TASK 12 Execution), InventoryService (TASK 13), RepairPartService (TASK 13), QuotationService (TASK 14), InvoiceService (TASK 15), PaymentService (TASK 15), NotificationService (TASK 16), ChatService (TASK 18), ReportService (TASK 19)
│   └── websocket/        # WebSocketConfig (TASK 17), WebSocketAuthChannelInterceptor (TASK 17), WebSocketEventPublisher (TASK 17)
├── frontend/             # React + Vite foundation
├── mobile/               # Flutter customer/technician app foundation
├── database/
│   ├── GarageManagementSystem.sql       # SQL Server DDL schema (source of truth)
│   └── seed/
│       └── V01__development_seed.sql    # 5 roles, 8 dev accounts, 2 branches (BCrypt)
└── docs/PROJECT_CONTEXT.md
```

## 4. Environment Verified
| Tool | Version |
|---|---|
| OS | Windows 11 |
| Java | 17.0.12 |
| Maven | 3.9.16 |
| Node.js | v24.15.0 (npm 11.12.1) |
| Flutter/Dart | Installed |
| Git | 2.51.0 |

## 4.1 Running the Project
- **Backend (Spring Boot)**:
  - Port: `8080`
  - Base API URL: `http://localhost:8080/api`
  - Realtime WebSocket: `http://localhost:8080/ws`
  - Command: `mvn spring-boot:run` (in `backend/`) or `java -jar target/garage-backend-0.0.1-SNAPSHOT.jar`
- **Frontend (React + Vite + TypeScript)**:
  - Port: `3001`
  - Web URL: `http://localhost:3001/`
  - Command: `npm run dev` (in `frontend/`)
  - Production Build: `npm run build`
- **Mobile (Flutter)**:
  - Khi mở app, splash screen AutoCare dùng ảnh `mobile/asset/screen.png` được hiển thị trong lúc khôi phục session, tối thiểu 1,6 giây.
  - Android emulator: `flutter run` (in `mobile/`, mặc định gọi `http://10.0.2.2:8080/api`).
  - Nếu AVD Android 17/API 37 16 KB chỉ hiện màn hình đen dù app vẫn chạy, Android debug manifest đã tắt Impeller để dùng Skia fallback. Dừng phiên cũ rồi chạy lại `flutter run`; nếu vẫn đen, đổi `Emulated Performance > Graphics` sang `Software`, cold boot AVD và thử lại.
  - Thiết bị thật/iOS/Desktop: `flutter run --dart-define=API_BASE_URL=http://<backend-host>:8080/api`.
  - Thiết bị Android thật qua USB: chạy `adb reverse tcp:8080 tcp:8080`, sau đó `flutter run -d <device-id> --dart-define=API_BASE_URL=http://127.0.0.1:8080/api`.
  - Customer/guest foundation: guest home, đăng ký khách hàng công khai, JWT login, secure token storage, role routing và protected navigation với `returnTo`; CUSTOMER có màn `/vehicles` để xem/thêm xe và chuyển thẳng sang `/appointments?vehicleId=...` để đặt lịch với xe đã chọn, xem lịch của mình, hủy lịch khi backend cho phép. Form thêm xe tải hãng qua `GET /api/brands`, tải model phụ thuộc qua `GET /api/brands/{brandId}/models` và gửi `maHangXe`/`maModel` theo schema mới; không gửi owner từ mobile. Tab `/tracking` tải lịch hẹn thuộc customer và mở `/tracking/{appointmentId}` qua `GET /api/appointments/{id}` để hiển thị chi tiết cùng tiến trình `CHO_XAC_NHAN -> DA_XAC_NHAN -> DA_TIEP_NHAN -> HOAN_TAT`; hỗ trợ pull-to-refresh, loading/error/empty state và không cho mobile tự đổi trạng thái. Các màn dùng header/banner AutoCare theo tham khảo Stitch; thanh điều hướng vẫn đồng bộ 5 mục hiện hành.

## 4.2 Infrastructure & Development Workflow
Chi tiết đầy đủ xem tại [docs/DOCKER.md](docs/DOCKER.md).

```bash
# 1. Khởi động SQL Server 2022 qua Docker Compose (Terminal 1)
docker compose up -d --build

# 2. Khởi động Backend Spring Boot (Terminal 2)
cd backend
mvn spring-boot:run

# 3. Khởi động Frontend React / Vite (Terminal 3)
cd frontend
npm run dev
```

### Services & Ports
| Thành phần | Môi trường | Port | Công nghệ / Ghi chú |
|---|---|---|---|
| `garage-sqlserver` | Docker Compose | `1433` | MS SQL Server 2022 (Persistent Volume `garage-sqlserver-data`) |
| Backend | Native Host | `8080` | Java 17 / Spring Boot 3.2.5 (`http://localhost:8080/api`) |
| Frontend | Native Host | `3001` | React 18 / Vite 5 (`http://localhost:3001/`) |
| Mobile App | Flutter CLI | — | Flutter / Android / iOS |

## 5. Development Test Accounts (Password: `Password123@`)
| Username | Role | Branch | Records |
|---|---|---|---|
| `admin` | `ROLE_ADMIN` | Global | — |
| `manager` | `ROLE_MANAGER` | Garage Central Chi Nhánh 1 | `NhanVien` |
| `receptionist` | `ROLE_FRONT_DESK` | Garage Central Chi Nhánh 1 | `NhanVien` |
| `technician` | `ROLE_TECHNICIAN` | Garage Central Chi Nhánh 1 | `NhanVien` |
| `manager2` | `ROLE_MANAGER` | Garage Chi Nhánh 2 Bình Thạnh | `NhanVien` |
| `technician2` | `ROLE_TECHNICIAN` | Garage Chi Nhánh 2 Bình Thạnh | `NhanVien` |
| `customer` | `ROLE_CUSTOMER` | — | `KhachHang`, `Xe` (51A-11111) |
| `customer2` | `ROLE_CUSTOMER` | — | `KhachHang`, `Xe` (51B-22222) |

## 6. Seed Vehicles (TASK 07)
| Biển số | Owner | Hãng xe | Model | Năm |
|---|---|---|---|---|
| `51A-11111` | `customer` | Toyota | Camry | 2020 |
| `51B-22222` | `customer2` | Honda | Civic | 2021 |

## 7. Endpoints
### Authentication (`AuthController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `POST` | `/api/auth/register` | Đăng ký khách hàng bằng số điện thoại; tạo transaction `NguoiDung` + `ROLE_CUSTOMER` + `KhachHang`, BCrypt hash mật khẩu; không dùng `MaKhachHangCode` trong schema mới | Public |
| `POST` | `/api/auth/login` | Đăng nhập bằng số điện thoại/tên đăng nhập hoặc email và mật khẩu; trả thêm `hasPin` để mobile biết có cần màn nhập PIN không | Public |
| `GET` | `/api/auth/me` | Lấy thông tin người dùng hiện tại, gồm trạng thái `hasPin` | Authenticated |

### Branch Master Data (`BranchController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/branches` | Lấy danh sách chi nhánh (Master Data) | Authenticated (`SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN`, `CUSTOMER`) |
| `GET` | `/api/branches/{id}` | Lấy thông tin chi tiết một chi nhánh theo ID | Authenticated |

### Report & Statistics (`ReportController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/reports/dashboard` | Thống kê dashboard tổng quan (doanh thu, đơn, lịch, khách, xe) | Staff / Admin |
| `GET` | `/api/reports/revenue` | Thống kê doanh thu, đã thanh toán, chưa thanh toán, theo ngày | Staff / Admin |
| `GET` | `/api/reports/appointments` | Thống kê lịch hẹn theo trạng thái | Staff / Admin |
| `GET` | `/api/reports/repair-orders` | Thống kê phiếu sửa chữa theo trạng thái | Staff / Admin |
| `GET` | `/api/reports/services` | Top dịch vụ sử dụng nhiều nhất & doanh thu dịch vụ | Staff / Admin |
| `GET` | `/api/reports/parts` | Top phụ tùng sử dụng nhiều nhất & doanh thu phụ tùng | Staff / Admin |
| `GET` | `/api/reports/inventory` | Báo cáo tồn kho & cảnh báo phụ tùng sắp hết hàng | Staff / Admin |
| `GET` | `/api/reports/technicians` | Thống kê hiệu suất kỹ thuật viên (phân công, hoàn tất, đang sửa) | Staff / Admin |
| `GET` | `/api/reports/branches` | Báo cáo so sánh các chi nhánh (doanh thu, lịch hẹn, sửa chữa) | Staff / Admin |

### Chat & Realtime Messaging (`ChatController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/conversations` | Lấy danh sách cuộc hội thoại của user hiện tại | Authenticated (`CUSTOMER`, `STAFF`, `ADMIN`) |
| `POST` | `/api/conversations` | Tạo cuộc hội thoại mới (kèm tin nhắn đầu tiên nếu có) | Authenticated |
| `GET` | `/api/conversations/{conversationId}` | Xem chi tiết cuộc hội thoại (kèm kiểm tra membership) | Conversation member |
| `GET` | `/api/conversations/{conversationId}/messages` | Lấy lịch sử tin nhắn của cuộc hội thoại | Conversation member |
| `POST` | `/api/conversations/{conversationId}/messages` | Gửi tin nhắn mới (kèm push realtime qua WebSocket) | Conversation member |
| `PATCH` | `/api/conversations/{conversationId}/read` | Đánh dấu các tin nhắn trong hội thoại là đã đọc | Conversation member |

### WebSocket Realtime (`/ws`)
| Protocol | Destination / Channel | Description | Permission |
|---|---|---|---|
| `STOMP` | `/ws` | WebSocket connection handshake (SockJS + native WS) | `Authorization: Bearer <JWT>` header required |
| `STOMP` | `/user/queue/notifications` | User private notification destination | Authenticated user (own queue) |
| `STOMP` | `/topic/branches/{branchCode}` | Branch-specific event broadcast topic | `SYSTEM_ADMIN` or Staff belonging to that branch |

### Notification Endpoints (`NotificationController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/notifications` | Lấy danh sách thông báo của người dùng hiện tại (mới nhất trước) | Authenticated user (`ALL ROLES`) |
| `GET` | `/api/notifications/{id}` | Xem chi tiết thông báo (kèm kiểm tra ownership) | Authenticated user (own notification) |
| `PATCH` | `/api/notifications/{id}/read` | Đánh dấu thông báo là đã đọc (idempotent) | Authenticated user (own notification) |
| `PATCH` | `/api/notifications/read-all` | Đánh dấu tất cả thông báo của mình là đã đọc | Authenticated user (`ALL ROLES`) |
| `GET` | `/api/notifications/unread-count` | Đếm số lượng thông báo chưa đọc của mình | Authenticated user (`ALL ROLES`) |

### Invoice & Payment Endpoints (`InvoiceController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/repair-orders/{repairOrderId}/invoice` | Xem hóa đơn của phiếu sửa chữa | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN`, `CUSTOMER` (own order) |
| `POST` | `/api/repair-orders/{repairOrderId}/invoice` | Tạo hóa đơn từ phiếu sửa chữa (tự động tính tổng & items) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST` |
| `GET` | `/api/invoices/{invoiceId}` | Xem chi tiết hóa đơn (kèm dịch vụ, phụ tùng, thanh toán) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN`, `CUSTOMER` (own order) |
| `GET` | `/api/invoices` | Xem danh sách hóa đơn theo chi nhánh | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `CUSTOMER` |
| `POST` | `/api/invoices/{invoiceId}/payments` | Thực hiện thanh toán hóa đơn (Full / Partial payment) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST` |
| `GET` | `/api/invoices/{invoiceId}/payments` | Xem lịch sử thanh toán của hóa đơn | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN`, `CUSTOMER` (own order) |

### Additional Quotation Endpoints (`QuotationController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/repair-orders/{repairOrderId}/quotations` | Xem danh sách báo giá phát sinh của phiếu sửa chữa | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN`, `CUSTOMER` (own order) |
| `POST` | `/api/repair-orders/{repairOrderId}/quotations` | Tạo báo giá phát sinh mới (tự động tính giá và tổng tiền) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` (assigned) |
| `GET` | `/api/quotations/{quotationId}` | Xem chi tiết báo giá phát sinh (kèm items dịch vụ & phụ tùng) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN`, `CUSTOMER` (own order) |
| `PATCH` | `/api/quotations/{quotationId}/approve` | Khách hàng duyệt báo giá phát sinh (`DA_DUYET`) | `CUSTOMER` (own quotation), `SYSTEM_ADMIN`, `BRANCH_MANAGER` |
| `PATCH` | `/api/quotations/{quotationId}/reject` | Khách hàng từ chối báo giá phát sinh (`TU_CHOI`) | `CUSTOMER` (own quotation), `SYSTEM_ADMIN`, `BRANCH_MANAGER` |
| `PATCH` | `/api/quotations/{quotationId}/cancel` | Quản lý/nhân viên hủy báo giá phát sinh (`HUY`) | `SYSTEM_ADMIN`, `BRANCH_MANAGER` |

### Parts & Inventory Endpoints (`PartController`, `InventoryController`, `RepairPartController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/parts` | Lấy danh mục phụ tùng đang hoạt động | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` |
| `GET` | `/api/parts/{id}` | Lấy chi tiết phụ tùng theo ID | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` |
| `GET` | `/api/inventory` | Xem danh sách tồn kho theo chi nhánh (kèm branch auth) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` |
| `GET` | `/api/inventory/{partId}` | Xem chi tiết tồn kho phụ tùng tại chi nhánh | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` |
| `GET` | `/api/repair-orders/{repairOrderId}/parts` | Xem danh sách phụ tùng trong phiếu sửa chữa | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` |
| `POST` | `/api/repair-orders/{repairOrderId}/parts` | Thêm phụ tùng vào phiếu sửa chữa (tự động trừ tồn kho & audit `GiaoDichKho`) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` (assigned) |
| `PUT` | `/api/repair-orders/{repairOrderId}/parts/{partDetailId}` | Cập nhật số lượng phụ tùng (điều chỉnh tồn kho tương ứng) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` (assigned) |
| `DELETE` | `/api/repair-orders/{repairOrderId}/parts/{partDetailId}` | Xóa phụ tùng khỏi phiếu (hoàn trả 100% tồn kho & audit `GiaoDichKho`) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `TECHNICIAN` (assigned) |

### Technician Execution & Progress Endpoints (`TechnicianExecutionController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/technician/repair-orders` | Xem danh sách phiếu sửa chữa được phân công cho kỹ thuật viên | `TECHNICIAN` (assigned only) |
| `GET` | `/api/technician/repair-orders/{repairOrderId}` | Xem chi tiết phiếu sửa chữa được phân công | `TECHNICIAN` (assigned only) |
| `GET` | `/api/technician/repair-orders/{repairOrderId}/items` | Xem danh sách dịch vụ thuộc phiếu sửa chữa của mình | `TECHNICIAN` (assigned only) |
| `PATCH` | `/api/technician/repair-orders/{repairOrderId}/progress` | Cập nhật tiến độ sửa chữa (`TienDoSuaChua`, chuyển status sang `DANG_SUA`/`HOAN_TAT`) | `TECHNICIAN` (assigned only) |
| `PATCH` | `/api/technician/repair-orders/{repairOrderId}/items/{itemId}` | Cập nhật trạng thái hạng mục dịch vụ (`CHO_XU_LY`, `DANG_SUA`, `HOAN_TAT`, `HUY`) | `TECHNICIAN` (assigned only) |
| `GET` | `/api/technician/repair-orders/{repairOrderId}/progress-history` | Xem lịch sử các lần cập nhật tiến độ sửa chữa | `TECHNICIAN` (assigned only) |

### Technician Assignment Endpoints (`TechnicianAssignmentController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/repair-orders/{repairOrderId}/assignments` | Xem danh sách kỹ thuật viên được phân công cho phiếu sửa chữa | `SYSTEM_ADMIN`, `BRANCH_MANAGER` |
| `POST` | `/api/repair-orders/{repairOrderId}/assignments` | Phân công kỹ thuật viên (kiểm tra vai trò TECHNICIAN, cùng chi nhánh, chống duplicate, đồng bộ trạng thái -> DA_PHAN_CONG) | `SYSTEM_ADMIN`, `BRANCH_MANAGER` |
| `DELETE` | `/api/repair-orders/{repairOrderId}/assignments/{assignmentId}` | Hủy phân công kỹ thuật viên (rollback về CHO_XU_LY nếu không còn phân công) | `SYSTEM_ADMIN`, `BRANCH_MANAGER` |

### Service / Repair Items Management Endpoints (`RepairItemController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/repair-orders/{repairOrderId}/items` | Danh sách dịch vụ trong phiếu sửa chữa (kèm thành tiền tính tự động) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST` |
| `POST` | `/api/repair-orders/{repairOrderId}/items` | Thêm dịch vụ vào phiếu sửa chữa (tự động lấy đơn giá theo bảng giá chi nhánh) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST` |
| `PUT` | `/api/repair-orders/{repairOrderId}/items/{itemId}` | Cập nhật số lượng, đơn giá, trạng thái của hạng mục dịch vụ | `SYSTEM_ADMIN`, `BRANCH_MANAGER` |
| `DELETE` | `/api/repair-orders/{repairOrderId}/items/{itemId}` | Xóa dịch vụ khỏi phiếu sửa chữa | `SYSTEM_ADMIN`, `BRANCH_MANAGER` |

### Repair Order Management Endpoints (`RepairOrderController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `POST` | `/api/repair-orders` | Tạo phiếu sửa chữa từ phiếu tiếp nhận (idempotent, branch-scoped) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST` |
| `GET` | `/api/repair-orders` | Lấy danh sách phiếu sửa chữa (SYSTEM_ADMIN: toàn hệ thống, BRANCH_MANAGER/RECEPTIONIST: chi nhánh mình) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST` |
| `GET` | `/api/repair-orders/{id}` | Lấy chi tiết phiếu sửa chữa theo ID (branch-scoped) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST` |
| `PUT` | `/api/repair-orders/{id}` | Cập nhật thông tin phiếu sửa chữa (ghi chú, thời gian bắt đầu, kết thúc) | `SYSTEM_ADMIN`, `BRANCH_MANAGER` |
| `PATCH` | `/api/repair-orders/{id}/status` | Cập nhật trạng thái phiếu sửa chữa (`CHO_XU_LY`, `DA_PHAN_CONG`, `DANG_SUA`, `CHO_KH_DUYET`, `TAM_DUNG`, `HOAN_TAT`, `HUY`) | `SYSTEM_ADMIN`, `BRANCH_MANAGER` |

### Reception / Check-in Endpoints (`ReceptionController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `POST` | `/api/reception/check-in/{appointmentId}` | Check-in tiếp nhận xe từ lịch hẹn (tạo PhieuTiepNhan, cập nhật DatLich -> DA_TIEP_NHAN, cập nhật Odometer) | `SYSTEM_ADMIN`, `RECEPTIONIST`, `BRANCH_MANAGER` |
| `GET` | `/api/reception` | Lấy danh sách phiếu tiếp nhận (SYSTEM_ADMIN: toàn hệ thống, RECEPTIONIST/BRANCH_MANAGER: chi nhánh mình) | `SYSTEM_ADMIN`, `RECEPTIONIST`, `BRANCH_MANAGER` |
| `GET` | `/api/reception/{id}` | Lấy chi tiết phiếu tiếp nhận theo ID (branch-scoped) | `SYSTEM_ADMIN`, `RECEPTIONIST`, `BRANCH_MANAGER` |

### Appointment Management Endpoints (`AppointmentController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/appointments` | Danh sách lịch hẹn (SYSTEM_ADMIN: toàn hệ thống, BRANCH_MANAGER/RECEPTIONIST: chi nhánh mình, CUSTOMER: lịch hẹn của mình) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `CUSTOMER` |
| `GET` | `/api/appointments/{id}` | Chi tiết lịch hẹn (ownership & branch check) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `CUSTOMER` |
| `POST` | `/api/appointments` | Đặt lịch hẹn mới (kiểm tra sở hữu xe, chi nhánh hoạt động, thời gian tương lai, chống trùng lịch) | `SYSTEM_ADMIN`, `CUSTOMER` |
| `PATCH` | `/api/appointments/{id}/cancel` | Hủy lịch hẹn (chỉ cho phép khi trạng thái là CHO_XAC_NHAN hoặc DA_XAC_NHAN) | `SYSTEM_ADMIN`, `BRANCH_MANAGER`, `RECEPTIONIST`, `CUSTOMER` |

### Vehicle Management Endpoints (`VehicleController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/brands` | Danh sách hãng xe hoạt động cho dropdown | Public |
| `GET` | `/api/brands/{brandId}/models` | Danh sách model hoạt động thuộc hãng đã chọn | Public |
| `GET` | `/api/vehicles` | Danh sách xe (SYSTEM_ADMIN: toàn bộ, CUSTOMER: chỉ xe của mình) | `SYSTEM_ADMIN`, `CUSTOMER` |
| `GET` | `/api/vehicles/{id}` | Chi tiết xe (ownership check) | `SYSTEM_ADMIN`, `CUSTOMER` |
| `POST` | `/api/vehicles` | Tạo xe mới bằng `maHangXe`/`maModel` (owner tự động từ JWT đối với CUSTOMER) | `SYSTEM_ADMIN`, `CUSTOMER` |
| `PUT` | `/api/vehicles/{id}` | Cập nhật xe (ownership check) | `SYSTEM_ADMIN`, `CUSTOMER` |
| `DELETE` | `/api/vehicles/{id}` | Xóa xe (ownership check + business constraint) | `SYSTEM_ADMIN`, `CUSTOMER` |

### Customer Management Endpoints (`CustomerController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/customers/me` | Lấy hồ sơ cá nhân của khách hàng đăng nhập | `CUSTOMER`, `SYSTEM_ADMIN` |
| `PUT` | `/api/customers/me` | Cập nhật hồ sơ cá nhân của khách hàng | `CUSTOMER`, `SYSTEM_ADMIN` |
| `GET` | `/api/customers` | Lấy danh sách toàn bộ khách hàng | `SYSTEM_ADMIN` |
| `GET` | `/api/customers/{id}` | Lấy chi tiết khách hàng theo ID | `SYSTEM_ADMIN` hoặc chính chủ `CUSTOMER` |
| `POST` | `/api/customers` | Tạo khách hàng mới | `SYSTEM_ADMIN` |
| `PUT` | `/api/customers/{id}` | Cập nhật thông tin khách hàng | `SYSTEM_ADMIN` hoặc chính chủ `CUSTOMER` |
| `PATCH` | `/api/customers/{id}/status` | Khóa/kích hoạt tài khoản khách hàng | `SYSTEM_ADMIN` |

### Employee Management Endpoints (`EmployeeController`)
| Method | Endpoint | Description | Permission |
|---|---|---|---|
| `GET` | `/api/employees` | Lấy danh sách nhân viên | `SYSTEM_ADMIN` (toàn hệ thống), `BRANCH_MANAGER` (chỉ chi nhánh mình) |
| `GET` | `/api/employees/{id}` | Lấy chi tiết nhân viên | `SYSTEM_ADMIN` (toàn hệ thống), `BRANCH_MANAGER` (chỉ chi nhánh mình) |
| `POST` | `/api/employees` | Tạo nhân viên mới | `SYSTEM_ADMIN` (bất kỳ chi nhánh), `BRANCH_MANAGER` (chỉ chi nhánh mình) |
| `PUT` | `/api/employees/{id}` | Cập nhật nhân viên | `SYSTEM_ADMIN` (bất kỳ chi nhánh), `BRANCH_MANAGER` (chỉ chi nhánh mình) |
| `PATCH` | `/api/employees/{id}/status` | Khóa/kích hoạt nhân viên | `SYSTEM_ADMIN` (bất kỳ chi nhánh), `BRANCH_MANAGER` (chỉ chi nhánh mình) |

### Admin User Management Endpoints (`AdminUserController`) — SYSTEM_ADMIN Only
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/users` | Lấy danh sách tài khoản người dùng và vai trò |
| `GET` | `/api/admin/users/{id}` | Lấy chi tiết thông tin và vai trò của tài khoản |
| `POST` | `/api/admin/users` | Tạo tài khoản mới (BCrypt hash mật khẩu, gán vai trò) |
| `PUT` | `/api/admin/users/{id}` | Cập nhật thông tin tài khoản và vai trò |
| `PATCH` | `/api/admin/users/{id}/status` | Khóa/vô hiệu hóa hoặc kích hoạt lại tài khoản |

## 7. Security & Authorization Architecture
| Layer | Convention |
|---|---|
| Authority prefix | `ROLE_SYSTEM_ADMIN`, `ROLE_CUSTOMER`, etc. |
| Customer Ownership | Khách hàng chỉ truy cập/sửa thông tin chính mình (`JWT → NguoiDung → KhachHang`), chặn truy cập profile khách hàng khác (403) |
| Employee Authorization | `SYSTEM_ADMIN` (global), `BRANCH_MANAGER` (own branch only), other roles denied (403) |
| User Management Authorization | `SYSTEM_ADMIN` only (`@PreAuthorize("hasRole('SYSTEM_ADMIN')")`) |
| Password Security | `BCryptPasswordEncoder` (never plaintext, never exposed in API) |
| 401 Unauthorized | Chưa đăng nhập / JWT không hợp lệ / hết hạn |
| 403 Forbidden | Đã đăng nhập nhưng không đủ quyền vai trò, sai chi nhánh, hoặc truy cập trái phép profile khách hàng khác |
| 404 Not Found | Tài nguyên không tồn tại |
| 409 Conflict | Mã nhân viên, tên đăng nhập hoặc email đã tồn tại |

## 8. Completed Tasks
- **TASK 01**: Project Foundation — PASS
- **TASK 02**: Database Mapping (35 JPA Entities) — PASS
- **TASK 03**: Authentication / JWT — PASS
- **TASK 03.1**: Development Seed Data & Auth Validation — PASS
- **TASK 04**: Authorization / RBAC — PASS (19/19 tests)
- **TASK 05**: Branch Authorization — PASS (31/31 tests)
- **TASK 06A**: User Management (`NguoiDung`) — PASS (48/48 tests)
- **TASK 06B**: Employee Management (`NhanVien`) — PASS (64/64 tests)
- **TASK 06C**: Customer Management (`KhachHang`) — PASS (76/76 tests)
- **TASK 07**: Vehicle Management (`Xe`) — PASS (101/101 tests)
- **TASK 08**: Appointment Management (`DatLich`) — PASS (132/132 tests)
- **TASK 09**: Reception / Check-in (`PhieuTiepNhan`) — PASS (151/151 tests)
- **TASK 10**: Repair Order Management (`PhieuSuaChua`) — PASS (174/174 tests)
- **TASK 11**: Technician Assignment (`PhanCong`) — PASS (215/215 tests)
- **EXTRA TASK**: Service / Repair Items Management (`PhieuSuaChua_DichVu`) — PASS (195/195 tests)
- **TASK 12**: Repair Progress / Service Execution (`TienDoSuaChua`) — PASS (234/234 tests)
- **TASK 13**: Parts / Inventory Management (`PhuTung`, `TonKho`, `PhieuSuaChua_PhuTung`, `GiaoDichKho`) — PASS (263/263 tests)
- **TASK 14**: Additional Quotation (`BaoGiaPhatSinh`, `BaoGiaPhatSinh_DichVu`, `BaoGiaPhatSinh_PhuTung`) — PASS (280/280 tests)
- **TASK 15**: Invoice / Payment (`HoaDon`, `HoaDon_DichVu`, `HoaDon_PhuTung`, `ThanhToan`) — PASS (298/298 tests)
- **TASK 16**: Notification Management (`ThongBao`) — PASS (313/313 tests)
- **TASK 17**: WebSocket Realtime (`WebSocketConfig`, `WebSocketAuthChannelInterceptor`, `WebSocketEventPublisher`) — PASS (326/326 tests)
- **TASK 18**: Chat / Realtime Conversation (`CuocHoiThoai`, `TinNhan`) — PASS (341/341 tests)
- **TASK 19**: Reports / Statistics (`ReportService`, `ReportController`) — PASS (**357/357 tests PASS**)
- **FRONTEND TASK 01**: Frontend Foundation (React 18, TypeScript, Vite, Router, JWT Auth, API Client, Layout Foundation) — PASS
- **FRONTEND TASK 02**: Authentication UI & User Experience (AutoCare Theme, Login UX, Show/Hide Password, User Dropdown, Role Navigation, Dashboard KPIs) — PASS
- **FRONTEND TASK 03**: Dashboard & Real Data Integration (Role-Specific Dashboards for Admin, Manager, Receptionist, Technician, Customer; Live Backend Reports API `/api/reports/*`, Vehicles `/api/vehicles`, Appointments `/api/appointments`) — PASS
- **FRONTEND TASK 05**: User / Employee / Customer Management (Employee List/Detail/Form/Status, Customer List/Detail/Form/Status/Vehicles, User List/Detail/Form/Status, RoleGuards for `/app/employees`, `/app/customers`, `/app/users`) — PASS
- **FRONTEND TASK 06**: Vehicle Management (Vehicle List/Detail/Form/Delete, Customer Ownership Protection, License Plate/VIN validation, RoleGuards for `/app/vehicles` for `ROLE_ADMIN` & `ROLE_CUSTOMER`) — PASS
- **FRONTEND TASK 07**: Appointment Management (role-scoped list, KPI/filter/search/date views, detail, create for `ROLE_ADMIN`/`ROLE_CUSTOMER`, cancel/confirm/receive theo quyền backend, loading/error/empty states, Real Data API `/api/appointments` & `/api/branches`, Customer/Branch Isolation, RoleGuards for `/app/appointments` cho `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_FRONT_DESK`, `ROLE_CUSTOMER`) — PASS (frontend production build)
- **MOBILE CUSTOMER REGISTRATION & LOGIN PIN STATE**: Public registration UI, phone-as-username contract, transactional customer account creation without `MaKhachHangCode`, fixed `ROLE_CUSTOMER`, BCrypt password hashing, login/session exposes `hasPin` from `NguoiDung.MaPinHash` — PASS (87 targeted backend tests, backend compile, 6 Flutter tests, Flutter analyze clean)
- **FRONTEND TASK 08**: Reception / Check-in Management (Vehicle Check-in Modal with ODO & exterior condition tracking, Reception Slips List/Detail, Direct Appointment-to-Reception Check-in, One-click Repair Order Creation, RoleGuards for `/app/reception` for `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_FRONT_DESK`) — PASS
- **INFRASTRUCTURE TASK 01**: Docker Development Environment (Docker Compose, Multi-stage Backend Dockerfile, React/Vite Frontend Dockerfile, MSSQL Container with Auto-Init & Named Volume Persistence, Zero Source Code Regressions) — PASS (381/381 backend tests, 0 build errors)

## 9. Next Step
**FRONTEND TASK 09 — REPAIR ORDER MANAGEMENT (LỆNH SỬA CHỮA & PHÂN CÔNG KỸ THUẬT)**


