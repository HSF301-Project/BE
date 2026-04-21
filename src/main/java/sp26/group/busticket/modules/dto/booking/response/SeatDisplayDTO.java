package sp26.group.busticket.modules.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import sp26.group.busticket.modules.enumType.SeatStatusEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDisplayDTO {
    private String seatId;
    private SeatStatusEnum status;
    private boolean aisleAfter;
}
