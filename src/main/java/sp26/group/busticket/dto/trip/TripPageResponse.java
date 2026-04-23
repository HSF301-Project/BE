package sp26.group.busticket.dto.trip;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TripPageResponse {
    private TripStatsResponseDTO stats;
    private List<TripResponseDTO> trips;

    // Thông tin phân trang
    private int currentPage;
    private long totalCount;
    private int totalPages;
    private boolean hasNext;
    private int displayedCount;

    // Thông tin Admin (Sidebar)
    private String adminFullName;
    private String adminAvatar;
}