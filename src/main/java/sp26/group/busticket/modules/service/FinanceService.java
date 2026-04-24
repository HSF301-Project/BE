package sp26.group.busticket.modules.service;

import org.springframework.data.domain.Page;
import sp26.group.busticket.modules.dto.finance.response.FinanceDashboardResponseDTO;
import sp26.group.busticket.modules.dto.finance.response.TransactionResponseDTO;

import java.math.BigDecimal;
import java.util.UUID;

public interface FinanceService {
    FinanceDashboardResponseDTO getFinanceDashboardData();
    Page<TransactionResponseDTO> getTransactions(String query, int page, int size);
    BigDecimal sumTotalRevenue();
    BigDecimal sumMonthlyRevenue(int month, int year);
    java.util.List<java.util.Map<String, Object>> getMonthlyRevenueStats(int year);
    java.util.List<java.util.Map<String, Object>> getDailyRevenueStats(int month, int year);
}
