# Bàn giao web: xuất hóa đơn và thanh toán ngân hàng

Cập nhật 01/10/2026. Backend SePay và giao diện mobile đã được triển khai. Local đã ghi nhận thanh toán Live 2.000đ; người dùng xác nhận luồng chạy điện thoại đã ổn. Máy mới vẫn phải cấu hình tài khoản nhận và webhook riêng. Phần web vẫn do bạn cùng nhóm thực hiện. Không coi yêu cầu báo giá khách duyệt là tính năng đã được làm trong phiên thanh toán này.

## 1. Luồng thống nhất

Phiếu sửa chữa và phiếu phát sinh hoàn tất → tiếp tân bấm **Xuất hóa đơn** trên phiếu chính → backend tổng hợp thành một hóa đơn → web/mobile cùng đọc → thanh toán tiền mặt tại quầy hoặc chuyển khoản.

Không nhập lại dịch vụ/phụ tùng vào một hóa đơn độc lập. Bỏ riêng kế hoạch phiếu thô TECHNICIAN, giữ công việc và cập nhật tiến độ. Khách duyệt/từ chối phát sinh trên mobile trước khi sửa là yêu cầu đã chốt, nhưng API/màn duyệt phát sinh chưa được triển khai trong phiên này.

## 2. Phần đã có và cần làm

| Thành phần | Hiện trạng |
| --- | --- |
| API xuất hóa đơn | Có `POST /api/repair-orders/{id}/invoice`; gộp dòng phiếu chính và phiếu con trực tiếp không hủy |
| API đọc hóa đơn | Có list/detail và lịch sử thanh toán; CUSTOMER chính chủ, staff theo chi nhánh |
| Thu tiền tại quầy | API staff đã có; bổ sung khóa hóa đơn để phối hợp với webhook |
| Backend SePay | Có thông tin nhận tiền, phiên QR 15 phút, kiểm tra trạng thái, webhook xác thực Apikey, chống lặp và lưu trường hợp cần đối soát |
| Mobile | Hóa đơn → Thanh toán ngân hàng → thông tin nhận/QR/sao chép → chờ kết quả; polling 5 giây và kiểm tra khi resume |
| Web `/app/invoices` | Vẫn placeholder; chưa sửa React trong phiên này |
| SePay thật | Đã có giao dịch Live local được ghi nhận; bản Git mặc định `SEPAY_ENABLED=false`, không chứa cấu hình bí mật |

## 3. Web cần triển khai

1. Nút **Xuất hóa đơn** trên phiếu sửa chữa chính, tải lại hóa đơn đã có khi gặp tạo trùng. Hiển thị nội dung backend tổng hợp, không gửi giá dòng tự tính từ browser.
2. Trang danh sách/chi tiết hóa đơn dưới `/app/invoices`: dòng dịch vụ, phụ tùng, giảm giá, thuế, thành tiền, đã trả, còn lại, lịch sử.
3. Thu tiền mặt tại quầy: staff xác nhận tiền thực nhận, gọi API hiện có, tải lại hóa đơn.
4. Chuyển khoản: đọc payment-options → tạo/tái sử dụng payment-session → hiển thị QR và thông tin nhận → GET trạng thái định kỳ → tải lại hóa đơn khi đã thanh toán.
5. Không có nút tự xác nhận thành công cho SePay. Không gọi API thu tiền staff để giả lập giao dịch ngân hàng.
6. UI xử lý unavailable/loading/error/expired/requires-review/paid; lỗi mạng không đồng nghĩa thất bại và không khuyến khích chuyển lần nữa.
7. Giữ API tập trung, ProtectedRoute/RoleGuard, navbar và token AutoCare hiện hành; tham khảo Stitch trước khi sửa UI.

MVP cấu hình **một tài khoản nhận của garage**, dùng chung các chi nhánh; mỗi phiên vẫn gắn hóa đơn có quyền chi nhánh riêng. Đây là ngân hàng nhận, không phải danh sách app ngân hàng của khách. Android mobile đã có lưu QR và mở MB Bank; lưu ảnh không đồng nghĩa xác nhận thanh toán. Nhiều tài khoản nhận/chi nhánh và các app ngân hàng khác là phần mở rộng.

## 4. Contract đang triển khai

