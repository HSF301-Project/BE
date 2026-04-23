package sp26.group.busticket.modules.dto.trip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.modules.dto.trip.response.TripStopEtaDTO;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripAdminResponseDTO {
    private UUID id;
    private String tripCode;
    private String fromStation;
    private String toStation;
    private String fromCity;
    private String toCity;

    // Thông tin xe
    private String busType;
    private String busTypeLabel;

    // Thời gian
    private String departureTime;
    private String departureAmPm;

    // Trạng thái & Lấp đầy
    private int bookedSeats;
    private int totalSeats;
    private int fillPercent;

    private String status;
    private String statusLabel;

    private String driverName;
    private String driverPhone;
    private String assistantName;
    private String assistantPhone;
    private String coachPlate;
    private String departureDateTime;
    private String arrivalDateTime;
    private String arrivalTime;
    private String priceFormatted;

    private boolean hasIssue;
    private Integer minutesUntilDeparture;
    private String formattedDepartureCountdown;

    private List<TripStopEtaDTO> routeTimeline;
    private String nextStopLabel;
}