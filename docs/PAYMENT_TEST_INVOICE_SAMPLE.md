# Hóa đơn mẫu thanh toán đã tạo trên local

## Bổ sung 01/10/2026: hóa đơn thử 1.000đ

Theo yêu cầu thử chuyển khoản thật số tiền nhỏ, đã tạo **hóa đơn #2** cho khách #1001, phiếu sửa chữa #3002, trạng thái `CHUA_THANH_TOAN`, chưa có thanh toán. Có phiếu tiếp nhận và dòng dịch vụ nguồn riêng, giữ nguyên ownership của xe #1001. Tổng dịch vụ 350.000đ, giảm giá mẫu 349.000đ, thuế 0, còn phải trả **1.000đ**. Đây là dữ liệu thử local, không phải dịch vụ thực tế đã thực hiện.

Script idempotent: `D:/KLCN/Garage_Management_Work_Sessions/2026-10-01_create-1000vnd-test-invoice.sql`. Không sửa hóa đơn #1 và các phiên của hóa đơn đó. Chọn hóa đơn **#2** khi thử số tiền nhỏ; không tự nhập 1.000đ cho QR của hóa đơn #1.

Đã xác minh dịch vụ QR trả ảnh PNG cho mã ngân hàng `MBBank`; mã SWIFT `MSCBVNVX` trả HTML và gây lỗi tải ảnh trên mobile. Local đã sửa mã ngân hàng và chuyển `SEPAY_ENVIRONMENT=live` theo yêu cầu chuyển thật; phải nạp lại biến và khởi động backend. Chưa xác minh liên kết ngân hàng trên SePay Live, chưa có giao dịch thật/webhook end-to-end. Cần kiểm tra tài khoản nhận đã kết nối trên SePay trước khi người dùng chuyển tiền.

## Hóa đơn mẫu ban đầu

Cập nhật 30/09/2026. Sau khi người dùng yêu cầu triển khai và xác nhận tài khoản, đã tạo bản ghi mẫu trong `GarageManagementSystem` của container `garage-sqlserver`. Không có giao dịch thanh toán thật hoặc tiền thử được giữ lại sau kiểm thử.

| Dữ liệu | Giá trị đã xác minh |
| --- | --- |
| Hóa đơn | **1** |
| Phiếu sửa chữa | **2002**, HOAN_TAT |
| Phiếu tiếp nhận | 5002 |
| Khách hàng / người dùng / xe | 1001 / 1001 / 1001 |
| Chi nhánh | 1 |
| Nhân viên ghi trong hóa đơn mẫu | 2 |
| Thành tiền / còn lại | **1.190.000đ / 1.190.000đ** |
| Trạng thái | CHUA_THANH_TOAN |

Tài khoản mobile người dùng cung cấp đã được đối chiếu trực tiếp với khách #1001. Không đổi ownership, trạng thái sửa chữa, tài khoản hoặc mật khẩu để làm mẫu.

## Dòng hóa đơn

| Loại | Mã dòng phiếu nguồn | Mã danh mục | Số lượng | Đơn giá | Thành tiền |
| --- | --- | --- | --- | --- | --- |
| Dịch vụ | 1002 | 2 | 1 | 350.000 | 350.000 |
| Phụ tùng | 2002 | 1 | 4 | 180.000 | 720.000 |
| Phụ tùng | 2003 | 2 | 1 | 120.000 | 120.000 |

Thuế và giảm giá bằng 0. Tổng 1.190.000đ. Mã hóa đơn lấy từ bản ghi thực, không đặt ID giả. Fixture được tạo bằng script SQL local có transaction, kiểm tra chủ xe/phiếu hoàn tất/tổng tiền/không có phiếu con, chống tạo trùng; không phải chứng cứ đã bấm xuất hóa đơn trên web.

Script để truy vết: `D:/KLCN/Garage_Management_Work_Sessions/2026-09-30_create-test-invoice.sql`. Không chạy script với máy/database khác nếu chưa xác minh lại các mã. Lần chạy đầu gặp SET QUOTED_IDENTIFIER không phù hợp và rollback; bản script hiện tại đã khai báo SET options đúng cho cột tính toán và tạo thành công.

## Cách thử mobile

1. Người dùng tự chạy backend và app như bình thường; phiên làm việc không chạy/build Flutter.
2. Đăng nhập tài khoản khách đã cung cấp.
3. **Tài khoản → Hóa đơn của tôi → Hóa đơn #1 → Thanh toán ngân hàng**.
4. Mặc định chưa cấu hình SePay, app hiển thị garage chưa cấu hình, không sinh QR giả.
5. Sau khi cấu hình theo [SEPAY_SETUP.md](SEPAY_SETUP.md), chọn tài khoản nhận, mở phiên QR và mô phỏng webhook bằng SePay Test Mode.

