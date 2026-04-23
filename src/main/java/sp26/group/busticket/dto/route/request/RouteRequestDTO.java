package sp26.group.busticket.dto.route.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequestDTO {

    private UUID id;

    @NotBlank(message = "Vui lòng nhập mã/tên tuyến (ví dụ: SG-DL-01)")
    private String routeCode;

    @NotNull(message = "Vui lòng chọn điểm khởi hành")
    private UUID departureLocationId;

    @NotNull(message = "Vui lòng chọn điểm đến")
    private UUID arrivalLocationId;

    @NotNull(message = "Vui lòng nhập khoảng cách")
    @Positive(message = "Khoảng cách phải > 0")
    private Float distanceKm;

    @NotNull(message = "Vui lòng nhập thời lượng (phút)")
    @Positive(message = "Thời lượng phải > 0")
    private Integer durationMinutes;

    /**
     * Danh sách điểm dừng đón/trả trung gian.
     * Mỗi phần tử chứa locationId (FK → locations) + metadata.
     * Thứ tự trong list = thứ tự dừng thực tế (stopOrder).
     */
    @Valid
    @Builder.Default
    private List<RouteStopRequestDTO> stops = new ArrayList<>();

    /**
     * Danh sách điểm dừng đón/trả trung gian cho tuyến khứ hồi (nếu createReturn = true).
     */
    @Valid
    @Builder.Default
    private List<RouteStopRequestDTO> returnStops = new ArrayList<>();

    private boolean createReturn; // Checkbox tích chọn tạo khứ hồi
    private String returnRouteCode;
    private Float returnDistanceKm;
    private Integer returnDurationMinutes;
}
