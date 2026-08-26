# DOCKER DEVELOPMENT ENVIRONMENT — AUTO-GARAGE

Tài liệu hướng dẫn triển khai và vận hành môi trường phát triển AUTO-GARAGE bằng Docker Compose.

---

## 1. Yêu cầu hệ thống (Prerequisites)

- **Docker Desktop** (version 24.0+ trở lên) hỗ trợ Docker Compose V2.
- RAM: Tối thiểu 4GB (Khuyến nghị 8GB+ để chạy mượt mà SQL Server, Spring Boot, và React).
- Disk: Tối thiểu 10GB dung lượng trống.

---

## 2. Kiến trúc Docker (Docker Architecture)

```
                            Docker Compose
                            (garage-network)
                                   │
         ┌─────────────────────────┼─────────────────────────┐
         ▼                         ▼                         ▼
  garage-frontend           garage-backend           garage-sqlserver
  (Node 20 / Vite)          (Java 17 / Spring Boot)  (MS SQL Server 2022)
  Port: 3001                Port: 8080               Port: 1433
  Host: 0.0.0.0             Host: 0.0.0.0            Volume: garage-sqlserver-data
         │                         │                         ▲
         │                         └─────────────────────────┘
         └───────────────────────────────────────────────────┘
```

### Các dịch vụ trong Docker Compose:

| Tên Service | Container Name | Công nghệ | Cổng Host | Cổng Container | Mô tả |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`sqlserver`** | `garage-sqlserver` | Microsoft SQL Server 2022 | `1433` | `1433` | Database chính. Khởi tạo schema và seed data tự động nếu DB chưa tồn tại. |
| **`backend`** | `garage-backend` | Java 17 / Spring Boot 3.2.5 | `8080` | `8080` | REST API, JWT Authentication, WebSocket STOMP. |
| **`frontend`** | `garage-frontend` | Node 20 / React 18 / Vite 5 | `3001` | `3001` | Giao diện Single Page Application (SPA). |

---

## 3. Khởi động nhanh (Quick Start)

### Bước 1: Clone repository
```bash
git clone <repository-url>
cd AUTO-GARAGE
```

### Bước 2: Chuẩn bị file môi trường
Tạo file `.env` từ `.env.example` (có thể giữ nguyên giá trị mặc định cho local development):
```bash
cp .env.example .env
```

### Bước 3: Build và khởi động các container
```bash
docker compose up --build
```
*(Để chạy dưới nền, thêm cờ `-d`: `docker compose up -d --build`)*

