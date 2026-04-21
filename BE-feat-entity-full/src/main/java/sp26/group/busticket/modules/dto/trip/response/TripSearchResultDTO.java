package sp26.group.busticket.modules.dto.trip.response;

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
    private List<TripResponseDTO> trips;
    private Long totalCount;
    private boolean hasMore;
    private Integer currentPage;
    private Integer displayedCount;
    private String sort;
}
