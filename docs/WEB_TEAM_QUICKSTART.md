# Bàn giao cho nhóm web: lấy code, chạy và tích hợp thanh toán

Cập nhật 01/10/2026. Đọc tài liệu này trước, sau đó dùng [contract chi tiết](PAYMENT_SEPAY_WEB_HANDOFF.md) để làm giao diện.

## 1. Phạm vi bản bàn giao

- Backend: SePay payment-options, phiên QR 15 phút, status, webhook, chống ghi nhận trùng và khóa hóa đơn khi thu tiền.
- Database: migration V04 bổ sung PhienThanhToan và GiaoDichSePay. Git không mang theo dữ liệu SQL Server hoặc tài khoản/ngrok/SePay của máy tác giả.
- Mobile: danh sách/chi tiết hóa đơn, QR/thanh toán, lưu QR/mở MB trên Android; cùng các cập nhật hồ sơ khách, sửa xe, dự toán, trang chủ và tiến độ kỹ thuật viên đang có.
- Frontend trong bản bàn giao: /app/invoices còn placeholder. Bạn web triển khai theo mục 5; nếu đã có code riêng, tích hợp vào nhánh đó, không ghi đè thay đổi của mình.
- Local tác giả đã thử thanh toán Live số tiền nhỏ; cấu hình mặc định trong Git vẫn tắt SePay. Không sao chép ID hóa đơn local hoặc cho rằng tài khoản mẫu của hai máy giống nhau.

## 2. Lấy code

Nếu checkout sạch và đang trên main:

```powershell
git switch main
git pull --ff-only origin main
```

Nếu đang làm trên nhánh riêng, commit công việc của mình rồi `git fetch origin` và `git merge origin/main` trên nhánh đó, xử lý xung đột bình thường. Không dùng reset --hard.

Máy mới cần Git, Docker Desktop, Java 17, Maven và Node/npm. Flutter chỉ cần khi tự chạy mobile.

## 3. Database, backend và web

Các lệnh bắt đầu từ thư mục gốc repository. Nếu chưa có .env, sao chép .env.example thành .env và điền cấu hình local. Không ghi đè .env đã có, không commit .env.

### Terminal 1: SQL Server

```powershell
docker compose up -d --build sqlserver
docker compose ps
```

Compose này chạy SQL Server, không chạy backend/web. Với database mới, script init tạo schema và seed. Volume đã có sẽ không tự áp migration.

**Database đã có dữ liệu:** sao lưu theo quy trình của nhóm rồi áp riêng `database/migrations/V04__sepay_payment_sessions.sql` vào `GarageManagementSystem` bằng SSMS/sqlcmd. Ví dụ Docker mặc định, tại root:

```powershell
Get-Content .\database\migrations\V04__sepay_payment_sessions.sql -Raw |
  docker exec -i garage-sqlserver sh -c 'SQLCMDPASSWORD="$MSSQL_SA_PASSWORD" /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -C -d GarageManagementSystem -b'
```

Migration V04 chỉ tạo bảng/index nếu chưa có. Nếu database rất cũ, đối chiếu các migration trước đó và [DOCKER.md](DOCKER.md), [HANDOVER_WEB_HANDOFF.md](HANDOVER_WEB_HANDOFF.md). Không chạy lại toàn bộ GarageManagementSystem.sql trên DB hiện hữu vì file có đoạn DELETE phục vụ thử nghiệm từ trước. Không xóa Docker volume để cập nhật schema.

### Terminal 2: backend

Backend tự đọc `.env` ở root; không cần chạy script nạp biến. Từ thư mục root:

