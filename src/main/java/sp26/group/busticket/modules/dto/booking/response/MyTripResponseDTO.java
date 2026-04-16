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
public class MyTripResponseDTO {
    private UUID id;
    private String bookingCode;
    private String status; // UPCOMING, COMPLETED, CANCELLED
    private String busTypeLabel;
    private Long daysUntilDeparture;
    private String fromCity;
    private String departureStation;
    private String departureTime;
    private String toCity;
    private String arrivalStation;
    private String arrivalTime;
}
