package sp26.group.busticket.modules.dto.coach.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CoachRequestDTO {
    @NotBlank(message = "biển số xe không được để trống")
    String plateNumber;

    @NotBlank(message = "loại xe không được để trống")
    String coachType;

    @NotNull(message = "tổng số ghế không được để trống")
    @Positive(message = "tổng số ghế phải lớn hơn 0")
    Integer totalSeats;
}
