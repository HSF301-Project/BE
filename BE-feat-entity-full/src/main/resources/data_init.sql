-- ====================================================================
-- DỮ LIỆU MẪU BÁN VÉ XE KHÁCH
-- ====================================================================

-- 1. Insert Locations
INSERT INTO LOCATION (name, city) VALUES 
(N'Bến xe Hanoi', N'Hà Nội'),
(N'Bến xe Sài Gòn', N'Hồ Chí Minh'),
(N'Bến xe Đà Nẵng', N'Đà Nẵng'),
(N'Bến xe Hải Phòng', N'Hải Phòng'),
(N'Bến xe Nha Trang', N'Khánh Hòa'),
(N'Bến xe Đà Lạt', N'Lâm Đồng');

-- 2. Insert Accounts (password: admin123, user123 - plain text)
INSERT INTO accounts (email, password, full_name, phone, status, role, created_at, updated_at)
VALUES 
('admin@busticket.com', 'admin123', 'Admin', '0912345678', 'ACTIVE', 'ADMIN', GETDATE(), GETDATE()),
('user@busticket.com', 'user123', N'Nguyễn Văn A', '0987654321', 'ACTIVE', 'USER', GETDATE(), GETDATE());

-- 3. Insert Coaches
INSERT INTO COACH (plate_number, coach_type, total_seats)
VALUES 
('29B-12345', N'Giường nằm', 40),
('29B-54321', N'Ghế ngồi', 45),
('51B-11111', N'Giường nằm', 38);

-- 4. Insert Routes
-- route_id, departure_location_id, arrival_location_id, distance, duration
INSERT INTO ROUTE (departure_location_id, arrival_location_id, distance, duration)
VALUES 
(1, 2, 1700, 1680),  -- Hanoi -> Ho Chi Minh
(1, 3, 760, 720),   -- Hanoi -> Da Nang
(2, 5, 440, 420),   -- Ho Chi Minh -> Nha Trang
(2, 6, 310, 360),   -- Ho Chi Minh -> Da Lat
(1, 4, 120, 120);   -- Hanoi -> Hai Phong

-- 5. Insert Trips
-- trip_id, route_id, coach_id, departure_time, arrival_time, price_base
INSERT INTO TRIP (route_id, coach_id, departure_time, arrival_time, price_base)
VALUES 
(1, 1, DATEADD(DAY, 1, GETDATE()) + ' 18:00:00', DATEADD(DAY, 2, GETDATE()) + ' 22:00:00', 350000),
(1, 2, DATEADD(DAY, 1, GETDATE()) + ' 19:30:00', DATEADD(DAY, 2, GETDATE()) + ' 23:30:00', 320000),
(2, 3, DATEADD(DAY, 2, GETDATE()) + ' 08:00:00', DATEADD(DAY, 2, GETDATE()) + ' 20:00:00', 250000),
(3, 1, DATEADD(DAY, 3, GETDATE()) + ' 07:00:00', DATEADD(DAY, 3, GETDATE()) + ' 14:00:00', 180000),
(4, 2, DATEADD(DAY, 4, GETDATE()) + ' 06:00:00', DATEADD(DAY, 4, GETDATE()) + ' 12:00:00', 150000),
(5, 3, DATEADD(DAY, 1, GETDATE()) + ' 09:00:00', DATEADD(DAY, 1, GETDATE()) + ' 11:00:00', 80000);

-- 6. Insert Seats for each coach
-- Coach 1: 40 seats
INSERT INTO SEAT (coach_id, seat_number, floor)
SELECT 1, number, CASE WHEN number <= 20 THEN 1 ELSE 2 END
FROM (SELECT TOP 40 ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) + 0 AS number FROM sys.objects) AS seats;

-- Coach 2: 45 seats
INSERT INTO SEAT (coach_id, seat_number, floor)
SELECT 2, number, CASE WHEN number <= 22 THEN 1 ELSE 2 END
FROM (SELECT TOP 45 ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) + 0 AS number FROM sys.objects) AS seats;

-- Coach 3: 38 seats
INSERT INTO SEAT (coach_id, seat_number, floor)
SELECT 3, number, CASE WHEN number <= 19 THEN 1 ELSE 2 END
FROM (SELECT TOP 38 ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) + 0 AS number FROM sys.objects) AS seats;

PRINT N'Đã thêm dữ liệu mẫu thành công!';