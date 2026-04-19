package sp26.group.busticket.modules.dto.booking.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketConfirmationDTO {
    private UUID id;
    private String statusLabel;
    private String bookingCode;
    private String fromCityShort;
    private String toCityShort;
    private String departureStation;
    private String arrivalStation;
    private String departureTime;
    private String arrivalTime;
    private String departureDateLabel;
    private String seatLabel;
    private String licensePlate;
    private String serviceType;
    private String passengerName;
    private String totalFormatted;
}
