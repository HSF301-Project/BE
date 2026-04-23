package sp26.group.busticket.dto.coach.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripHistoryDTO {
    private java.util.UUID tripId;
    private String routeName;
    private String departureTime;
    private String arrivalTime;
    private String status;
    private String driverName;
    private Integer totalOccupiedSeats;
}
