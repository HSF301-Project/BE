package sp26.group.busticket.dto.trip.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TripDriverOptionDTO {
    private UUID id;
    private String fullName;
    private String phone;
    private String licenseNumber;
    /** Hiển thị: Hoạt động (sẵn sàng) — map từ StatusEnum.ACTIVE cho DRIVER */
    private String readinessLabel;
}
