# Hướng dẫn restart backend/frontend và kiểm tra realtime lịch hẹn

Tài liệu này dành cho bạn phụ trách web khi cần nạp thay đổi realtime lịch hẹn mới.

## 1. Khi nào cần restart

- Có thay đổi Java/backend: phải restart backend.
- Có thay đổi React/Vite hoặc cấu hình frontend: restart frontend dev server.
- Có thay đổi `/api/auth/me` hoặc token/session: sau khi restart backend, nên reload web và đăng nhập lại để frontend nhận đủ `maChiNhanh`.
- Không reset database, không xóa Docker volume khi chỉ cần nạp code realtime.

## 2. Restart nếu chạy native bằng terminal

### Backend

Trong terminal đang chạy backend:

```powershell
Ctrl + C
```

Sau đó chạy lại:

```powershell
cd D:\KLCN\Garage_Management\backend
mvn spring-boot:run
```

Backend sẵn sàng khi log không còn lỗi startup và API chạy tại:

```text
http://localhost:8080/api
```

Kiểm tra nhanh:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8080/api/branches
```

Nếu trả `401 Unauthorized` là backend đã lên và endpoint đang được bảo vệ đúng.

### Frontend web

Trong terminal đang chạy frontend:

```powershell
Ctrl + C
```

Sau đó chạy lại:

```powershell
cd D:\KLCN\Garage_Management\frontend
npm run dev
```

Mở web:

```text
http://localhost:3001/
```

Sau khi web mở lại, nên hard reload trình duyệt hoặc logout/login lại.

## 3. Restart nếu chạy bằng Docker Compose

Chỉ restart service, không xóa volume:

```powershell
cd D:\KLCN\Garage_Management
docker compose restart garage-backend garage-frontend
```

Nếu image cần build lại sau khi đổi code:

```powershell
cd D:\KLCN\Garage_Management
docker compose up -d --build garage-backend garage-frontend
```

Không chạy lệnh này nếu chỉ muốn giữ dữ liệu:

```powershell
docker compose down -v
```

Lệnh `down -v` sẽ xóa volume SQL Server và có thể mất dữ liệu dev hiện tại.

## 4. Cách kiểm tra realtime lịch hẹn

1. Mở web quản lý tại `http://localhost:3001/`.
2. Đăng nhập tài khoản `admin` hoặc tài khoản quản lý/tiếp nhận đúng chi nhánh của lịch hẹn.
3. Vào `Lịch hẹn`.
4. Mở mobile/web tracking của khách hàng ở tab khác.
5. Trên web, bấm xác nhận hoặc tiếp nhận một lịch hẹn.
6. Kỳ vọng:
   - Bảng/KPI trên web tự đổi mà không cần bấm reload.
   - Modal chi tiết lịch hẹn đang mở cũng đổi trạng thái.
   - Mobile tracking/detail tự đổi trong tối đa khoảng 8 giây.

## 5. Kiểm tra WebSocket trong DevTools

Mở Chrome DevTools:

```text
F12 -> Network -> WS
```

Khi vào trang `Lịch hẹn`, phải thấy kết nối tới:

```text
ws://localhost:8080/ws
```

Trong frame WebSocket nên có:

```text
CONNECT
CONNECTED
SUBSCRIBE
MESSAGE
```

Nếu không thấy `CONNECTED`, kiểm tra token đăng nhập hoặc backend đã restart chưa.

## 6. Lưu ý phân quyền chi nhánh

- `admin` thấy lịch toàn hệ thống.
- `manager` và `frontdesk` chỉ thấy lịch thuộc chi nhánh của tài khoản đó.
- Nếu khách đặt lịch ở chi nhánh 2 nhưng web đăng nhập quản lý chi nhánh 1 thì danh sách vẫn rỗng; đây là đúng branch isolation, không phải lỗi realtime.

## 7. Các file realtime chính

- Backend phát event: `backend/src/main/java/com/garage/service/AppointmentService.java`
- WebSocket publisher: `backend/src/main/java/com/garage/websocket/WebSocketEventPublisher.java`
- WebSocket config/auth: `backend/src/main/java/com/garage/websocket/WebSocketConfig.java`, `backend/src/main/java/com/garage/websocket/WebSocketAuthChannelInterceptor.java`
- Frontend STOMP client: `frontend/src/lib/websocket/websocket-client.ts`
- Trang web lịch hẹn: `frontend/src/features/appointments/pages/AppointmentsPage.tsx`
- Mobile tracking: `mobile/lib/features/appointments/presentation/appointment_tracking_page.dart`
- Mobile detail: `mobile/lib/features/appointments/presentation/appointment_detail_page.dart`
