package sp26.group.busticket.dto.booking.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    private String customerPhone;

    @NotBlank(message = "Họ tên khách hàng không được để trống")
    private String customerName;

    private String customerEmail;

    @NotEmpty(message = "Vui lòng chọn ít nhất một ghế")
    private List<String> selectedSeats; // List seat numbers (e.g., A1, A2)

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String paymentMethod; // CASH

    private UUID pickupLocationId;
    private UUID dropoffLocationId;
}