API nghiệp vụ bọc `ApiResponse { success, message, data }`, dùng JWT. Role cho ba API SePay nghiệp vụ: ADMIN/MANAGER/FRONT_DESK/CUSTOMER. TECHNICIAN không được tạo/xem phiên. Service tái sử dụng kiểm tra ownership/branch hóa đơn.

| Method/path | Body | Ý nghĩa |
| --- | --- | --- |
| `POST /api/repair-orders/{id}/invoice` | `{ "giamGia": 0, "thue": 0 }` | Staff xuất hóa đơn; hai giá trị là số tiền, không phải % |
| `GET /api/invoices` | Không | Danh sách được phép xem |
| `GET /api/invoices/{id}` | Không | Chi tiết gồm `services`, `parts`, `payments` |
| `POST /api/invoices/{id}/payments` | `{ "soTien": 100000, "phuongThuc": "TIEN_MAT", "maGiaoDich": "<ma-thu>" }` | Staff thu tiền tại quầy; không dùng prefix `SEPAY:` |
| `GET /api/invoices/{id}/payment-options` | Không | Khả dụng và tài khoản nhận |
| `POST /api/invoices/{id}/payment-sessions` | **Không** | Tính số dư tại server, tạo hoặc trả lại phiên còn hiệu lực |
| `GET /api/invoices/{id}/payment-sessions/{sessionId}` | Không | Trạng thái phiên/hóa đơn |
| `POST /api/payments/sepay/webhook` | Payload nhà cung cấp | SePay gọi, xác thực riêng `Authorization: Apikey ...` |

Không gửi owner, branch, role, amount hoặc STK tùy ý vào POST tạo phiên. Hai client mở cùng hóa đơn sẽ tái sử dụng phiên đang chờ phù hợp, do backend khóa theo hóa đơn; không cần client tự tạo khóa idempotency cho endpoint này.

`payment-options.data`:

```json
{
  "available": false,
  "message": "Garage chưa cấu hình thanh toán ngân hàng. Vui lòng liên hệ quầy tiếp nhận.",
  "environment": "test",
  "remainingAmount": 1190000,
  "receivers": []
}
```

Khi sẵn sàng, `receivers` có một đối tượng gồm `bankCode`, `bankName`, `accountNumber`, `accountName`. Khi unavailable, không hiển thị QR hoặc tài khoản giả.

Response phiên có: `sessionId`, `invoiceId`, `status`, `environment`, `amount`, `currency` (`VND`), `transferContent`, `expiresAt` (ISO UTC), `receiver`, `qrImageUrl`, `invoiceStatus`, `remainingAmount`.

| `status` của phiên | Hiển thị |
| --- | --- |
| `PENDING` | Chờ ngân hàng; QR chỉ dùng khi có URL và chưa hết hạn |
| `SUCCEEDED` | Phiên đã nhận đúng tiền |
| `EXPIRED` | Hết thời gian hoặc số dư/trạng thái hóa đơn đã đổi; kiểm tra trước khi chuyển thêm |
| `REQUIRES_REVIEW` | Liên hệ garage đối soát, không mở thêm phiên để trả lần nữa |

Hóa đơn `DA_THANH_TOAN` và `remainingAmount = 0` là điều kiện hiển thị hóa đơn đã trả đủ (kể cả trả tại quầy). Một phiên có thể hết hiệu lực do hóa đơn được trả bằng kênh khác; không suy ra rằng tiền đã được trả qua chính phiên đó.

Các lỗi: 401 chưa đăng nhập; 403 sai role/owner/branch; 404 không tìm thấy phiên/hóa đơn; 400 chưa cấu hình, không còn nợ, đang đối soát hoặc số dư lẻ không hỗ trợ chuyển VND nguyên. Phiên đã có `REQUIRES_REVIEW` sẽ chặn mở phiên mới cho hóa đơn đó.

## 5. Tiền và webhook

