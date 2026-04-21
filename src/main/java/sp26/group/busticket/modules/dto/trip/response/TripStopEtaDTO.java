package sp26.group.busticket.modules.dto.trip.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripStopEtaDTO {
    private String stopName;
    private String etaTime;
    /** START / INTERMEDIATE / END */
    private String stopType;
    /** PICKUP / DROPOFF / BOTH */
    private String pointType;
    /** Nhãn UI: "Chỉ đón" / "Chỉ trả" / "Đón & trả" */
    private String pointTypeLabel;
    /** offset phút kể từ lúc xuất bến, nếu được cấu hình trực tiếp */
    private Integer offsetMinutes;
}
