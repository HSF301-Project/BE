# Docker Setup Guide for BusTicket Project

## Yêu cầu
- Docker Desktop cài đặt trên Mac
- Docker Compose (đã bao gồm trong Docker Desktop)

## Các bước chạy SQL Server trên Docker

### 1. Khởi động SQL Server Container

Chạy lệnh sau từ thư mục root của project:

```bash
docker-compose up -d
```

### 2. Kiểm tra container đang chạy

```bash
docker ps
```

Bạn sẽ thấy container `busticket-mssql` đang chạy.

### 3. Kết nối tới SQL Server

**Thông tin kết nối:**
- Host: `localhost`
- Port: `1433`
- Username: `sa`
- Password: `Admin@123456`
- Database: `BusTicket`

### 4. Dừng SQL Server Container

```bash
docker-compose down
```

### 5. Dừng và xóa dữ liệu (reset database)

```bash
docker-compose down -v
```

## Công cụ để kết nối tới SQL Server

### Option 1: Dùng Azure Data Studio (Recommended)
- Tải tại: https://aka.ms/sqltoolsservice
- Host: `localhost`
- Port: `1433`
- Username: `sa`
- Password: `Admin@123456`

### Option 2: Dùng SQL Server Management Studio (SSMS)
- Tải tại: https://learn.microsoft.com/sql/ssms/download-sql-server-management-studio-ssms
- Server: `localhost,1433`
- Authentication: SQL Server Authentication
- Login: `sa`
- Password: `Admin@123456`

### Option 3: Dùng IntelliJ IDEA Database Tool
- Mở Database tab trong IntelliJ
- Tạo connection mới với SQL Server
- Nhập thông tin trên

## Troubleshooting

### Docker không chạy trên M1/M2 Mac
Nếu bạn dùng Mac M1/M2, thêm platform vào `docker-compose.yml`:

```yaml
services:
  mssql:
    image: mcr.microsoft.com/mssql/server:2022-latest
    platform: linux/amd64
    ...
```

### Port 1433 đã bị sử dụng
Thay đổi port trong `docker-compose.yml`:

```yaml
ports:
  - "1433:1433"  # Thay 1433 thành port khác (VD: 1434)
```

Sau đó cập nhật URL trong `application.properties`:

```
spring.datasource.url=jdbc:sqlserver://localhost:1434;databaseName=BusTicket;...
```

### Kiểm tra logs container
```bash
docker logs busticket-mssql
```

## Spring Boot Application Setup

Application.properties đã được cấu hình tự động kết nối tới Docker SQL Server.

Bạn chỉ cần:
1. Khởi động Docker container: `docker-compose up -d`
2. Chạy Spring Boot application từ IDE hoặc lệnh: `./mvnw spring-boot:run`

JPA Hibernate sẽ tự động tạo các bảng dựa trên Entity class.

## File Khởi Tạo Database

File `init-db.sql` sẽ tự động chạy khi container khởi động, tạo các bảng cơ bản. 
Bạn có thể chỉnh sửa file này để thêm bảng hoặc dữ liệu khác.

