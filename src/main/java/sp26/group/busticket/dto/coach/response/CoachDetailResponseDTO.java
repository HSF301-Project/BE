package sp26.group.busticket.dto.coach.response;

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
    
    // Thông tin tài xế hiện tại
    private String currentDriverName;
    private String currentDriverPhone;
    
    // Lịch sử chuyến đi
    private List<TripHistoryDTO> tripHistory;
}
