package sp26.group.busticket.modules.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.modules.enumType.TripStatusEnum;
import sp26.group.busticket.modules.repository.BookingRepository;
import sp26.group.busticket.modules.repository.TripRepository;
import sp26.group.busticket.modules.repository.PaymentRepository;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final PaymentRepository paymentRepository;
    private final NumberFormat vnCurrency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public Map<String, Object> getGeneralReport() {
        long totalBookings = bookingRepository.count();
        long activeTrips = tripRepository.countByTripStatus(TripStatusEnum.DEPARTED);
        
        BigDecimal totalRevenue = paymentRepository.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        BigDecimal monthlyRevenue = paymentRepository.sumMonthlyRevenue(now.getMonthValue(), now.getYear());
        if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;
        
        List<Map<String, Object>> topRoutes = bookingRepository.getTopRoutesByBookingCount().stream()
            .limit(5)
            .map(row -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("name", row[0]);
                map.put("count", row[1]);
                map.put("revenue", vnCurrency.format(row[2] != null ? row[2] : BigDecimal.ZERO));
                return map;
            })
            .collect(Collectors.toList());

        List<Map<String, Object>> monthlyStats = paymentRepository.getMonthlyRevenueStats(now.getYear()).stream()
            .map(row -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("month", "Th " + row[0]);
                map.put("revenue", ((BigDecimal) row[1]).divide(BigDecimal.valueOf(1000000), 1, java.math.RoundingMode.HALF_UP).doubleValue());
                return map;
            })
            .collect(Collectors.toList());

        // Fill in missing months if needed (optional, for better chart)
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("totalRevenue", vnCurrency.format(totalRevenue));
        result.put("monthlyRevenue", vnCurrency.format(monthlyRevenue));
        result.put("totalBookings", totalBookings);
        result.put("activeTrips", activeTrips);
        result.put("topRoutes", topRoutes);
        result.put("monthlyStats", monthlyStats);
        
        return result;
    }
}
