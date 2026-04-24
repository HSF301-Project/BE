package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.modules.service.BookingService;
import sp26.group.busticket.modules.service.FinanceService;
import sp26.group.busticket.modules.service.ReportService;
import sp26.group.busticket.modules.service.TripService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BookingService bookingService;
    private final TripService tripService;
    private final FinanceService financeService;
    private final sp26.group.busticket.modules.repository.TicketRepository ticketRepository;
    private final sp26.group.busticket.modules.repository.TripRepository tripRepository;
    
    private final NumberFormat vnCurrency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    public Map<String, Object> getGeneralReport(Integer year, Integer month) {
        long totalBookings = bookingService.countTotalBookings();
        long activeTrips = tripService.countActiveTrips();
        
        BigDecimal totalRevenue = financeService.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        
        BigDecimal monthlyRevenue = financeService.sumMonthlyRevenue(targetMonth, targetYear);
        if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;
        
        List<Map<String, Object>> topRoutes = bookingService.getTopRoutes().stream()
            .limit(5)
            .map(row -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("name", row[0]);
                map.put("count", row[1]);
                map.put("revenue", vnCurrency.format(row[2] != null ? row[2] : BigDecimal.ZERO));
                return map;
            })
            .collect(Collectors.toList());

        List<Map<String, Object>> monthlyStats = financeService.getMonthlyRevenueStats(targetYear);
        List<Map<String, Object>> dailyStats = financeService.getDailyRevenueStats(targetMonth, targetYear);
        
        long totalTickets = ticketRepository.count();
        Long totalCapacity = tripRepository.sumTotalCapacity();
        double occupancyRate = 0.0;
        if (totalCapacity != null && totalCapacity > 0) {
            occupancyRate = (double) totalTickets / totalCapacity * 100.0;
        }
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalRevenue", vnCurrency.format(totalRevenue));
        result.put("monthlyRevenue", vnCurrency.format(monthlyRevenue));
        result.put("totalBookings", totalBookings);
        result.put("activeTrips", activeTrips);
        result.put("topRoutes", topRoutes);
        result.put("monthlyStats", monthlyStats);
        result.put("dailyStats", dailyStats);
        result.put("occupancyRate", String.format("%.1f", occupancyRate) + "%");
        
        return result;
    }
}
