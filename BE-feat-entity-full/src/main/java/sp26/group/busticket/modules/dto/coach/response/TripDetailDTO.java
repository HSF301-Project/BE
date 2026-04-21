package sp26.group.busticket.modules.dto.coach.response;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDetailDTO {
    private String routeName;
    private String departureTime;
    private String status;
    private Integer passengerCount;
    private List<PassengerDetailDTO> passengers;
}
