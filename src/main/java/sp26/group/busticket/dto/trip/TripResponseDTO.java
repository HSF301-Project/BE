package sp26.group.busticket.dto.trip;

import lombok.Builder;
import lombok.Data;

import sp26.group.busticket.dto.trip.response.TripStopEtaDTO;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TripResponseDTO {
    private UUID id;
    private String tripCode;        // Tự sinh hoặc lấy từ ID: "TRP-" + id
    private String fromStation;     // Lấy từ route.getStartLocation()
    private String toStation;       // Lấy từ route.getEndLocation()
    private String fromCity;        // Tỉnh/Thành phố xuất phát
    private String toCity;          // Tỉnh/Thành phố đích

    // Thông tin xe
    private String busType;         // Lấy từ coach.getType() (ví dụ: LIMOUSINE)
    private String busTypeLabel;    // "Hạng Limousine"

    // Thời gian
    private String departureTime;   // Format "HH:mm"
    private String departureAmPm;   // "SA" hoặc "CH"

    // Trạng thái & Lấp đầy
    private int bookedSeats;        // Tính từ bảng Tickets/Orders
    private int totalSeats;         // Lấy từ coach.getCapacity()
    private int fillPercent;        // (bookedSeats * 100 / totalSeats)

    private String status;          // DA_XAC_NHAN, DANG_CHO, DA_HUY
    private String statusLabel;     // "Đã xác nhận", ...

    private String driverName;
    private String driverPhone;
    private String secondDriverName;
    private String secondDriverPhone;
    private String assistantName;
    private String assistantPhone;
    private String coachPlate;
    private String departureDateTime; // "HH:mm dd/MM/yyyy"
    private String arrivalDateTime;   // "HH:mm dd/MM/yyyy"
    private String arrivalTime;       // "HH:mm"

    private boolean hasIssue;
    private Integer minutesUntilDeparture;
    private String formattedDepartureCountdown;

    /** Timeline các điểm theo tuyến + ETA để hiển thị trong bảng quản lý. */
    private List<TripStopEtaDTO> routeTimeline;

    /** Trạm kế tiếp (đối với chuyến đang chạy). */
    private String nextStopLabel;
}