package sp26.group.busticket.modules.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.modules.enumType.BookingStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponseDTO {
    // 1. Thông tin chung
    private String bookingCode;
    private BookingStatusEnum status;
    private LocalDateTime bookingTime;
    
    // 2. Thông tin hành khách
    private String passengerName;
    private String passengerPhone;
    private String passengerEmail;
    private List<String> seatNumbers;
    
    // 3. Thông tin chuyến đi & Route
    private String routeName; // Ví dụ: Bến xe Miền Đông -> Bến xe Đà Lạt
    private String pickupPointName;
    private String pickupTime;
    private String dropoffPointName;
    private String dropoffTime;
    
    // 4. Thông tin xe
    private String coachPlate;
    private String coachType;
    
    // 5. Chi tiết thanh toán
    private Double basePrice;
    private Integer seatCount;
    private Double totalAmount;
    private String totalAmountFormatted;
}
