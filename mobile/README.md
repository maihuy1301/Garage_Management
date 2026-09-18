# AutoCare Garage Mobile

Flutter app dùng chung cho khách hàng và kỹ thuật viên của hệ thống Garage Management.

## Foundation hiện tại

- Splash screen toàn màn hình dùng ảnh nhận diện `asset/screen.png`, hiển thị trong lúc khôi phục session và tối thiểu 1,6 giây.
- Guest home theo AutoCare design direction, không bắt đăng nhập khi mở app.
- Đăng nhập qua backend, lưu JWT bằng `flutter_secure_storage`.
- Điều hướng theo role `ROLE_CUSTOMER` / `ROLE_TECHNICIAN`.
- Các màn cá nhân được bảo vệ và quay lại đúng màn ban đầu sau login bằng `returnTo`.
- Customer bottom navigation: Trang chủ, Đặt lịch, Theo dõi, Thông báo, Tài khoản.
- Customer có quản lý xe, đặt/hủy lịch và theo dõi trạng thái lịch hẹn.
- Technician có danh sách công việc được phân công, bộ lọc trạng thái, màn chi tiết phiếu sửa chữa, lịch sử tiến độ và cập nhật tiến độ/hạng mục dịch vụ.
- Quyền sở hữu, chi nhánh và phân công luôn do backend xác thực từ JWT; mobile không gửi mã khách hàng, kỹ thuật viên hoặc chi nhánh để tự xác lập quyền.

## Chạy ứng dụng

```powershell
flutter pub get
flutter run
```

Mặc định app gọi `http://10.0.2.2:8080/api`, phù hợp với Android emulator khi backend chạy ở máy phát triển.

Với thiết bị thật, iOS simulator hoặc desktop, truyền URL backend có thể truy cập được:

```powershell
flutter run --dart-define=API_BASE_URL=http://<backend-host>:8080/api
```

HTTP cleartext chỉ được bật trong Android debug manifest. Bản phát hành cần dùng backend HTTPS.

### Xử lý màn hình đen trên Android emulator

Với image Android 17/API 37 16 KB, emulator có thể mở app nhưng không render frame nào khi Flutter dùng Impeller/OpenGLES. Cấu hình debug của dự án đã tắt Impeller để `flutter run` tự dùng Skia; bản release vẫn giữ renderer mặc định của Flutter.

Nếu AVD vẫn đen sau khi cập nhật source:

1. Dừng phiên `flutter run` hiện tại.
2. Trong Device Manager, đặt `Emulated Performance > Graphics` thành `Software`.
3. Chọn `Cold Boot Now` cho AVD rồi chạy lại `flutter run`.

Có thể kiểm tra nhanh mà không đổi source bằng `flutter run --no-enable-impeller`.

## Ảnh hồ sơ xe

Trong màn `Xe của tôi`, khách hàng có thể chụp ảnh hoặc chọn ảnh từ thư viện khi thêm xe. Camera được xin quyền tại thời điểm sử dụng; ảnh được xem trước, giới hạn 8 MB và upload sau khi hồ sơ xe được tạo. Backend phải có migration `HinhAnhXe` và thư mục lưu ảnh có quyền ghi; xem lệnh `database/apply-vehicle-image-migration.ps1` ở thư mục gốc dự án.

## Thông báo khách hàng

Tab `/notifications` dùng `GET /notifications`, `GET /notifications/unread-count`, `PATCH /notifications/{id}/read` và `PATCH /notifications/read-all` qua API client có JWT. Số chưa đọc được lấy từ backend; sau thao tác đọc, app tải lại dữ liệu và không tự suy diễn quyền sở hữu.

Màn hình có loading, lỗi kèm thử lại, trạng thái rỗng và kéo để làm mới. Khi refresh thất bại, dữ liệu cũ vẫn được giữ và có thông báo rõ. STOMP nhận sự kiện `DAT_LICH`/`THONG_BAO` từ private queue; URL WebSocket suy ra từ `API_BASE_URL` bằng cách thay hậu tố `/api` thành `/ws`, dùng `wss` với HTTPS. Có tải lại khi reconnect/resume và polling dự phòng mỗi 30 giây; rời màn hình hoặc đưa app xuống nền sẽ dừng cập nhật.

## Kiểm tra thông báo và hồi quy

```powershell
flutter analyze
flutter test
```
