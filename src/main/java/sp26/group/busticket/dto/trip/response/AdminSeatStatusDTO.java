package sp26.group.busticket.dto.trip.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSeatStatusDTO {
    private String seatNumber;
    private Integer floor;
    private boolean isOccupied;
    private String passengerName;
    private String passengerPhone;
    private String ticketCode;
}
