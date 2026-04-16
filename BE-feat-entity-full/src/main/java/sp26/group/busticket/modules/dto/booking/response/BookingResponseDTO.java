package sp26.group.busticket.modules.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private Integer bookingId;
    private String bookingCode;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    
    private TripInfoDTO trip;
    private List<TicketInfoDTO> tickets;
    private PaymentInfoDTO payment;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripInfoDTO {
        private Integer tripId;
        private String departureLocation;
        private String arrivalLocation;
        private String departureTime;
        private String arrivalTime;
        private String coachType;
        private String plateNumber;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketInfoDTO {
        private Integer ticketId;
        private String ticketCode;
        private String seatNumber;
        private Integer floor;
        private String passengerName;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfoDTO {
        private Integer paymentId;
        private String paymentMethod;
        private BigDecimal amount;
        private String status;
        private String transactionId;
    }
}