Web sau này dùng cùng `GET /api/invoices/1` và các endpoint payment-session. Hóa đơn #1 chỉ tồn tại trên local hiện tại; người làm web cần dữ liệu riêng tương ứng hoặc dùng backend chung được nhóm thống nhất.

## Giới hạn

Local chưa có phiếu phát sinh con: tất cả bốn phiếu tại thời điểm kiểm tra đều MaPhieuCha NULL. Bộ mẫu này thử thanh toán dịch vụ/phụ tùng, chưa nghiệm thu gộp phát sinh. Không tự liên kết phiếu 1002 với 2002 chỉ để tạo mẫu.

Không POST thu tiền staff để giả lập đã thanh toán trước khi thử QR; như vậy hóa đơn sẽ hết số dư. Test tự động dùng transaction rollback, để hóa đơn mẫu vẫn chưa thanh toán sau kiểm tra.

## Cập nhật 01/10/2026: đổi hóa đơn thử #2 thành 2.000đ

Theo yêu cầu trực tiếp của người dùng, đã kiểm tra hóa đơn #2 chưa có thanh toán hoặc phiên QR, rồi cập nhật local trong transaction có kiểm tra điều kiện: tổng 350.000đ, giảm giá 348.000đ, thành tiền 2.000đ, trạng thái CHUA_THANH_TOAN. Không đổi hóa đơn #1. Script tạo mẫu 1.000đ trước đó là lịch sử, không chạy lại cho fixture hiện tại. Chưa có giao dịch thật được xác minh.


### Xác minh thanh toán Live 01/10/2026 08:11

Hóa đơn local #2 đã thanh toán đủ 2.000đ qua webhook Live. Nguyên nhân chậm cập nhật là tunnel ngrok không chạy; sau khi khôi phục, nhận POST thành công, receipt APPLIED, phiên SUCCEEDED và hóa đơn DA_THANH_TOAN. Có một ThanhToan thành công, hóa đơn vẫn tồn tại. Chưa quan sát màn hình mobile sau callback; người dùng cần tải lại. Đây là kiểm chứng giao dịch đơn lẻ, chưa xác minh toàn bộ các trường hợp lỗi trên provider.

## Cập nhật 01/10/2026: hóa đơn thử lại #3

Người dùng yêu cầu tạo lại hóa đơn để test và hướng dẫn ngrok. Giữ nguyên hóa đơn #2 đã nhận 2.000đ thật; tạo fixture mới #3, phiếu #3003, khách #1001, tổng 350.000đ, giảm 348.000đ, còn 2.000đ, CHUA_THANH_TOAN. Script additive/idempotent ngoài repo: 2026-10-01_create-2000vnd-retest-invoice.sql. Áp SQL local thành công. Ngrok inspect xác nhận tunnel hiện hoạt động đến localhost:8080, cùng domain đã dùng ở lần thanh toán trước. Người dùng có thể thử #3 ngay, không chạy ngrok thứ hai. Những lần sau tự chạy `ngrok http 8080` trong terminal riêng và giữ mở cùng backend. Không sửa source/schema, không chuyển tiền, không commit/push.

## Tạo thêm hóa đơn 2.000đ cho thử MB trên điện thoại — 01/10/2026

Theo yêu cầu trực tiếp của người dùng, đã tạo hóa đơn **#1002**, khách #1001, phiếu sửa chữa #4002, tổng dịch vụ 350.000đ, giảm giá fixture 348.000đ, thuế 0, còn phải trả **2.000đ**, CHUA_THANH_TOAN. Đây là dữ liệu thử local, không phải dịch vụ thực tế. Đã kiểm tra trước: #2 và #3 đều đã thanh toán 2.000đ; giữ nguyên toàn bộ hóa đơn cũ. Tạo thêm tiếp nhận/phiếu/dòng dịch vụ và hóa đơn trong transaction với guard ownership, nhân viên và dịch vụ nguồn. Không sửa schema/source, không chuyển tiền, không tạo payment/QR session.

Script additive/idempotent: D:/KLCN/Garage_Management_Work_Sessions/2026-10-01_create-2000vnd-mobile-mb-invoice.sql. Kết quả INSERT trả ID #1002; SELECT sau tạo xác nhận còn chưa thanh toán và không có bản ghi ThanhToan. Chưa xác minh mobile đã tải lại, chưa xác minh webhook/ngrok hiện tại. Không commit/push/deploy.