---
name: garage-management
description: Hỗ trợ phân tích, sửa, kiểm tra, build, duy trì và ghi tài liệu phiên làm việc cho đồ án Garage Management trong đúng monorepo Spring Boot, React, Flutter và SQL Server. Dùng khi yêu cầu nhắc đến đồ án Garage hoặc workspace có backend/pom.xml, frontend/package.json, mobile/pubspec.yaml và database/GarageSystemDB.sql; không dùng cho dự án garage khác hay tác vụ lập trình chung ngoài repository này.
---

# Garage Management

Làm việc trong repository `Garage_Management` và trả lời bằng tiếng Việt. Trước khi hành động, xác nhận đúng workspace bằng các dấu hiệu trong mô tả; nếu không đủ dấu hiệu thì không áp dụng skill.

## Chuẩn bị ngữ cảnh

1. Đọc `README.md` và `docs/PROJECT_CONTEXT.md` trước khi sửa hoặc đánh giá source.
2. Đọc [references/project-profile.md](references/project-profile.md) để lấy phạm vi, quy ước, quyền hạn và ma trận kiểm tra.
3. Khi phân tích, thiết kế hoặc triển khai luồng nghiệp vụ, đọc [references/business-workflow.md](references/business-workflow.md) để bám đề cương khảo sát thực tế.
4. Chỉ đọc `docs/DOCKER.md` khi yêu cầu liên quan Docker, môi trường chạy hoặc deploy. Chỉ đọc `AUTO-GARAGE-UI-SUGGESTION/autocare_multi_branch/DESIGN.md` khi xử lý UI/UX.
5. Không dùng `ProgressReport.txt` làm tài liệu bắt buộc; chỉ mở khi người dùng yêu cầu đối chiếu lịch sử tiến độ.
6. Kiểm tra `git status --short` trước khi sửa và giữ nguyên mọi thay đổi không thuộc yêu cầu.

## Bất biến quan trọng

- `database/GarageSystemDB.sql` là nguồn chuẩn của schema và không được sửa nếu người dùng chưa phê duyệt rõ.
- Backend là authority cuối cùng cho role, branch và ownership. Không tin các giá trị quyền hạn do client gửi; giữ RBAC, branch isolation và ownership checks ở backend.
- Giữ luồng backend `Controller -> Service -> Repository -> Entity`, dùng DTO và `ApiResponse`; không trả entity trực tiếp qua API.
- Giữ frontend theo feature trong `frontend/src/features`, API tập trung qua `apiClient`/`API_ENDPOINTS`, route qua `ProtectedRoute` và `RoleGuard`.
- `AUTO-GARAGE-UI-SUGGESTION/` là tài liệu/mockup tham khảo, không phải source chạy chính. Không sửa file sinh tự động hay toolchain nếu không cần cho yêu cầu.
- Không triển khai thêm business feature ngoài phạm vi được giao.

## Thực hiện và kiểm tra

- Truy vết tác động xuyên lớp trước khi sửa: schema/entity/repository/service/controller/DTO/test và types/service/page/router ở frontend khi có liên quan.
- Ưu tiên thay đổi nhỏ, phù hợp pattern hiện hữu. Giữ tương thích API, trạng thái nghiệp vụ và ma trận phân quyền trừ khi yêu cầu nói rõ khác đi.
- Thêm hoặc cập nhật test gần hành vi bị thay đổi. Chạy kiểm tra phù hợp theo ma trận trong project profile; được phép chạy lint, test và build cục bộ không phá hủy dữ liệu.
- Không tự khởi động Docker stack, reset volume, sửa dữ liệu thật, deploy, commit, push, xóa file hay thay đổi production config nếu người dùng chưa yêu cầu rõ.
- Chỉ cập nhật `README.md` và `docs/PROJECT_CONTEXT.md` khi thay đổi thực sự ảnh hưởng hành vi, kiến trúc, API, cách chạy hoặc trạng thái tính năng.
- Sau khi hoàn tất, báo ngắn gọn: kết quả, file đã sửa, kiểm tra đã chạy, phần chưa kiểm tra và rủi ro/cách dùng nếu có.

## Tài liệu phiên làm việc

- Khi dùng `$garage-management` cho công việc đồ án có thay đổi code, tài liệu, cấu hình, test/build, quyết định kỹ thuật, hoặc kiểm tra đáng ghi nhận, tự tạo hoặc cập nhật tài liệu phiên theo quy trình của `work-session-docs`.
- Khi code, sửa hoặc hoàn thiện một chức năng/module mới, đồng thời tạo hoặc cập nhật tài liệu khóa luận theo [references/thesis-documentation.md](references/thesis-documentation.md) để lưu kiến thức hệ thống phục vụ báo cáo tốt nghiệp.
- Đọc `.agents/skills/work-session-docs/SKILL.md` và dùng mẫu `.agents/skills/work-session-docs/references/document-template.md` trước khi ghi tài liệu phiên.
- Lưu tài liệu phiên trong `D:\Khóa Luận Cử Nhân\Garage_Management_Work_Sessions\`, không lưu trong source code của repository. Nếu phiên tiếp tục cùng một mục tiêu và đã có tài liệu tương ứng, ưu tiên cập nhật tài liệu đó thay vì tạo file trùng.
- Lưu tài liệu khóa luận trong `D:\KLCN\Garage_Management_Work_Sessions\thesis-notes\` nếu thư mục này tồn tại hoặc có thể tạo; nếu phiên đang dùng thư mục `D:\Khóa Luận Cử Nhân\Garage_Management_Work_Sessions\` thì dùng cùng cấu trúc `thesis-notes\` ở đó. Không lưu tài liệu khóa luận trong source code trừ khi người dùng yêu cầu rõ.
- Chỉ ghi thông tin có bằng chứng từ yêu cầu người dùng, file đã đọc/tạo/sửa/xóa, `git diff`, `git status`, lệnh kiểm tra đã chạy, kết quả deploy/upload đã xác minh, hoặc quyết định kỹ thuật đã được người dùng xác nhận.
- Không ghi bí mật vào tài liệu phiên: mật khẩu, token, API key, cookie/session, private key, certificate, keystore, chuỗi kết nối có credentials, nội dung `.env`, hoặc cấu hình hosting nhạy cảm.
- Không tạo tài liệu phiên nếu người dùng yêu cầu không ghi lại phiên. Việc ghi tài liệu không cho phép tự commit, push, deploy, upload, xóa file, sửa database, hoặc chạy build tốn thời gian ngoài phạm vi đã được yêu cầu.
