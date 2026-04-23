package sp26.group.busticket.modules.dto.trip.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSearchRequestDTO {
    private String from;
    private String to;
    private String date;
    private String returnDate;
    private boolean roundTrip;
    @Builder.Default
    private List<String> timeSlots = new ArrayList<>();
    private String busType;
    private Integer maxPrice;
    private String sort;
}
