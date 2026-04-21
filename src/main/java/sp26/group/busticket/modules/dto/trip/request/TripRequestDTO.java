package sp26.group.busticket.modules.dto.trip.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.modules.enumType.TripStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripRequestDTO {

    private UUID id;

    @NotNull(message = "Vui lòng chọn tuyến đường")
    private UUID routeId;

    @NotNull(message = "Vui lòng chọn xe")
    private UUID coachId;

    /**
     * Chế độ: "existing" = chọn tài xế có sẵn; "new" = nhập tài xế mới (tạo Account DRIVER).
     */
    @Builder.Default
    private String driverInputMode = "existing";

    /** Bắt buộc khi driverInputMode = existing */
    private UUID driverId;



    private String newDriverFullName;
    private String newDriverPhone;

    @Email(message = "Email tài xế không hợp lệ")
    private String newDriverEmail;

    private String newDriverLicense;

    /** Cập nhật GPLX cho tài xế đã chọn (tùy chọn). */
    private String existingDriverLicenseUpdate;

    private UUID assistantId;

    @NotNull(message = "Thời gian khởi hành không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime departureTime;

    @NotNull(message = "Thời gian đến dự kiến không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Giá vé không được để trống")
    @Positive(message = "Giá vé phải lớn hơn 0")
    private BigDecimal priceBase;

    @NotBlank(message = "Số điện thoại liên hệ chuyến không được để trống")
    private String contactPhoneNumber;

    @NotNull(message = "Trạng thái chuyến đi không được để trống")
    @Builder.Default
    private TripStatusEnum status = TripStatusEnum.SCHEDULED;

    @AssertTrue(message = "Vui lòng chọn tài xế khi dùng danh sách có sẵn")
    public boolean isDriverValidForMode() {
        if (driverInputMode == null || "existing".equalsIgnoreCase(driverInputMode.trim())) {
            return driverId != null;
        }
        return true;
    }

    @AssertTrue(message = "Vui lòng nhập họ tên, SĐT và email khi tạo tài xế mới")
    public boolean isNewDriverFieldsValid() {
        if (driverInputMode != null && "new".equalsIgnoreCase(driverInputMode.trim())) {
            return newDriverFullName != null && !newDriverFullName.isBlank()
                    && newDriverPhone != null && !newDriverPhone.isBlank()
                    && newDriverEmail != null && !newDriverEmail.isBlank();
        }
        return true;
    }
}
