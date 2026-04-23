package sp26.group.busticket.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.enumType.BookingStatusEnum;

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
    private String departureDateLabel;
    private String arrivalDateLabel;
    private String bookingDate;
    private boolean isCancellable;
    private boolean roundTrip;
    private MyTripResponseDTO returnTrip;

    public String getStatusLabel() {
        if (status == null) {
            return "Không xác định";
        }

        return switch (status) {
            case CONFIRMED -> "Đã xác nhận";
            case COMPLETED -> "Đã hoàn thành";
            case CANCELLED -> "Đã hủy";
            case PENDING -> "Chờ thanh toán";
        };
    }
}
