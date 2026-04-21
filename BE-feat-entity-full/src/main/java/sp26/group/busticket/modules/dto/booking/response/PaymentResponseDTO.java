package sp26.group.busticket.modules.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private UUID bookingId;
    private String expiryTime;
    private Long expiryTimestamp;
    private String selectedMethod;
    private String fromCity;
    private String toCity;
    private String departureTime;
    private String arrivalTime;
    private String dateLabel;
    private String busTypeLabel;
    private Integer ticketCount;
    private String totalFormatted;
}
