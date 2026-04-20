package sp26.group.busticket.modules.dto.coach.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachDetailResponseDTO {
    private UUID id;
    private String plateNumber;
    private String coachType;
    private Integer totalSeats;
    private List<TripDetailDTO> activeTrips;
}
