package sp26.group.busticket.modules.dto.trip.response;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponseDTO {
    private UUID id;
    private String name;
    private String imageUrl;
    private boolean featured;
    private Double rating;
    private String busTypeLabel;
    private String amenities;
    private BigDecimal price;
    private String priceFormatted;
    private String departureTime;
    private String departureStation;
    private String arrivalTime;
    private String arrivalStation;
    private String duration;
    private Integer seatsLeft;
}
