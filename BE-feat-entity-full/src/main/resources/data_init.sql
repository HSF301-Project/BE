-- ====================================================================
-- TẠO CƠ SỞ DỮ LIỆU BÁN VÉ XE KHÁCH
-- ====================================================================
CREATE DATABASE BusTicket;
GO
USE BusTicket;

-- 1. Bảng LOCATION
CREATE TABLE LOCATION (
    location_id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    city NVARCHAR(255) NOT NULL
);

-- 2. Bảng COACH
CREATE TABLE COACH (
    coach_id INT IDENTITY(1,1) PRIMARY KEY,
    plate_number VARCHAR(50) NOT NULL,
    coach_type NVARCHAR(100),
    total_seats INT NOT NULL
);

-- 3. Bảng SEAT
CREATE TABLE SEAT (
    seat_id INT IDENTITY(1,1) PRIMARY KEY,
    coach_id INT NOT NULL,
    seat_number VARCHAR(20) NOT NULL,
    floor INT,
    FOREIGN KEY (coach_id) REFERENCES COACH(coach_id) ON DELETE CASCADE
);

-- 4. Bảng ROUTE
CREATE TABLE ROUTE (
    route_id INT IDENTITY(1,1) PRIMARY KEY,
    departure_location_id INT NOT NULL,
    arrival_location_id INT NOT NULL,
    distance FLOAT,
    duration INT,
    FOREIGN KEY (departure_location_id) REFERENCES LOCATION(location_id),
    FOREIGN KEY (arrival_location_id) REFERENCES LOCATION(location_id)
);

-- 5. Bảng TRIP
CREATE TABLE TRIP (
    trip_id INT IDENTITY(1,1) PRIMARY KEY,
    route_id INT NOT NULL,
    coach_id INT NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    price_base DECIMAL(18,2) NOT NULL,
    FOREIGN KEY (route_id) REFERENCES ROUTE(route_id),
    FOREIGN KEY (coach_id) REFERENCES COACH(coach_id)
);

-- 6. Bảng ACCOUNTS
CREATE TABLE accounts (
    id INT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name NVARCHAR(255),
    phone VARCHAR(20),
    status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    role NVARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- 7. Bảng BOOKING
CREATE TABLE BOOKING (
    booking_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NULL,
    trip_id INT NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    status NVARCHAR(50) DEFAULT 'PENDING',
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES accounts(id),
    FOREIGN KEY (trip_id) REFERENCES TRIP(trip_id)
);

-- 8. Bảng PAYMENT
CREATE TABLE PAYMENT (
    payment_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    payment_method NVARCHAR(100),
    amount DECIMAL(18,2) NOT NULL,
    transaction_id VARCHAR(100),
    status NVARCHAR(50) DEFAULT 'PENDING',
    paid_at DATETIME,
    FOREIGN KEY (booking_id) REFERENCES BOOKING(booking_id) ON DELETE CASCADE
);

-- 9. Bảng TICKET
CREATE TABLE TICKET (
    ticket_id INT IDENTITY(1,1) PRIMARY KEY,
    booking_id INT NOT NULL,
    seat_id INT NOT NULL,
    passenger_name NVARCHAR(255) NOT NULL,
    ticket_code VARCHAR(50) UNIQUE NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES BOOKING(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (seat_id) REFERENCES SEAT(seat_id)
);

-- 10. Bảng FEEDBACK
CREATE TABLE FEEDBACK (
    feedback_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    trip_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    comment NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES accounts(id),
    FOREIGN KEY (trip_id) REFERENCES TRIP(trip_id)
);

-- ====================================================================
-- DỮ LIỆU MẪU
-- ====================================================================

INSERT INTO LOCATION (name, city) VALUES 
(N'Bến xe Hanoi', N'Hà Nội'),
(N'Bến xe Sài Gòn', N'Hồ Chí Minh'),
(N'Bến xe Đà Nẵng', N'Đà Nẵng'),
(N'Bến xe Hải Phòng', N'Hải Phòng'),
(N'Bến xe Nha Trang', N'Khánh Hòa'),
(N'Bến xe Đà Lạt', N'Lâm Đồng');

INSERT INTO accounts (email, password, full_name, phone, status, role, created_at, updated_at)
VALUES 
('admin@busticket.com', 'admin123', 'Admin', '0912345678', 'ACTIVE', 'ADMIN', GETDATE(), GETDATE()),
('staff@busticket.com', 'staff123', 'Nhan Vien', '0912345679', 'ACTIVE', 'STAFF', GETDATE(), GETDATE()),
('user@busticket.com', 'user123', N'Nguyễn Văn A', '0987654321', 'ACTIVE', 'USER', GETDATE(), GETDATE());

INSERT INTO COACH (plate_number, coach_type, total_seats)
VALUES 
('29B-12345', N'Giường nằm', 40),
('29B-54321', N'Ghế ngồi', 45),
('51B-11111', N'Giường nằm', 38);

INSERT INTO ROUTE (departure_location_id, arrival_location_id, distance, duration)
VALUES 
(1, 2, 1700, 1680),
(1, 3, 760, 720),
(2, 5, 440, 420),
(2, 6, 310, 360),
(1, 4, 120, 120);

INSERT INTO TRIP (route_id, coach_id, departure_time, arrival_time, price_base)
VALUES 
(1, 1, DATEADD(DAY, 1, GETDATE()) + ' 18:00:00', DATEADD(DAY, 2, GETDATE()) + ' 22:00:00', 350000),
(1, 2, DATEADD(DAY, 1, GETDATE()) + ' 19:30:00', DATEADD(DAY, 2, GETDATE()) + ' 23:30:00', 320000),
(2, 3, DATEADD(DAY, 2, GETDATE()) + ' 08:00:00', DATEADD(DAY, 2, GETDATE()) + ' 20:00:00', 250000),
(3, 1, DATEADD(DAY, 3, GETDATE()) + ' 07:00:00', DATEADD(DAY, 3, GETDATE()) + ' 14:00:00', 180000),
(4, 2, DATEADD(DAY, 4, GETDATE()) + ' 06:00:00', DATEADD(DAY, 4, GETDATE()) + ' 12:00:00', 150000),
(5, 3, DATEADD(DAY, 1, GETDATE()) + ' 09:00:00', DATEADD(DAY, 1, GETDATE()) + ' 11:00:00', 80000);

INSERT INTO SEAT (coach_id, seat_number, floor)
SELECT 1, number, CASE WHEN number <= 20 THEN 1 ELSE 2 END
FROM (SELECT TOP 40 ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) + 0 AS number FROM sys.objects) AS seats;

INSERT INTO SEAT (coach_id, seat_number, floor)
SELECT 2, number, CASE WHEN number <= 22 THEN 1 ELSE 2 END
FROM (SELECT TOP 45 ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) + 0 AS number FROM sys.objects) AS seats;

INSERT INTO SEAT (coach_id, seat_number, floor)
SELECT 3, number, CASE WHEN number <= 19 THEN 1 ELSE 2 END
FROM (SELECT TOP 38 ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) + 0 AS number FROM sys.objects) AS seats;

PRINT N'Tạo database và dữ liệu mẫu thành công!';
