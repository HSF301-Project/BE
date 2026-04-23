package sp26.group.busticket.dto.booking.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sp26.group.busticket.dto.booking.response.PriceItemDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingFormDTO {
    @Builder.Default
    private List<PassengerInfoDTO> passengers = new ArrayList<>();
    @Builder.Default
    private List<PassengerInfoDTO> returnPassengers = new ArrayList<>();
    @Builder.Default
    private List<PriceItemDTO> priceItems = new ArrayList<>();
    private String totalFormatted;
    private UUID pickupLocationId;
    private UUID dropoffLocationId;
    private UUID returnPickupLocationId;
    private UUID returnDropoffLocationId;
    private boolean roundTrip;
    private UUID returnTripId;

    public List<PassengerInfoDTO> getPassengers() {
        return passengers;
    }
}
