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
    private String arrivalTime;
    private String departureDate;
    private String coachPlate;
    private String coachType;
    private String driverName;
    private String driverPhone;
    private String secondDriverName;
    private String secondDriverPhone;
    private String assistantName;
    private String assistantPhone;
    private String pickUpAddress;
    private String dropOffAddress;
    private String intermediateStopsText;
    private List<TripStopEtaDTO> stopEtas;
    private List<AdminSeatStatusDTO> seats;
}
