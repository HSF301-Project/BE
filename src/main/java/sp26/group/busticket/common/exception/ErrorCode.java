package sp26.group.busticket.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Lỗi hệ thống
    UNEXPECTED_ERROR(500, "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."),

    // Lỗi tài khoản
    USER_NOT_FOUND(404, "Không tìm thấy thông tin người dùng!"),
    EMAIL_ALREADY_EXISTS(409, "Email này đã được sử dụng!"),
    PHONE_ALREADY_EXISTS(409, "Số điện thoại này đã được sử dụng!"),
    PASSWORD_NOT_MATCH(400, "Mật khẩu xác nhận không khớp!"),
    ROLE_NOT_FOUND(404, "Không tìm thấy vai trò này!"),

    // Lỗi nghiệp vụ Xe (Coach)
    COACH_NOT_FOUND(404, "Không tìm thấy xe trong hệ thống!"),
    PLATE_NUMBER_ALREADY_EXISTS(409, "Biển số xe này đã tồn tại!"),
    LOCATION_NOT_FOUND(404, "Không tìm thấy địa điểm này!"),

    // Lỗi nghiệp vụ Chuyến đi & Đặt vé
    TRIP_NOT_FOUND(404, "Chuyến xe không tồn tại hoặc đã bị hủy!"),
    TRIP_MISSING_ROUTE_POINTS(400, "Vui lòng nhập đầy đủ địa điểm đón và địa điểm trả."),
    TRIP_DRIVER_REQUIRED(400, "Vui lòng chọn tài xế hoặc tạo tài xế mới cho chuyến đi."),
    DRIVER_NOT_ASSIGNABLE(400, "Tài xế không hợp lệ hoặc chưa ở trạng thái sẵn sàng (hoạt động)."),
    DRIVER_EMAIL_EXISTS(409, "Email tài xế đã được sử dụng."),
    ROUTE_NOT_FOUND(404, "Không tìm thấy tuyến đường này!"),
    BOOKING_NOT_FOUND(404, "Không tìm thấy thông tin đặt vé!"),
    SEAT_NOT_AVAILABLE(400, "Ghế bạn chọn hiện không còn trống!"),
    SEAT_NOT_FOUND(404, "Không tìm thấy vị trí ghế!"),
    
    // Lỗi chung
    INVALID_INPUT(400, "Dữ liệu nhập vào không hợp lệ!"), PRODUCT_NOT_FOUND(404, "Không tìm thấy sản phẩm!"), ORDER_NOT_FOUND(404, "Không tìm thấy đơn hàng!"), PAYMENT_FAILED(400, "Thanh toán thất bại!"), INSUFFICIENT_STOCK(400, "Số lượng sản phẩm không đủ!"), UNAUTHORIZED(401, "Bạn không có quyền truy cập tài nguyên này!"), FORBIDDEN(403, "Bạn không có quyền thực hiện hành động này!");

    private final int code;
    private final String defaultMessage;
}
