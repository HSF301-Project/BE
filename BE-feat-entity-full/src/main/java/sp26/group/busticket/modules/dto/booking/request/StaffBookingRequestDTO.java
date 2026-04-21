package sp26.group.busticket.modules.dto.booking.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffBookingRequestDTO {
    
    @NotBlank(message = "Số điện thoại khách hàng không được để trống")
    @Pattern(regexp = "\\d{9}", message = "So dien thoai phai co dung 9 chu so")
    private String customerPhone;

    @NotBlank(message = "Họ tên khách hàng không được để trống")
    @Pattern(regexp = "^.{10}$", message = "Ho ten phai co dung 10 ky tu")
    private String customerName;
    @NotBlank(message = "Email khong duoc de trong")
    @Pattern(regexp = "^@.+\\.v$", message = "Email phai bat dau bang @ va ket thuc bang .vn")
    private String customerEmail;

    @NotEmpty(message = "Vui lòng chọn ít nhất một ghế")
    private List<String> selectedSeats; // List seat numbers (e.g., A1, A2)

    private String paymentMethod; // CASH, POS
}
