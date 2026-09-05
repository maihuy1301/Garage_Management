---
name: sync-project
description: >-
  Đọc lại toàn bộ ngữ cảnh dự án Garage Management, kiểm tra trạng thái thực tế của
  repository, so sánh với snapshot cũ, cập nhật PROJECT_STATUS_SNAPSHOT.md trong Agent Pack,
  và báo cáo tóm tắt trạng thái dự án. Kích hoạt khi người dùng gọi $sync-project.
---

# Sync Project Context

Đọc lại toàn bộ ngữ cảnh dự án Garage Management và cập nhật snapshot tiến độ.

## Mục tiêu

Khi người dùng gọi `$sync-project`, AI sẽ:

1. Đọc lại tất cả tài liệu chuẩn của dự án.
2. Kiểm tra trạng thái thực tế từ repository và file source.
3. So sánh với snapshot tiến độ cũ.
4. Cập nhật `PROJECT_STATUS_SNAPSHOT.md` trong Agent Pack nếu có thay đổi.
5. Báo cáo tóm tắt rõ ràng.

## Bước 1 — Đọc tài liệu chuẩn theo thứ tự

Đọc theo thứ tự sau, **không bỏ qua bước nào**:

1. `d:\KLCN\Garage_Management\README.md`
2. `d:\KLCN\Garage_Management\docs\PROJECT_CONTEXT.md`
3. `d:\KLCN\Garage_Management_Agent_Pack\garage-management\SKILL.md`
4. `d:\KLCN\Garage_Management_Agent_Pack\garage-management\references\project-profile.md`
5. `d:\KLCN\Garage_Management_Agent_Pack\garage-management\references\business-workflow.md`
6. `d:\KLCN\Garage_Management_Agent_Pack\PROJECT_STATUS_SNAPSHOT.md` (snapshot cũ để so sánh)
7. `d:\KLCN\Garage_Management_Agent_Pack\DO_NOT_BREAK_CHECKLIST.md`

## Bước 2 — Kiểm tra trạng thái thực tế

Chạy các lệnh sau để lấy thông tin hiện tại từ repository:

```
cd d:\KLCN\Garage_Management
git status --short
git log --oneline -10
```

Ngoài ra, kiểm tra nhanh cấu trúc các thư mục quan trọng:

- `d:\KLCN\Garage_Management\backend\src\main\java\` — xem package/module backend có gì
- `d:\KLCN\Garage_Management\frontend\src\features\` — xem feature frontend đã có
- `d:\KLCN\Garage_Management\mobile\lib\` — xem mobile đang ở giai đoạn nào
- `d:\KLCN\Garage_Management_Work_Sessions\` — xem phiên làm việc gần nhất

## Bước 3 — So sánh và phát hiện thay đổi

Đối chiếu thực tế vừa kiểm tra với `PROJECT_STATUS_SNAPSHOT.md` cũ:

- Backend: có module nào mới xuất hiện hoặc đã hoàn thành thêm không?
- Frontend: feature nào đã có trong `frontend/src/features/` mà snapshot chưa ghi nhận?
- Mobile: `mobile/lib/` có nhiều hơn foundation không?
- Work Sessions: phiên gần nhất ghi lại gì, có task nào done mà snapshot chưa cập nhật?
- Có git commit mới nào liên quan module chưa được snapshot ghi nhận không?

## Bước 4 — Cập nhật PROJECT_STATUS_SNAPSHOT.md

Nếu phát hiện **bất kỳ sự khác biệt** nào giữa thực tế và snapshot cũ:

1. Cập nhật file `d:\KLCN\Garage_Management_Agent_Pack\PROJECT_STATUS_SNAPSHOT.md`.
2. Giữ nguyên cấu trúc section hiện tại của snapshot.
3. Chỉ thêm/sửa phần thực sự thay đổi — không xóa thông tin còn đúng.
4. Ghi rõ ngày cập nhật ở đầu snapshot theo format `<!-- Updated: YYYY-MM-DD -->`.
5. Không ghi thông tin suy đoán — chỉ ghi những gì có bằng chứng từ source, git log, hoặc work session docs.

Nếu snapshot đã khớp thực tế, không cập nhật, chỉ ghi nhận "snapshot đã đồng bộ".

## Bước 5 — Báo cáo tóm tắt

Sau khi hoàn tất, báo cáo ngắn gọn theo mẫu:

```
## 📊 Trạng thái dự án (sync lúc HH:mm DD/MM/YYYY)

**Backend:** [X module đã hoàn thành / ghi nhận thay đổi nếu có]
**Frontend:** [Features đã có / còn thiếu]
**Mobile:** [Giai đoạn hiện tại]
**Snapshot:** [Đã cập nhật / Đã đồng bộ, không cần cập nhật]

**Thay đổi phát hiện:** [Liệt kê hoặc "Không có thay đổi mới"]
**Task ưu tiên tiếp theo:** [Dựa trên PROJECT_STATUS_SNAPSHOT.md]
**Rủi ro cần chú ý:** [Nếu có]
```

## Giới hạn

- Không sửa source code trong quá trình sync.
- Không commit, push, deploy, reset Docker hay xóa file.
- Không sửa `database/GarageSystemDB.sql`.
- Chỉ cập nhật `PROJECT_STATUS_SNAPSHOT.md` trong Agent Pack — không sửa file khác trong Agent Pack trừ khi được yêu cầu rõ.
- Nếu không có quyền đọc một file, bỏ qua và ghi nhận trong báo cáo.
