package sp26.group.busticket.modules.dto.booking.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerInfoDTO {
    private String seatId;
    private String seatLabel;
    private String deck;
    private String fullName;
    private String phoneNumber;
    private String email;
}
