package sp26.group.busticket.modules.dto.trip;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TripStatsResponseDTO {
    private long todayTrips;         // Count trips where departureTime is Today
    private Double tripGrowthPercent; // % so với hôm qua

    private int totalSeats;          // Sum capacity của tất cả các xe chạy hôm nay

    private double avgFillRate;      // Trung bình cộng fillPercent các chuyến
    private String fillRateLevel;    // "Cao", "Thấp" dựa trên avgFillRate

    private int alertCount;          // Số chuyến có trạng thái DA_HUY hoặc chưa có tài xế
}