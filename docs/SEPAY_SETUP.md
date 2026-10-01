# Cấu hình SePay cho hóa đơn Garage Management

Code đã có; mặc định tắt tích hợp vì chưa có tài khoản nhận và webhook key. Không có STK giả, endpoint tự đánh dấu thành công hoặc secret SePay trong mobile/web.

Rà soát hoàn tất ngày 01/10/2026: 30 test backend thanh toán (gồm 3 test SQL Server) và 33 test mobile hóa đơn/thanh toán đạt; Flutter analyze sạch. Receipt SePay chỉ INSERT, không merge/update bản ghi cùng provider ID. Hóa đơn mẫu #1 vẫn chưa thanh toán sau các test rollback. Đây là kết quả kiểm tra trước tích hợp. Sau đó local đã có giao dịch Live 2.000đ và người dùng xác nhận chạy điện thoại ổn; xem PAYMENT_TEST_INVOICE_SAMPLE.md. Máy mới cần cấu hình riêng.

## 1. Chuẩn bị database

Schema chuẩn: `database/GarageManagementSystem.sql`. Database đã tồn tại cần áp `database/migrations/V04__sepay_payment_sessions.sql` vào đúng database sau khi người quản lý môi trường duyệt. Local của người dùng đã được duyệt và áp ngày 30/09/2026, không cần chạy lại để dùng mẫu.

Hai bảng mới: `PhienThanhToan`, `GiaoDichSePay`. Spring Boot vẫn `ddl-auto=none`. Migration thêm bảng/index, không xóa dữ liệu, không sửa bảng cũ. Không chạy lại toàn bộ schema để nâng cấp DB đã có dữ liệu.

## 2. Liên kết ngân hàng trong SePay

