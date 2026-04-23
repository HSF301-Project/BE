package sp26.group.busticket.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.enumType.BookingStatusEnum;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponseDTO { // DTO dùng để hiện thị cho page my_trip.
    // 1. Thông tin chung
    private String bookingCode;
    private BookingStatusEnum status;
    private String statusLabel;
    private LocalDateTime bookingTime;
    private String bookingDate;
    private boolean roundTrip;
    
    // 2. Thông tin hành khách
    private String passengerName;
    private String passengerPhone;
    private String passengerEmail;
    private List<String> seatNumbers;
    private List<String> seatTicketLines;
    private String seatLabel;
    
    // 3. Thông tin chuyến đi & Route
    private String routeName;
    private String fromCityShort;
    private String toCityShort;
    private String pickupLocationName;
    private String pickupPointName;
    private String pickupTime;
    private String dropoffLocationName;
    private String dropoffPointName;
    private String dropoffTime;
    private String departureDateLabel;
    private String arrivalDateLabel;
    private String fromCity;
    private String toCity;
    
    // 4. Thông tin xe
    private String licensePlate;
    private String coachPlate;
    private String coachType;
    private String serviceType;
    
    // 5. Chi tiết thanh toán
    private Double basePrice;
    private Integer seatCount;
    private Double totalAmount;
    private String totalFormatted;
    private String totalAmountFormatted;

    // 6. Thông tin khứ hồi (nếu có)
    private TicketDetailResponseDTO returnTicket;
}