### Bước 4: Truy cập ứng dụng
- **Frontend**: [http://localhost:3001](http://localhost:3001)
- **Backend API**: [http://localhost:8080](http://localhost:8080)
- **API Health / Base**: [http://localhost:8080/api](http://localhost:8080/api)
- **WebSocket STOMP**: `ws://localhost:8080/ws` (hoặc `http://localhost:8080/ws`)
- **SQL Server**: `localhost:1433` (User: `sa`, Password: `YourPassword123`)

---

## 4. Tài khoản kiểm thử mặc định (Default Test Accounts)

Tất cả tài khoản trong seed data có mật khẩu: `Password123@`

| Vai trò (Role) | Username | Password | Chi nhánh |
| :--- | :--- | :--- | :--- |
| **System Admin** | `admin` | `Password123@` | Toàn hệ thống (Global) |
| **Branch Manager (CN001)** | `manager` | `Password123@` | CN001 - Central Chi Nhánh 1 |
| **Branch Manager (CN002)** | `manager2` | `Password123@` | CN002 - Chi Nhánh 2 Bình Thạnh |
| **Receptionist / Front Desk** | `receptionist` | `Password123@` | CN001 - Central Chi Nhánh 1 |
| **Technician** | `technician` | `Password123@` | CN001 - Central Chi Nhánh 1 |
| **Customer** | `customer` | `Password123@` | Khách hàng |

---

## 5. Các lệnh quản lý thông dụng (Useful Commands)

### Khởi động và dừng
```bash
# Khởi động toàn bộ dịch vụ dưới nền
docker compose up -d

# Dừng tất cả container (dữ liệu DB được bảo toàn trong volume)
docker compose down

# Dừng và xóa volume (reset toàn bộ database về ban đầu)
docker compose down -v
```

### Xem logs
```bash
# Xem log toàn bộ hệ thống (realtime)
docker compose logs -f

# Xem riêng log Backend
docker compose logs -f backend

# Xem riêng log Frontend
docker compose logs -f frontend

# Xem riêng log SQL Server
docker compose logs -f sqlserver
```

### Rebuild một service cụ thể
```bash
# Rebuild chỉ backend khi có thay đổi cấu hình
docker compose up -d --build backend

# Rebuild frontend
docker compose up -d --build frontend
```

---

## 6. Tính bền vững của Database (Database Persistence)

- Dữ liệu của SQL Server được lưu trong Docker Named Volume `garage-sqlserver-data`.
- Khi bạn chạy `docker compose down` và sau đó `docker compose up`, toàn bộ bảng biểu và dữ liệu đã phát sinh **không bị mất**.
- Script `database/entrypoint.sh` sẽ kiểm tra nếu DB `GarageManagementSystem` đã tồn tại thì sẽ bỏ qua bước nạp init schema/seed, bảo toàn 100% dữ liệu mới nhất.
- Khi muốn **làm mới hoàn toàn** (fresh start):
  ```bash
  docker compose down -v
  docker compose up -d --build
  ```

---

## 7. Xử lý sự cố (Troubleshooting)

### Lỗi 1: Port already in use (3001, 8080, hoặc 1433)
**Hiện tượng**: `Bind for 0.0.0.0:XXXX failed: port is already allocated`.
**Nguyên nhân**: Đang có ứng dụng local (như local SQL Server, Spring Boot, Vite) chiếm port.
**Cách xử lý**:
1. Đổi port trên host trong file `.env`:
   ```env
   FRONTEND_PORT=3002
   BACKEND_PORT=8081
   SQLSERVER_PORT=1434
   ```
2. Hoặc tắt process đang chiếm port trên máy host.

### Lỗi 2: Backend báo `SQL Server connection refused` lúc mới start
**Hiện tượng**: Backend fail khởi động trong vài giây đầu.
**Cơ chế**: Compose đã cấu hình `healthcheck` trên SQL Server và `depends_on: { sqlserver: { condition: service_healthy } }` để đảm bảo Backend chỉ start sau khi SQL Server đã sẵn sàng nhận kết nối TCP port 1433.

### Lỗi 3: WebSocket không kết nối được
**Hiện tượng**: Trình duyệt báo CORS hoặc WebSocket handshake error.
**Kiểm tra**:
- Backend hỗ trợ dynamic allowed origin patterns qua `CORS_ALLOWED_ORIGIN_PATTERNS` (mặc định `*` cho development) trong cả `SecurityConfig.java` và `WebSocketConfig.java`.
- STOMP connect header phải truyền đúng token `Authorization: Bearer <JWT_TOKEN>`.

---

## 8. Phát triển nhiều máy trong cùng mạng LAN (Multi-Machine LAN Development)

Hệ thống hỗ trợ phát triển và kiểm thử đồng thời từ nhiều máy tính / thiết bị di động trong cùng mạng LAN mà không cần sửa code hay hard-code IP:

### 8.1. Địa chỉ truy cập từ máy khác trong LAN:
Giả sử máy chạy Docker có địa chỉ IP LAN là `192.168.1.50` (hoặc hostname LAN):
- **Frontend SPA**: `http://192.168.1.50:3001`
- **Backend REST API**: `http://192.168.1.50:8080/api`
- **WebSocket STOMP**: `ws://192.168.1.50:8080/ws`
- **SQL Server**: Tiếp tục chạy cô lập trong mạng nội bộ Docker (`garage-sqlserver:1433`) phục vụ Backend. Các máy client trong LAN **không cần cài đặt SQL Server**.

### 8.2. Cơ chế Dynamic Host Resolution:
- Frontend (`env.ts`) tự động trích xuất `window.location.hostname` tại thời điểm client mở web, tự động định tuyến API và WebSocket đến đúng máy chủ Docker Host (`http://<HOST_IP>:8080/api` và `ws://<HOST_IP>:8080/ws`).
- Khi chạy trên chính máy host, frontend tự động kết nối qua `localhost` mà không bị xung đột.
- Backend cấu hình CORS và WebSocket cho phép kết nối linh hoạt theo mẫu `CORS_ALLOWED_ORIGIN_PATTERNS=*` (hoặc tùy biến qua `.env`).

