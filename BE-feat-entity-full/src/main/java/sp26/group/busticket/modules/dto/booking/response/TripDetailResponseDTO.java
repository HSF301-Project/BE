package sp26.group.busticket.modules.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDetailResponseDTO {
    private Integer tripId;
    private String departureLocation;
    private String arrivalLocation;
    private String departureTime;
    private String arrivalTime;
    private BigDecimal priceBase;
    private String coachType;
    private String plateNumber;
    private Integer totalSeats;
    private Integer availableSeats;
}
