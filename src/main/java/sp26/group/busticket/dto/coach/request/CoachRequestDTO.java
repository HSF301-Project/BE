package sp26.group.busticket.dto.coach.request;

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
    @NotBlank(message = "Biển số xe không được để trống")
    @jakarta.validation.constraints.Size(max = 9, message = "Sai định dạng biển số")
    String plateNumber;

    @NotNull(message = "Vui lòng chọn loại xe")
    java.util.UUID coachTypeId;

    @NotNull(message = "Tổng số ghế không được để trống")
    @jakarta.validation.constraints.Min(value = 6, message = "Xe khách cơ bản thường là từ 6 tới 40 chỗ")
    @jakarta.validation.constraints.Max(value = 40, message = "Xe khách cơ bản thường là từ 6 tới 40 chỗ")
    Integer totalSeats;


}
