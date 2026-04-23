package sp26.group.busticket.dto.trip.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripSearchResultDTO {
    private String fromCity;
    private String toCity;
    private String date;
    private String dateLabel;
    private String returnDate;
    private String returnDateLabel;
    private boolean roundTrip;
    private List<TripResponseDTO> trips;
    private List<TripResponseDTO> returnTrips;
    private Long totalCount;
    private Long totalReturnCount;
    private boolean hasMore;
    private Integer currentPage;
    private Integer displayedCount;
    private String sort;
}
