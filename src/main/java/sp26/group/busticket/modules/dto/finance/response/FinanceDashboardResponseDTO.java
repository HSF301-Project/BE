package sp26.group.busticket.modules.dto.finance.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class FinanceDashboardResponseDTO {
    private String todayRevenueFormatted;
    private Double growthPercent;
    private Double successRate;
    private String successLabel;
    private Double failureRate;
    private Long failedCount;
    private List<GatewayDTO> topGateways;
    private String forecastLabel;
    private List<ChartBarDTO> chartBars;

    @Getter
    @Builder
    public static class GatewayDTO {
        private String name;
        private Double percent;
    }

    @Getter
    @Builder
    public static class ChartBarDTO {
        private boolean highlighted;
        private Double heightPercent;
    }
}
