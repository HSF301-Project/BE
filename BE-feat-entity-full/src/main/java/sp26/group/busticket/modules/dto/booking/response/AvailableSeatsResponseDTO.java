package sp26.group.busticket.modules.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSeatsResponseDTO {
    private Integer tripId;
    private Integer coachId;
    private String coachType;
    private String plateNumber;
    private List<SeatDTO> seats;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatDTO {
        private Integer seatId;
        private String seatNumber;
        private Integer floor;
    }
}