- Chỉ ghi `ThanhToan.THANH_CONG` khi tiền vào có mã thanh toán khớp chính xác, đúng môi trường/tài khoản/ngân hàng, phiên chưa hết hạn, số tiền đúng số dư cần trả.
- Mã nội dung server sinh: `GAR` + 24 ký tự hex in hoa. Cấu hình SePay bóc đúng mã này vào `code`. Không đoán mã từ số hóa đơn hoặc nội dung tự do khi code thiếu.
- Mã giao dịch bền vững: `SEPAY:<test|live>:<provider-id>` là PK trong `GiaoDichSePay`. Retry đã xử lý trả success và không ghi thêm tiền.
- Receipt luôn INSERT, không dùng JPA merge để ghi đè provider ID đã tồn tại; nếu trùng đồng thời, database từ chối và transaction rollback, provider retry sẽ nhận lại success khi receipt đã được xử lý.
- Ghi thanh toán và receipt webhook trong cùng transaction. Invoice lock phối hợp với API thu tiền tại quầy; refresh entity sau khi lấy lock tránh trạng thái cũ trong persistence context.
- Sai/thiếu/thừa tiền, sai tài khoản, code không nhận diện, đến muộn hoặc sau khi hóa đơn đã trả: lưu receipt để đối soát, không tự trả đủ/hoàn tiền.
- Các bản ghi không áp dụng nằm ở `GiaoDichSePay.KetQua`. Chưa có giao diện/API xử lý đối soát thủ công hoặc hoàn tiền; cần quy trình staff trước khi dùng thật.
- Thời hạn phiên không chặn được tiền đến tài khoản ngân hàng. Khách đã chuyển tiền phải liên hệ đối soát, không chuyển lại chỉ vì QR hết hạn.
- Kết quả được đồng bộ qua GET/polling; chưa phát notification/realtime riêng cho hóa đơn hoặc giao dịch này.

## 6. Schema và dữ liệu test

Người dùng đã duyệt thêm bảng và áp dụng local ngày 30/09/2026:

- `PhienThanhToan`: FK hóa đơn, số tiền, nội dung unique, snapshot ngân hàng nhận, môi trường, trạng thái và thời gian UTC.
- `GiaoDichSePay`: PK chống trùng theo môi trường/provider ID, liên kết phiên tùy chọn, tiền, tài khoản, nội dung, tham chiếu, kết quả và thời gian UTC. Không lưu API key hay full payload ngân hàng.
- Migration: `database/migrations/V04__sepay_payment_sessions.sql`. Schema chuẩn thực tế: `database/GarageManagementSystem.sql`. Backend không tự chạy DDL.

Local đã có **hóa đơn #1**, phiếu chính #2002, khách #1001, tổng **1.190.000đ**, chưa trả tiền. Tài khoản mobile người dùng cung cấp đã được xác minh thuộc khách này. Xem [mẫu test](PAYMENT_TEST_INVOICE_SAMPLE.md). ID này chỉ xác minh trên local, không giả định có trên máy bạn web.

## 7. Cách tích hợp và cấu hình

Bắt đầu bằng [WEB_TEAM_QUICKSTART.md](WEB_TEAM_QUICKSTART.md), sau đó đọc [SEPAY_SETUP.md](SEPAY_SETUP.md). Nhóm lấy cả backend, migration, tài liệu và mobile từ cùng nhánh/commit sau khi chủ dự án yêu cầu push; không copy riêng một DTO rồi dùng backend cũ. Không đưa secret SePay vào Git.

Bạn web có thể làm UI từ các contract trên ngay. Khi SePay chưa cấu hình, dùng trạng thái unavailable đúng như mobile. Khi cấu hình test account ở SePay Test Mode, dùng giao dịch giả để thử toàn luồng; không cần khách chuyển tiền thật.

## 8. Giới hạn cần xử lý trước nghiệm thu toàn bộ nghiệp vụ xuất hóa đơn

- API xuất hóa đơn hiện có vẫn chỉ chặn phiếu hủy/tạo trùng theo phiếu, chưa enforce toàn bộ phiếu chính/con phải hoàn tất, chưa khóa mọi thay đổi hạng mục sau xuất. Phiên này tập trung thanh toán và không thay thế module xuất hóa đơn hiện hữu.
- DTO dòng hóa đơn chưa phân biệt dòng gốc/phát sinh; cần contract bổ sung nếu web/mobile muốn nhóm riêng.
- API/màn duyệt phát sinh chưa làm. Không coi phiếu con không hủy là bằng chứng khách đồng ý.
- Web hóa đơn/thu ngân trong checkout bàn giao vẫn là placeholder. Mobile đã được người dùng thử trên điện thoại; local có giao dịch Live, chưa nghiệm thu mọi trường hợp lỗi hoặc ngân hàng khác.
- Payment test mode phải dùng database thử riêng khi nhóm chuyển sang production; test mode vẫn mô phỏng cập nhật hóa đơn trong DB đang cấu hình.
