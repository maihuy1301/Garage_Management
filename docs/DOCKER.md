# DOCKER INFRASTRUCTURE ENVIRONMENT — AUTO-GARAGE

Tài liệu hướng dẫn triển khai và vận hành tầng hạ tầng (Infrastructure - SQL Server 2022) của dự án AUTO-GARAGE bằng Docker Compose.

---

## 1. Kiến trúc hệ thống phát triển (Development Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                        Host Machine                         │
│                                                             │
│   ┌─────────────────────┐         ┌─────────────────────┐   │
│   │ React 18 + Vite 5   │         │ Spring Boot (Java)  │   │
│   │ npm run dev         │ ──────> │ mvn spring-boot:run │   │
│   │ Port: 3001          │         │ Port: 8080          │   │
│   └─────────────────────┘         └──────────┬──────────┘   │
│                                              │              │
│                                              ▼ (Port 1433)  │
│                   ┌──────────────────────────────┐          │
│                   │ Docker Compose               │          │
│                   │                              │          │
│                   │ garage-sqlserver             │          │
│                   │ (SQL Server 2022)            │          │
│                   │ Volume: garage-sqlserver-data│          │
│                   └──────────────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### Dịch vụ trong Docker Compose:

| Tên Service | Container Name | Công nghệ | Cổng Host | Cổng Container | Mô tả |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`sqlserver`** | `garage-sqlserver` | Microsoft SQL Server 2022 | `1433` | `1433` | Database chính. Khởi tạo schema và seed data tự động nếu DB chưa tồn tại. Lưu dữ liệu persistent trên volume `garage-sqlserver-data`. |

* **Backend** và **Frontend** chạy trực tiếp trên máy host để tối ưu hiệu năng phát triển và hot reload.

---

## 2. Quy trình khởi chạy 3 bước (3-Terminal Workflow)

### Bước 1 (Terminal 1) — Khởi động Database qua Docker Compose:
```bash
# Tạo file .env từ .env.example (nếu chưa có)
cp .env.example .env

# Khởi động SQL Server 2022 dưới nền
docker compose up -d --build
```
> Kiểm tra trạng thái: `docker compose ps` (chỉ có container `garage-sqlserver` chạy).

### Bước 2 (Terminal 2) — Khởi động Backend (Spring Boot):
```bash
cd backend
mvn spring-boot:run
```
> Backend chạy tại: `http://localhost:8080` (API base: `http://localhost:8080/api`).

### Bước 3 (Terminal 3) — Khởi động Frontend (React / Vite):
```bash
cd frontend
npm run dev
```
> Frontend chạy tại: `http://localhost:3001`.

---

## 3. Tài khoản kiểm thử mặc định (Default Test Accounts)

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

## 4. Các lệnh quản lý Docker thông dụng (Useful Commands)

```bash
# Khởi động SQL Server dưới nền
docker compose up -d

# Xem log SQL Server (realtime)
docker compose logs -f sqlserver

# Dừng SQL Server (dữ liệu DB được bảo toàn trong volume)
docker compose down

# Dừng và xóa volume (reset toàn bộ database về ban đầu khi cần)
docker compose down -v
```

---

## 5. Tính bền vững của Database (Database Persistence)

- Dữ liệu của SQL Server được lưu trong Docker Named Volume `garage-sqlserver-data`.
- Khi bạn chạy `docker compose down` và sau đó `docker compose up`, toàn bộ bảng biểu và dữ liệu đã phát sinh **không bị mất**.
- Script `database/entrypoint.sh` sẽ kiểm tra nếu DB `GarageManagementSystem` đã tồn tại thì sẽ bỏ qua bước nạp init schema/seed, bảo toàn 100% dữ liệu mới nhất.
- Khi muốn **làm mới hoàn toàn** (fresh start):
  ```bash
  docker compose down -v
  docker compose up -d --build
  ```

---

## 6. Xử lý sự cố (Troubleshooting)

### Lỗi 1: Port already in use (1433)
**Hiện tượng**: `Bind for 0.0.0.0:1433 failed: port is already allocated`.
**Nguyên nhân**: Đang có SQL Server cục bộ chạy trên máy host.
**Cách xử lý**:
1. Đổi port trên host trong file `.env`: `SQLSERVER_PORT=1434`
2. Hoặc tắt service SQL Server cục bộ trên Windows Services (`services.msc`).

### Lỗi 2: Backend báo connection refused
**Hiện tượng**: Backend fail khi kết nối tới SQL Server.
**Cách xử lý**: Đảm bảo container SQL Server đã ở trạng thái healthy (`docker compose ps`).


