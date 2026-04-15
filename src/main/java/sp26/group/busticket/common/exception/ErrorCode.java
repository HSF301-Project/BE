package sp26.group.busticket.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Lỗi hệ thống
    UNEXPECTED_ERROR(500, "Đã xảy ra lỗi hệ thống cục bộ. Vui lòng thử lại sau."),
    
    // Lỗi nghiệp vụ (Business)
    USER_NOT_FOUND(404, "Không tìm thấy người dùng này trong hệ thống!"),
    EMAIL_ALREADY_EXISTS(409, "Email này đã được sử dụng, vui lòng chọn email khác!"),
    PRODUCT_NOT_FOUND(404, "Sản phẩm không tồn tại hoặc đã bị xóa!"),
    INVALID_INPUT(400, "Thông tin đầu vào không hợp lệ!"),
    PASSWORD_NOT_MATCH(400, "Mật khẩu xác nhận không khớp!"),
    ROLE_NOT_FOUND(404, "Không tìm thấy vai trò này!");

    private final int code;
    private final String defaultMessage;
}