```powershell

Đặt các giá trị dạng NAME=value, bỏ dấu # ở đầu các biến thực sự muốn dùng. SQLSERVER_PASSWORD dùng cho Docker; nếu đổi mật khẩu DB, đặt SPRING_DATASOURCE_PASSWORD tương ứng cho backend. Cấu hình SPRING_DATASOURCE_URL nếu đổi host/port/database. Dùng giá trị không có dấu nháy; sửa .env phải restart backend. Nếu đã nạp biến bằng PowerShell trước đây, mở terminal mới để tránh giá trị cũ ghi đè file. Không cần cấu hình SePay để làm danh sách hóa đơn/thu tiền mặt; payment-options.available=false là trạng thái hợp lệ.

Kiểm tra `http://localhost:8080/api/brands` trả JSON. Backend 8080; database 1433.

### Terminal 3: frontend

```powershell
cd frontend
npm ci
npm run dev
```

Mở `http://localhost:3001`. `frontend/vite.config.ts` dùng envDir ở root: đặt `VITE_API_BASE_URL=http://localhost:8080/api` trong .env gốc nếu cần. Không đặt SEPAY_WEBHOOK_KEY vào biến VITE_* hoặc source browser. Đăng nhập role staff phù hợp trong database của mình; dùng seed development theo README nếu DB mới.

## 4. SePay và ngrok khi thử QR

Đọc [SEPAY_SETUP.md](SEPAY_SETUP.md) để điền SEPAY_ENABLED, SEPAY_ENVIRONMENT, WEBHOOK_KEY, BANK_CODE, BANK_NAME, ACCOUNT_NUMBER, ACCOUNT_NAME. Test/live phải khớp tài khoản và cấu hình provider; chữ live không tự liên kết ngân hàng.

- MB nhận tiền: dịch vụ QR dùng mã `MBBank`, không dùng SWIFT `MSCBVNVX`.
- Web chỉ đọc tài khoản nhận từ payment-options/session. Tài khoản nhận của garage khác với app ngân hàng người trả tiền.
- Giữ backend và terminal ngrok chạy trong lúc thử:

```powershell
ngrok http 8080
```

- Trên SePay, webhook POST tới `https://<domain-ngrok>/api/payments/sepay/webhook`; chọn xác thực API Key đúng khóa của backend. Cấu hình bóc mã `GAR` + 24 hex in hoa từ nội dung theo tài liệu SePay.
- Domain đổi thì cập nhật webhook. Không trỏ webhook về localhost:3001. SePay không truy cập được localhost của máy bạn nếu thiếu tunnel.
- Nếu dùng chung tài khoản nhận giữa hai người, thống nhất backend/webhook nào chịu trách nhiệm đối soát. Database riêng không chứa phiên QR của backend bên kia.
- Không gửi webhook giả vào môi trường live để biến hóa đơn thành đã trả. Khi dùng sandbox, dùng cấu hình và dữ liệu thử phù hợp.

## 5. Việc bạn web cần làm

1. Thêm feature invoices với service qua `apiClient` và `API_ENDPOINTS.INVOICES`; thay placeholder /app/invoices, giữ navbar/role/route hiện tại.
2. List: GET /api/invoices trả ApiResponse với data là mảng (chưa phân trang). Detail: GET /api/invoices/{id}. Dùng các trường tiền backend: tongTien, giamGia, thue, thanhTien, daThanhToan, conLai; có services, parts, payments.
3. Nút xuất hóa đơn trên phiếu chính: POST /api/repair-orders/{id}/invoice, body {giamGia:0,thue:0}. Đây là số tiền VND, không phải phần trăm. Đọc lại qua GET cùng path khi đã có hóa đơn. Không tạo hóa đơn bằng cách gửi lại dòng dịch vụ/phụ tùng do frontend tính.
4. Thu tiền mặt: POST /api/invoices/{id}/payments, body {soTien:...,phuongThuc:"TIEN_MAT",maGiaoDich:...}. Tải lại detail; không dùng API này để giả xác nhận SePay.
5. QR: GET payment-options; nếu available=true thì POST payment-sessions **không body**. Hiển thị QR, amount, receiver, transferContent và expiresAt. GET payment-sessions/{sessionId} mỗi khoảng 5 giây khi màn đang mở/chờ, dừng khi rời màn/đã thanh toán; kiểm tra ngay khi tab hoạt động trở lại.
6. Thành công khi invoiceStatus=DA_THANH_TOAN và remainingAmount=0. EXPIRED: kiểm tra đã trả chưa trước khi tạo mới. REQUIRES_REVIEW: hướng dẫn đối soát, không yêu cầu trả lần nữa. Không có nút tự đổi status thành công.
7. Xử lý 401/403/404 bằng cách bỏ dữ liệu phiên không còn được phép xem; lỗi mạng tạm thời cho phép thử lại, không báo đã thanh toán. Không hiển thị 0đ giả khi chưa tải dữ liệu.

