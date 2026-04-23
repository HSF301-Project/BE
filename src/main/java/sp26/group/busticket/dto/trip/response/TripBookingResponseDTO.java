package sp26.group.busticket.dto.trip.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripBookingResponseDTO {
    private UUID id;
    private String departureStation;
    private String departureDateTimeLabel;
    private String arrivalStation;
    private String arrivalDateTimeLabel;
    private List<TripStopEtaDTO> stopEtas;
    private String stopEtasJson;
    private boolean isExpired; // Thêm trường này để check xem chuyến đi đã hết hạn đặt vé chưa
}
