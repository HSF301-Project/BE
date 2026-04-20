package sp26.group.busticket.modules.dto.trip.response;

import lombok.*;
import sp26.group.busticket.modules.dto.coach.response.TripHistoryDTO;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTripDetailResponseDTO {
    private UUID tripId;
    private String routeName;
    private String departureTime;
    private String coachPlate;
    private String driverName;
    private List<AdminSeatStatusDTO> seats;
}