Bắt đầu bằng **SePay Test Mode** và tài khoản thử trong môi trường đó. Môi trường Live chỉ dùng khi nhóm chủ động nghiệm thu tiền thật. Mã QR là thông tin chuyển khoản; SePay báo giao dịch cho backend qua webhook. [Tài liệu Test Mode](https://docs.sepay.vn/test-mode.html), [QR và form thanh toán](https://developer.sepay.vn/vi/sepay-webhooks/tao-qr-va-form-thanh-toan).

MVP hỗ trợ một tài khoản nhận, dùng chung garage. Người trả có thể dùng app ngân hàng của họ để quét/chuyển tiền đến tài khoản nhận đó.

## 3. Biến môi trường backend

| Biến | Giá trị |
| --- | --- |
| `SEPAY_ENABLED` | `true` khi đã cấu hình đủ; mặc định `false` |
| `SEPAY_ENVIRONMENT` | `test` hoặc `live`; mặc định `test` |
| `SEPAY_WEBHOOK_KEY` | Khóa bí mật tự cấu hình trùng với webhook SePay, tối thiểu 32 ký tự |
| `SEPAY_BANK_CODE` | Mã ngân hàng được dịch vụ QR hỗ trợ |
| `SEPAY_BANK_NAME` | Tên ngân hàng đúng giá trị `gateway` trong webhook, ví dụ tên do SePay trả về; kiểm tra trong môi trường test |
| `SEPAY_ACCOUNT_NUMBER` | STK nhận đã liên kết đúng môi trường SePay |
| `SEPAY_ACCOUNT_NAME` | Tên chủ tài khoản nhận |

Các biến được Spring đọc qua `backend/src/main/resources/application.properties`. File `.env` ở root không tự được Spring nạp khi chạy Maven trực tiếp: đặt biến trong terminal/process hoặc Run Configuration của IDE trước khi khởi động backend. Không ghi secret vào source, tài liệu, ảnh chụp hoặc chat.

Ví dụ cấu hình không chứa giá trị thật:

```powershell
$env:SEPAY_ENABLED = 'true'
$env:SEPAY_ENVIRONMENT = 'test'
$env:SEPAY_BANK_CODE = '<ma-ngan-hang>'
$env:SEPAY_BANK_NAME = '<gateway-trong-webhook>'
$env:SEPAY_ACCOUNT_NUMBER = '<stk-test-da-lien-ket>'
$env:SEPAY_ACCOUNT_NAME = '<ten-chu-tai-khoan>'
# Nhập SEPAY_WEBHOOK_KEY qua cấu hình bí mật của môi trường/IDE.
# Tại thư mục backend:
mvn spring-boot:run
```

Không dùng nguyên placeholder để chuyển tiền. Không dùng cùng key giữa sandbox và live. `SEPAY_ENVIRONMENT` chọn namespace lưu giao dịch, không tự chuyển tài khoản SePay sang sandbox và không ngăn việc chuyển tiền thật vào một STK thật. Khi thử sandbox phải dùng đúng tài khoản/key/webhook sandbox và database development.

## 4. Webhook

### Lưu ý mã ngân hàng QR

`SEPAY_BANK_CODE` phải dùng mã được [danh sách ngân hàng của dịch vụ QR](https://vietqr.app/banks.json) hỗ trợ, ví dụ `MBBank` cho MB. Không nhập SWIFT `MSCBVNVX`: kiểm tra local ngày 01/10/2026 cho thấy dịch vụ trả HTTP 200 nhưng nội dung HTML thay vì PNG, khiến Flutter báo không tải được QR. `SEPAY_BANK_NAME` vẫn phải khớp `gateway` của webhook.

Nếu thay mã ngân hàng/môi trường: nạp lại `.env` vào terminal Maven, khởi động lại backend, quay lại hóa đơn và tạo phiên mới để lưu cấu hình nhận tiền mới. Không dùng QR cũ. Hóa đơn local #2 là fixture riêng 1.000đ; chỉ thử tiền thật sau khi tài khoản nhận và webhook Live đã kết nối đúng. Không đổi số tiền của QR hóa đơn #1.

- URL: `https://<backend-public-host>/api/payments/sepay/webhook`.
- POST JSON, sự kiện tiền vào, lọc tài khoản nhận đã cấu hình.
- Chọn xác thực **API Key**, cùng key với `SEPAY_WEBHOOK_KEY`; provider gửi `Authorization: Apikey <key>`.
- Backend kiểm tra key bằng phép so sánh constant-time. Chỉ webhook này được bỏ JWT; các API hóa đơn/phiên vẫn phải đăng nhập và có quyền.
- Cấu hình mã thanh toán tiền tố **GAR**, hậu tố **24 ký tự chữ/số hex**, tổng 27 ký tự. Nội dung QR do server sinh, không sửa tay. SePay cần trích đúng vào trường `code`.
- Backend chỉ nhận mã khớp chính xác. `code` null/không khớp được lưu đối soát, không tự dò số hóa đơn trong nội dung.
- URL localhost của máy phát triển không nhận callback từ SePay. Cần một backend HTTPS công khai hoặc đường hầm được nhóm cho phép; chưa tự triển khai/mở đường hầm trong phiên này.

SePay yêu cầu HTTP 200/201 và JSON có `success: true`; backend trả `ApiResponse` có trường này sau khi transaction lưu xong. Retry giao dịch đã xử lý sẽ nhận success mà không cộng tiền lần nữa. [Tích hợp webhook và xác thực](https://docs.sepay.vn/tich-hop-webhooks.html).

## 5. Thử không chuyển tiền thật

1. Dùng hóa đơn mẫu chưa thanh toán. Local hiện là #1 của khách #1001, số tiền 1.190.000đ.
2. Mobile: Tài khoản → Hóa đơn của tôi → #1 → Thanh toán ngân hàng → Tiếp tục thanh toán.
3. Lấy **số tiền và nội dung của phiên hiện tại**, không dùng lại mã QR đã hết 15 phút.
4. Trong SePay Test Mode, tạo giao dịch giả tiền vào đúng tài khoản, số tiền và nội dung. SePay gửi webhook đến backend.
5. App tự GET sau khoảng 5 giây hoặc khi quay lại từ nền. Cần `invoiceStatus=DA_THANH_TOAN` và số dư 0 để hiển thị hóa đơn đã trả đủ.
6. Kiểm tra GET hóa đơn trên web/API cũng thấy kết quả đó. Gửi lại cùng giao dịch không tạo payment thứ hai.

Các test bắt buộc thêm khi kết nối provider: thiếu/thừa tiền, mã sai, tài khoản sai, giao dịch sau hết hạn, thu tiền mặt trước callback, mất mạng rồi mở lại app. Không dùng nút “đã chuyển” để tự ghi tiền.

## 6. Đối soát và giới hạn

`GiaoDichSePay.KetQua` gồm `APPLIED`, `UNKNOWN_CODE`, `WRONG_ENVIRONMENT`, `WRONG_RECEIVER`, `LATE_OR_CLOSED`, `INVOICE_CLOSED`, `AMOUNT_MISMATCH`, `IGNORED_OUT`.

Giao dịch không áp dụng được giữ để xử lý, không tự coi là trả đủ hoặc hoàn tiền. Phiên liên quan có thể sang `REQUIRES_REVIEW`, chặn mở phiên tiếp theo; cần quầy/nhóm đối chiếu giao dịch trước khi quyết định nghiệp vụ. Chưa có API/giao diện giải quyết đối soát/hoàn tiền trong MVP; không sửa trạng thái thủ công tùy tiện hoặc dùng lại giao dịch cũ để ghi thêm tiền.

Hết hạn phiên không khóa được tài khoản ngân hàng. QR trên cùng điện thoại có thể dùng bằng cách sao chép STK/số tiền/nội dung qua app ngân hàng; phiên này chưa làm tải ảnh QR, chia sẻ ảnh hoặc deep link mở từng ngân hàng.

## 7. Kiểm thử cục bộ

```powershell
# Backend unit + controller (cần SQL Server local cho SpringBootTest hiện hữu):
mvn '-Dtest=SePayServiceTest,SePayControllerTest,PaymentServiceTest' test

# Test tích hợp opt-in, chỉ dùng database local có fixture #2002/#1001:
$env:SEPAY_LOCAL_INTEGRATION = 'true'
mvn '-Dtest=SePayLocalIntegrationTest' test

# Mobile, tại thư mục mobile:
flutter analyze --no-pub
flutter test --no-pub
```

Test tích hợp dùng tài khoản ngân hàng giả chỉ trong test, không gọi SePay, không tải QR và rollback giao dịch. Có kiểm tra round trip SQL/replay, khóa hóa đơn giữa hai transaction độc lập và việc INSERT trùng provider ID bị database từ chối. Người dùng tự chạy/build Flutter khi cần; không phải điều kiện của bộ test này.

### Xác minh thanh toán Live 01/10/2026 08:11

Hóa đơn local #2 đã thanh toán đủ 2.000đ qua webhook Live. Nguyên nhân chậm cập nhật là tunnel ngrok không chạy; sau khi khôi phục, nhận POST thành công, receipt APPLIED, phiên SUCCEEDED và hóa đơn DA_THANH_TOAN. Có một ThanhToan thành công, hóa đơn vẫn tồn tại. Chưa quan sát màn hình mobile sau callback; người dùng cần tải lại. Đây là kiểm chứng giao dịch đơn lẻ, chưa xác minh toàn bộ các trường hợp lỗi trên provider.


## Nạp cấu hình khi chạy Maven

Spring Boot không tự đọc .env ở root. Xem mục 3 của [WEB_TEAM_QUICKSTART.md](WEB_TEAM_QUICKSTART.md) để nạp biến trong cùng terminal chạy backend; restart backend sau khi đổi test/live. Nếu mobile vẫn hiện test dù .env ghi live, kiểm tra biến môi trường của tiến trình backend và mở lại màn thanh toán.
