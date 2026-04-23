package sp26.group.busticket.dto.route.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PopularRouteDTO {
    private UUID tripId;
    private String fromCity;
    private String toCity;
    private Long price;
    private Long bookingsCount;
    private String imageUrl;
    private String priceDisplay;
    private String fromLocationName;
    private String toLocationName;
}

