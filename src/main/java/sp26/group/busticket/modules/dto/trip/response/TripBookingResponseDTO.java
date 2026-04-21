package sp26.group.busticket.modules.dto.trip.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripBookingResponseDTO {
    private UUID id;
    private String departureStation;
    private String departureDateTimeLabel;
    private String arrivalStation;
    private String arrivalDateTimeLabel;
}
