package sp26.group.busticket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.enumType.TripStatusEnum;
import sp26.group.busticket.repository.BookingRepository;
import sp26.group.busticket.repository.TripRepository;
import sp26.group.busticket.repository.PaymentRepository;
import sp26.group.busticket.entity.Booking;
import sp26.group.busticket.enumType.BookingStatusEnum;

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
        
        List<Booking> confirmedBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatusEnum.CONFIRMED || b.getStatus() == BookingStatusEnum.COMPLETED)
                .toList();

        List<Map<String, Object>> topRoutes = confirmedBookings.stream()
            .collect(Collectors.groupingBy(
                b -> b.getTrip().getRoute().getRouteCode(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> {
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("name", list.get(0).getTrip().getRoute().getRouteCode());
                        map.put("count", (long) list.size());
                        BigDecimal total = list.stream()
                                .map(Booking::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        map.put("revenue", vnCurrency.format(total));
                        map.put("bookingCount", (long) list.size());
                        return map;
                    }
                )
            ))
            .values().stream()
            .sorted((m1, m2) -> Long.compare((long) m2.get("bookingCount"), (long) m1.get("bookingCount")))
            .limit(5)
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
