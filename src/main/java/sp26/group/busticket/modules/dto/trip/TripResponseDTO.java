package sp26.group.busticket.modules.dto.trip;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TripResponseDTO {
    private Long id;
    private String tripCode;        // Tự sinh hoặc lấy từ ID: "TRP-" + id
    private String fromStation;     // Lấy từ route.getStartLocation()
    private String toStation;       // Lấy từ route.getEndLocation()

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
}