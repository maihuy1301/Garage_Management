---
name: work-session-docs
description: "Ghi lại phiên làm việc thành tài liệu Markdown có cấu trúc. Use when the user invokes $work-session-docs, asks to document a work session, create a development log, summarize completed changes, or update project documentation from verified work."
---

# Work Session Documents

Ghi lại công việc thực tế của phiên hiện tại thành tài liệu Markdown dễ tìm kiếm, kiểm tra và tiếp tục trong phiên sau.

Chỉ ghi thông tin có bằng chứng từ yêu cầu của người dùng, file đã đọc/tạo/sửa/xóa, `git diff`, `git status`, lịch sử Git liên quan, lệnh kiểm tra đã thực sự chạy, kết quả deploy/upload đã xác minh, hoặc quyết định kỹ thuật đã được người dùng xác nhận. Không suy đoán và không ghi một hành động là đã hoàn thành khi chưa thực hiện.

## Khi bắt đầu

1. Xác định workspace và phạm vi tính năng đang làm.
2. Tìm thư mục tài liệu của dự án nếu đã có.
3. Với đồ án Garage Management, lưu tài liệu trong `D:\Khóa Luận Cử Nhân\Garage_Management_Work_Sessions\` để tài liệu phiên nằm ngoài source code. Với dự án khác, nếu chưa có quy định riêng, lưu trong thư mục tài liệu phiên nằm ngoài source code nhưng cùng khu vực workspace.
4. Chỉ đọc tài liệu phiên cũ khi cần hiểu công việc đang tiếp nối.
5. Không tạo tài liệu nếu người dùng yêu cầu không ghi lại phiên.

## Khi ghi tài liệu

Dùng mẫu tại [references/document-template.md](references/document-template.md).

Tên file mặc định là `YYYY-MM-DD_HH-mm_tieu-de-ngan.md`. Tiêu đề kỹ thuật trong tên file dùng chữ thường không dấu và dấu gạch ngang. Nội dung tài liệu dùng tiếng Việt có dấu, UTF-8 không BOM.

Tài liệu phải ghi:

- Thời gian và mục tiêu phiên.
- Phạm vi đã xử lý.
- Các quyết định quan trọng.
- File thực sự đã thay đổi.
- Nội dung thay đổi theo từng file hoặc nhóm chức năng.
- Test hoặc kiểm tra đã chạy và kết quả.
- Phần chưa kiểm tra hoặc chưa hoàn thành.
- Rủi ro và việc nên làm tiếp theo.
- Commit, deploy hoặc upload nếu thực sự có.

Nếu phiên tiếp tục cùng một mục tiêu và đã có tài liệu tương ứng, ưu tiên cập nhật tài liệu đó thay vì tạo tài liệu trùng lặp.

## Bảo mật

Không ghi vào tài liệu mật khẩu, mã PIN, OTP, access token, refresh token, API key, cookie, session, chuỗi xác thực, private key, certificate, keystore, chuỗi kết nối chứa thông tin đăng nhập, nội dung file `.env`, hoặc cấu hình hosting có bí mật.

Khi phát hiện dữ liệu nhạy cảm, chỉ ghi tên file hoặc loại bí mật bị ảnh hưởng; không chép lại giá trị.

## Giới hạn

Việc tạo tài liệu không cho phép tự động sửa thêm source ngoài phạm vi, commit, push, deploy, upload, xóa file, hoặc chạy build tốn thời gian. Chỉ thực hiện các hành động đó khi người dùng đã yêu cầu.

## Hoàn tất

Trả lời ngắn gọn:

- Đường dẫn tài liệu đã tạo hoặc cập nhật.
- Những nội dung chính đã ghi.
- Phần nào chưa có đủ bằng chứng để ghi nhận.
