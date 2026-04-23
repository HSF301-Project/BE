package sp26.group.busticket.modules.dto.coach.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import sp26.group.busticket.modules.enumType.CoachStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoachResponseDTO {
    UUID id;
    String plateNumber;
    String coachType;
    Integer totalSeats;
    CoachStatusEnum status;
    String statusLabel;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
