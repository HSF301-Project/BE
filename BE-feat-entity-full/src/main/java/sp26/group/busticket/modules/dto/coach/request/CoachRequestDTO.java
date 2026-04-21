package sp26.group.busticket.modules.dto.coach.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{10}$",
            message = "Biển số  phai co dung 10 ky tu, bao gom chu va so"
    )
    String plateNumber;

    @NotBlank(message = "Loại xe không được để trống")

    String coachType;

    @NotNull(message = "Tổng số ghế không được để trống")
    @Pattern(regexp = "\\d{9}", message = "Tổng số ghế  phai bé hơn hoặc bằng 16")
    @Positive(message = "Tổng số ghế phải lớn hơn 0")
    Integer totalSeats;
}
