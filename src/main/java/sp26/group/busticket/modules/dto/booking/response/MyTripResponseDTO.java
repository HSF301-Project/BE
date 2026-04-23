package sp26.group.busticket.modules.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.modules.enumType.BookingStatusEnum;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyTripResponseDTO {
    private UUID id;
    private String bookingCode;
    private BookingStatusEnum status;
    private String busTypeLabel;
    private Long daysUntilDeparture;
    private String fromCity;
    private String departureStation;
    private String departureTime;
    private String toCity;
    private String arrivalStation;
    private String arrivalTime;
    private String pickupLocationName;
    private String dropoffLocationName;
    private String pickupTime;
    private String dropoffTime;
    private String bookingDate;
    private boolean isCancellable;

    public String getStatusLabel() {
        if (status == null) {
            return "Không xác định";
        }

        switch (status) {
            case CONFIRMED: return "Đã xác nhận";
            case COMPLETED: return "Đã hoàn thành";
            case CANCELLED: return "Đã hủy";
            case PENDING: return "Chờ thanh toán";
            default: return status.name();
        }
    }
}
