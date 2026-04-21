package sp26.group.busticket.modules.dto.coach.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDetailDTO {
    private String name;
    private String phone;
    private String seatNumber;
    private String tripInfo;
    private String ticketCode;
}
