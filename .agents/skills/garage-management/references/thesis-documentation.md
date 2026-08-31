# Tài liệu khóa luận từ quá trình phát triển

Dùng tài liệu này khi người dùng code, sửa, hoàn thiện hoặc kiểm thử một chức năng/module mới trong đồ án Garage Management. Mục tiêu là lưu lại kiến thức hệ thống có thể dùng để tổng hợp báo cáo khóa luận tốt nghiệp sau này.

## 1. Khi nào phải tạo hoặc cập nhật

- Có chức năng mới hoặc module mới được triển khai ở backend, frontend, mobile, database hoặc hạ tầng.
- Có thay đổi đáng kể về luồng nghiệp vụ, API contract, phân quyền, trạng thái nghiệp vụ, schema/entity, UI flow hoặc kiểm thử.
- Có quyết định kỹ thuật quan trọng được xác nhận hoặc thể hiện rõ trong source.
- Có sửa lỗi nghiệp vụ đáng kể làm rõ ràng hơn cách hệ thống vận hành.

Không cần tạo tài liệu khóa luận riêng cho thay đổi rất nhỏ như sửa typo, đổi text UI nhỏ, format, hoặc điều chỉnh không ảnh hưởng kiến trúc/nghiệp vụ. Vẫn có thể ghi trong tài liệu phiên nếu skill yêu cầu.

## 2. Vị trí lưu

- Mặc định lưu ngoài source code tại `D:\KLCN\Garage_Management_Work_Sessions\thesis-notes\`.
- Nếu dự án đang dùng thư mục phiên `D:\Khóa Luận Cử Nhân\Garage_Management_Work_Sessions\`, dùng `D:\Khóa Luận Cử Nhân\Garage_Management_Work_Sessions\thesis-notes\`.
- Tạo thư mục nếu chưa tồn tại.
- Không lưu trong repository `Garage_Management` nếu người dùng chưa yêu cầu rõ.

## 3. Quy tắc đặt tên

Dùng tên file Markdown dễ gom báo cáo:

`YYYY-MM-DD_<module-or-task>_<ten-chuc-nang>.md`

Ví dụ:

- `2026-08-26_frontend-task-07_quan-ly-lich-hen.md`
- `2026-08-26_backend-task-15_hoa-don-thanh-toan.md`
- `2026-08-26_infrastructure_docker-development.md`

Nếu tiếp tục cùng một chức năng, ưu tiên cập nhật file đã có thay vì tạo nhiều file trùng.

## 4. Cấu trúc nội dung bắt buộc

```markdown
# Ghi chú khóa luận: {{Tên chức năng/module}}

- Ngày cập nhật: {{YYYY-MM-DD HH:mm}}
- Phạm vi: {{Backend | Frontend | Mobile | Database | Infrastructure | Full-stack}}
- Trạng thái: {{Đang làm | Hoàn thành | Cần bổ sung}}
- Nguồn bằng chứng: {{file source, tài liệu, lệnh test/build đã đọc/chạy}}

## 1. Mục tiêu chức năng

{{Chức năng giải quyết nhu cầu gì trong nghiệp vụ garage.}}

## 2. Vai trò sử dụng

{{Các actor/role liên quan và quyền chính của từng role.}}

## 3. Luồng nghiệp vụ

{{Các bước nghiệp vụ theo thứ tự thực tế. Bám `business-workflow.md` khi có liên quan.}}

## 4. Thiết kế kỹ thuật

{{Tóm tắt kiến trúc và các lớp/file chính. Với backend nêu Controller -> Service -> Repository -> Entity/DTO. Với frontend nêu Page -> Component -> Service -> API client/types.}}

## 5. API và dữ liệu

{{Endpoint, request/response chính, entity/bảng dữ liệu liên quan, trạng thái nghiệp vụ. Không ghi secret hoặc dữ liệu nhạy cảm.}}

## 6. Phân quyền và bảo mật

{{RBAC, branch isolation, ownership, JWT/security checks, các trường hợp 401/403 quan trọng.}}

## 7. Giao diện và trải nghiệm

{{Các màn hình, thao tác, trạng thái loading/error/empty, filter/search/modal nếu có.}}

## 8. Kiểm thử và xác minh

{{Test/build/lint/analyze đã chạy, kết quả, test case nghiệp vụ quan trọng.}}

## 9. Hạn chế và hướng phát triển

{{Phần chưa làm, khác biệt so với đề cương khảo sát, backlog hoặc rủi ro.}}

## 10. Gợi ý đưa vào báo cáo

{{Các đoạn/ý có thể tái sử dụng cho chương phân tích, thiết kế, cài đặt hoặc kiểm thử.}}
```

## 5. Nguyên tắc ghi

- Chỉ ghi điều đã có bằng chứng: source đã đọc/sửa, `git diff`, tài liệu dự án, đề cương nghiệp vụ, lệnh test/build đã chạy hoặc quyết định người dùng xác nhận.
- Phân biệt rõ “đã triển khai” với “đề cương mong muốn/backlog”.
- Viết bằng tiếng Việt rõ ràng, phù hợp để sau này đưa vào báo cáo.
- Không ghi mật khẩu, token, API key, cookie/session, private key, certificate, keystore, chuỗi kết nối chứa credentials, nội dung `.env`, hoặc dữ liệu thật nhạy cảm.
- Không tự commit, push, deploy hoặc sửa source chỉ để tạo tài liệu.

## 6. Cách dùng trong phiên làm việc

1. Khi bắt đầu một chức năng, kiểm tra có tài liệu khóa luận tương ứng chưa.
2. Khi hoàn tất code hoặc một phần đáng kể, cập nhật tài liệu bằng bằng chứng thực tế.
3. Nếu chưa chạy test/build, ghi rõ là chưa chạy.
4. Trong final response, báo đường dẫn tài liệu khóa luận đã tạo/cập nhật cùng với tài liệu phiên nếu có.
