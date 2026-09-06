# DOCKER DEVELOPMENT ENVIRONMENT — AUTO-GARAGE (INFRASTRUCTURE ONLY)

Tài liệu hướng dẫn khởi chạy môi trường Infrastructure (SQL Server Database) cho AUTO-GARAGE bằng Docker Compose.

---

## 1. Yêu cầu hệ thống (Prerequisites)

- **Docker Desktop** (version 24.0+ trở lên) hỗ trợ Docker Compose V2.
- **Java 17** & **Maven 3.9+** (để chạy Backend cục bộ).
- **Node.js 20+** & **npm** (để chạy Frontend cục bộ).
- RAM: Tối thiểu 4GB.

---

## 2. Kiến trúc Môi trường (Environment Architecture)

```
                            Docker Compose
                           (Infrastructure)
                                   │
                                   ▼
                            garage-sqlserver
                          (MS SQL Server 2022)
                          Port: 1433:1433
                          Volume: garage-sqlserver-data
                                   ▲
                                   │ (localhost:1433)
                ┌──────────────────┴──────────────────┐
                │                                     │
         Backend Spring Boot                   Frontend React
         (Chạy trực tiếp từ IDE/Maven)         (Chạy trực tiếp từ npm/Vite)
         Port: 8080                            Port: 3001
```

### Dịch vụ trong Docker Compose:

| Tên Service | Container Name | Công nghệ | Cổng Host | Mô tả |
| :--- | :--- | :--- | :--- | :--- |
| **`sqlserver`** | `garage-sqlserver` | Microsoft SQL Server 2022 | `1433:1433` | Database chính. Tự động khởi tạo database `GarageManagementSystem` và seed data theo schema mới. |

---

## 3. Khởi động nhanh (Quick Start)

### Bước 1: Chuẩn bị file môi trường
```bash
cp .env.example .env
```

### Bước 2: Khởi động Infrastructure qua Docker Compose
```bash
docker compose up -d
```

Kiểm tra trạng thái container:
```bash
docker compose ps
```
*(Chỉ có container `garage-sqlserver` hoạt động).*

### Bước 3: Chạy Backend Spring Boot (Local)
Mở terminal tại thư mục `backend/` hoặc chạy từ IDE:
```bash
cd backend
mvn spring-boot:run
```
Backend sẽ tự động kết nối tới SQL Server tại `localhost:1433` và phục vụ API tại `http://localhost:8080`.

### Bước 4: Chạy Frontend React / Vite (Local)
Mở terminal tại thư mục `frontend/`:
```bash
cd frontend
npm install
npm run dev
```
Giao diện sẽ mở tại `http://localhost:3001` (hoặc port được chỉ định bởi Vite).

---

## 4. Tài khoản kiểm thử mặc định (Default Test Accounts)

Tất cả tài khoản trong seed data có mật khẩu: `Password123@` và mã PIN: `123456`

| Vai trò (Role) | Username | Password | Mã PIN | Chi nhánh |
| :--- | :--- | :--- | :--- | :--- |
| **System Admin** | `admin` | `Password123@` | `123456` | Toàn hệ thống |
| **Branch Manager (CN1)** | `manager` | `Password123@` | `123456` | Chi Nhánh 1 |
| **Branch Manager (CN2)** | `manager2` | `Password123@` | `123456` | Chi Nhánh 2 |
| **Receptionist** | `receptionist` | `Password123@` | `123456` | Chi Nhánh 1 |
| **Technician (CN1)** | `technician` | `Password123@` | `123456` | Chi Nhánh 1 |
| **Technician (CN2)** | `technician2` | `Password123@` | `123456` | Chi Nhánh 2 |
| **Customer** | `customer` | `Password123@` | `123456` | Khách hàng |

---

## 5. Các lệnh quản trị Docker thông dụng

```bash
# Khởi động infrastructure dưới nền
docker compose up -d

# Dừng infrastructure (dữ liệu được bảo toàn trong volume garage-sqlserver-data)
docker compose down

# Dừng và reset database hoàn toàn về trạng thái ban đầu
docker compose down -v
docker compose up -d --build

# Xem logs SQL Server
docker compose logs -f sqlserver
```
