# Giải thích nghiệp vụ và ERD (hiện trạng)

## 1) Tổng quan
Tài liệu này mô tả nghiệp vụ và mối quan hệ CSDL của hệ thống đặt vé xe theo code hiện tại.
Tất cả entity đều kế thừa `BaseEntity` trong `src/main/java/sp26/group/busticket/infrastructure/persistence/BaseEntity.java` với các trường dùng chung:
- `id` (UUID, PK)
- `created_at`
- `updated_at`

## 2) Ý nghĩa từng bảng (ngắn gọn)
- `accounts`: tài khoản đăng nhập (email, password, role, status).
- `locations`: địa điểm (tên bến/khu vực, thành phố).
- `routes`: tuyến đường cố định giữa 2 location, có khoảng cách và thời gian di chuyển.
- `coaches`: thông tin xe (biển số, loại xe, tổng số ghế).
- `seats`: danh sách ghế của 1 xe (số ghế, tầng).
- `trips`: chuyến xe cụ thể theo ngày giờ, gắn route + coach (+ assistant nếu có).
- `bookings`: đơn đặt chỗ của user cho 1 trip.
- `tickets`: vé từng hành khách trong booking, mỗi vé gắn 1 ghế.
- `payments`: giao dịch thanh toán của booking (1 booking chỉ có 1 payment).
- `feedbacks`: đánh giá của user cho trip.

## 3) ERD ngắn gọn (PK/FK + quan hệ)
```text
accounts (id PK)
  1 - n bookings.user_id
  1 - n feedbacks.user_id
  1 - n trips.assistant_id (nullable)

locations (id PK)
  1 - n routes.departure_location_id
  1 - n routes.arrival_location_id

routes (id PK, departure_location_id FK, arrival_location_id FK)
  1 - n trips.route_id

coaches (id PK)
  1 - n seats.coach_id
  1 - n trips.coach_id

trips (id PK, route_id FK, coach_id FK, assistant_id FK nullable)
  1 - n bookings.trip_id
  1 - n feedbacks.trip_id

bookings (id PK, user_id FK, trip_id FK)
  1 - n tickets.booking_id
  1 - 1 payments.booking_id (unique)

seats (id PK, coach_id FK)
  1 - n tickets.seat_id

tickets (id PK, booking_id FK, seat_id FK, ticket_code unique)

payments (id PK, booking_id FK unique)

feedbacks (id PK, user_id FK, trip_id FK)
```

## 4) Luồng nghiệp vụ đặt vé (hiện tại)
1. **Tìm chuyến**
   - Hệ thống lọc trip theo điểm đi/đến/ngày.
   - Số ghế trống được tính bằng tổng ghế của coach trừ đi số ticket đang ở booking `PENDING` hoặc `CONFIRMED`.

2. **Chọn ghế**
   - Lấy danh sách ghế theo tầng (floor 1, floor 2).
   - Ghế nào đã xuất hiện trong ticket của booking `PENDING|CONFIRMED` thì hiển thị `BOOKED`.

3. **Tạo booking**
   - Kiểm tra trùng ghế trên cùng trip (chống đặt trùng).
   - Tạo `booking` với trạng thái `PENDING`.
   - Tạo `payment` ban đầu với trạng thái `PENDING`.
   - Tạo nhiều `ticket` theo danh sách hành khách.

4. **Thanh toán**
   - Nếu hợp lệ: cập nhật booking sang `CONFIRMED`.
   - Cập nhật payment sang `PAID`, lưu phương thức thanh toán và thời điểm thanh toán.

5. **Quá hạn giữ chỗ**
   - Job chạy định kỳ mỗi phút.
   - Booking `PENDING` quá 7 phút sẽ bị chuyển `CANCELLED`.
   - Payment liên quan cũng chuyển `CANCELLED`.

## 5) Trạng thái nghiệp vụ
- `BookingStatusEnum`: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`.
- `PaymentStatusEnum`: `PENDING`, `PAID`, `CANCELLED`.
- `SeatStatusEnum` (hiển thị): `AVAILABLE`, `SELECTED`, `BOOKED`.

## 6) Tách theo vai trò (mức hiện tại)
- `USER`: đặt vé, thanh toán, xem chuyến của tôi.
- `ADMIN`: đã có dữ liệu seed tài khoản admin; rule endpoint admin trong security đang ở dạng comment.
- `ASSISTANT`: đã có liên kết dữ liệu tại `trips.assistant_id`, nhưng chưa thấy endpoint phân quyền riêng cho role này.

## 7) Ràng buộc dữ liệu quan trọng
- `accounts.email` là unique.
- `coaches.plate_number` là unique.
- `tickets.ticket_code` là unique.
- `payments.booking_id` là unique (đảm bảo 1 booking - 1 payment).
- Chống trùng ghế hiện tại xử lý ở tầng service khi tạo booking.