Các API đều nhận JWT qua apiClient. Roles thực: ROLE_ADMIN, ROLE_MANAGER, ROLE_FRONT_DESK cho xuất/thu tiền; CUSTOMER chỉ hóa đơn chính chủ và phiên của mình. TECHNICIAN không được dùng API payment-session. Backend là nơi quyết định branch/ownership.

Contract, mẫu JSON và các trường hợp lỗi đầy đủ nằm trong [PAYMENT_SEPAY_WEB_HANDOFF.md](PAYMENT_SEPAY_WEB_HANDOFF.md). Schema/DTO source là đối chiếu cuối cùng khi có khác biệt.

## 6. Kiểm tra nghiệm thu

- Staff đúng chi nhánh xem/xuất/thu tiền; staff khác chi nhánh và khách khác không xem được.
- Mở QR cùng hóa đơn ở web/mobile không ghi nhận tiền hai lần. Web/mobile tải lại thấy cùng số dư/trạng thái.
- Thử unavailable, hết hạn, mất mạng, quay lại tab; người đã chuyển không bị hướng dẫn trả thêm.
- Hóa đơn 2.000đ là fixture local của tác giả, không tự xuất hiện sau git pull. Tạo dữ liệu thử riêng bằng quy trình/API của nhóm; không sửa hóa đơn đã nhận tiền để thử lại.
- API xuất hóa đơn hiện chưa enforce đầy đủ mọi phiếu chính/con hoàn tất và khóa hạng mục sau xuất. UI không thay thế được guard backend; phần hoàn thiện này cần công việc backend riêng trước nghiệm thu toàn bộ nghiệp vụ.
- Chưa có UI/API đối soát/hoàn tiền hoặc khách duyệt phát sinh; không giả định đã hoàn thành.

## 7. Điện thoại Android qua USB nếu cần thử chung

```powershell
adb devices
adb -d reverse tcp:8080 tcp:8080
cd mobile
flutter pub get
flutter run -d <device-id> --dart-define=API_BASE_URL=http://127.0.0.1:8080/api
```

10.0.2.2 chỉ dành cho Android emulator. Điện thoại phải cấp quyền USB debugging; `unauthorized` thì chấp nhận hộp thoại trên điện thoại. Native lưu QR/mở MB cần dừng/chạy lại app đầy đủ, không chỉ hot reload. Android 10+ ảnh ở Pictures/AutoCare; chưa cài MB vẫn lưu ảnh và báo mở thủ công.
## Kiểm tra bản bàn giao ngày 01/10/2026

- Backend `mvn test`: BUILD SUCCESS, 493 tests được báo cáo; 0 failures/errors, 3 SQL integration tests opt-in bị skip (490 tests chạy đạt).
- Mobile `flutter analyze`: sạch; `flutter test`: 137/137 đạt. Không chạy/build Flutter trong phiên push.
- Frontend `npm run build`: đạt; cảnh báo bundle trên 500 kB, không chặn build. Không thay đổi source React trong bản bàn giao.
- Bộ kiểm thử không thay thế nghiệm thu SePay/ngân hàng. Người dùng đã xác nhận chạy điện thoại ổn; backend tiếp tục là nơi xác nhận thanh toán.