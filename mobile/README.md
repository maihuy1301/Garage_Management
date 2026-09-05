# AutoCare Garage Mobile

Flutter app dùng chung cho khách hàng và kỹ thuật viên của hệ thống Garage Management.

## Foundation hiện tại

- Splash screen toàn màn hình dùng ảnh nhận diện `asset/screen.png`, hiển thị trong lúc khôi phục session và tối thiểu 1,6 giây.
- Guest home theo AutoCare design direction, không bắt đăng nhập khi mở app.
- Đăng nhập qua backend, lưu JWT bằng `flutter_secure_storage`.
- Điều hướng theo role `ROLE_CUSTOMER` / `ROLE_TECHNICIAN`.
- Các màn cá nhân được bảo vệ và quay lại đúng màn ban đầu sau login bằng `returnTo`.
- Customer bottom navigation: Trang chủ, Đặt lịch, Theo dõi, Thông báo, Tài khoản.

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

## Kiểm tra

```powershell
flutter analyze
flutter test
```
