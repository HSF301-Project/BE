package sp26.group.busticket.dto.route.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.enumType.StopTypeEnum;

import java.util.UUID;

/**
 * Dữ liệu một điểm dừng khi tạo / chỉnh sửa tuyến đường.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopRequestDTO {

    /**
     * ID của địa điểm (FK → locations).
     * Bắt buộc – điểm dừng phải có trong danh mục địa điểm.
     */
    @NotNull(message = "Vui lòng chọn địa điểm dừng")
    private UUID locationId;

    /**
     * Loại hoạt động: PICKUP / DROPOFF / BOTH.
     * Mặc định BOTH nếu không truyền.
     */
    @Builder.Default
    private StopTypeEnum stopType = StopTypeEnum.BOTH;

    /**
     * Phút ước tính kể từ giờ xuất bến đến khi xe đến điểm dừng này.
     */
    private Integer offsetMinutes;

    /**
     * Số km từ bến đầu đến điểm dừng này.
     */
    private Float distanceFromStart;

    /** Ghi chú thêm (địa điểm chính xác, điểm nhận dạng, v.v.). */
    private String notes;
}
