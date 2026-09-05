---
description: Tự động nạp ngữ cảnh dự án Garage Management và quy tắc an toàn cốt lõi
trigger: always_on
---

# Tự Động Nạp Ngữ Cảnh Dự Án Garage Management

Bạn đang làm việc trong dự án **Garage Management** (Hệ thống quản lý garage ô tô đa chi nhánh).

## 1. Nguồn Tài Liệu Chuẩn Cần Đọc
Khi bắt đầu phiên hoặc giải quyết các task liên quan đến dự án, luôn đối chiếu các tài liệu:
- `README.md` & `docs/PROJECT_CONTEXT.md` (Hiện trạng backend, frontend, mobile, DB)
- `d:\KLCN\Garage_Management_Agent_Pack\PROJECT_STATUS_SNAPSHOT.md` (Snapshot tiến độ)
- `d:\KLCN\Garage_Management_Agent_Pack\DO_NOT_BREAK_CHECKLIST.md` (Checklist an toàn)
- `d:\KLCN\Garage_Management_Agent_Pack\garage-management\references\business-workflow.md` (Luồng nghiệp vụ)

## 2. Bất Biến Cốt Lõi (Tuyệt đối tuân thủ)
- **Không tự ý sửa Database**: `database/GarageSystemDB.sql` là single source of truth, chỉ sửa khi người dùng duyệt rõ ràng.
- **Backend Authority**: RBAC, Branch Isolation và Ownership bắt buộc kiểm tra ở Backend (Spring Boot), không tin tưởng role/branch gửi từ client.
- **Kiến trúc chuẩn**:
  - Backend: `Controller -> Service -> Repository -> Entity/DTO` (dùng `ApiResponse`).
  - Frontend: `src/features/<domain>`, gọi API qua service tập trung (`apiClient`, `API_ENDPOINTS`), bảo vệ route qua `ProtectedRoute` / `RoleGuard`.
- **An toàn Git & Hệ thống**: Không tự ý `git reset --hard`, không xóa file, không xóa Docker volume, không tự động deploy/push nếu chưa có yêu cầu.

## 3. Tự Động Cập Nhật Tiến Độ
- Khi hoàn thành hoặc thay đổi tính năng mới, chủ động đối chiếu và cập nhật `PROJECT_STATUS_SNAPSHOT.md` trong Agent Pack.
- Ghi chép tài liệu phiên làm việc vào `d:\KLCN\Garage_Management_Work_Sessions\` khi kết thúc các task quan trọng.
