package sp26.group.busticket.modules.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDisplayDTO {
    private String seatId;
    private String status; // AVAILABLE, SELECTED, BOOKED
    private boolean aisleAfter;
}
