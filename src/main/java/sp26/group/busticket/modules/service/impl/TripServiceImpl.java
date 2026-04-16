package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.modules.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.modules.dto.trip.response.TripResponseDTO;
import sp26.group.busticket.modules.dto.trip.response.TripSearchResultDTO;
import sp26.group.busticket.modules.entity.Trip;
import sp26.group.busticket.modules.enumType.BookingStatusEnum;
import sp26.group.busticket.modules.mapper.TripMapper;
import sp26.group.busticket.modules.repository.TicketRepository;
import sp26.group.busticket.modules.repository.TripRepository;
import sp26.group.busticket.modules.service.TripService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final TripMapper tripMapper;

    @Override
    public TripSearchResultDTO searchTrips(TripSearchRequestDTO request) {
        LocalDate date = LocalDate.parse(request.getDate());
        LocalDateTime now = LocalDateTime.now();
        
        // Chỉ lấy từ thời điểm hiện tại nếu ngày search là ngày hôm nay
        LocalDateTime searchStart = date.atStartOfDay();
        if (date.equals(now.toLocalDate())) {
            searchStart = now;
        }
        
        LocalDateTime searchEnd = date.atTime(LocalTime.MAX);

        List<Trip> trips = tripRepository.findByRoute_DepartureLocation_NameAndRoute_ArrivalLocation_NameAndDepartureTimeBetween(
                request.getFrom(), request.getTo(), searchStart, searchEnd);

        // Basic filtering by busType if present
        if (request.getBusType() != null && !request.getBusType().isEmpty()) {
            trips = trips.stream()
                    .filter(t -> t.getCoach().getCoachType().equalsIgnoreCase(request.getBusType()))
                    .collect(Collectors.toList());
        }

        // Basic filtering by maxPrice if present
        if (request.getMaxPrice() != null) {
            BigDecimal maxPrice = BigDecimal.valueOf(request.getMaxPrice() * 1000);
            trips = trips.stream()
                    .filter(t -> t.getPriceBase().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }

        Locale localeVN = new Locale("vi", "VN");
        NumberFormat vnFormat = NumberFormat.getCurrencyInstance(localeVN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<TripResponseDTO> tripDTOs = trips.stream()
                .map(trip -> {
                    TripResponseDTO dto = tripMapper.toTripResponseDTO(trip);
                    
                    // Tính số ghế trống
                    int totalSeats = trip.getCoach().getTotalSeats();
                    long bookedSeatsCount = ticketRepository.findByBooking_Trip_Id(trip.getId()).stream()
                            .filter(t -> t.getBooking().getStatus() == BookingStatusEnum.PENDING || 
                                        t.getBooking().getStatus() == BookingStatusEnum.CONFIRMED)
                            .count();
                    dto.setSeatsLeft(totalSeats - (int) bookedSeatsCount);

                    dto.setImageUrl("https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?auto=format&fit=crop&q=80&w=400");
                    dto.setFeatured(false);
                    dto.setRating(4.9);
                    dto.setAmenities("WiFi • Sạc • Nước uống");
                    dto.setPriceFormatted(vnFormat.format(trip.getPriceBase()));
                    dto.setDepartureTime(trip.getDepartureTime().format(timeFormatter));
                    dto.setArrivalTime(trip.getArrivalTime().format(timeFormatter));
                    dto.setDuration(calculateDuration(trip.getDepartureTime(), trip.getArrivalTime()));
                    return dto;
                })
                .collect(Collectors.toList());

        return TripSearchResultDTO.builder()
                .fromCity(request.getFrom())
                .toCity(request.getTo())
                .date(request.getDate())
                .dateLabel(date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")))
                .trips(tripDTOs)
                .totalCount((long) tripDTOs.size())
                .displayedCount(tripDTOs.size())
                .hasMore(false)
                .currentPage(0)
                .sort(request.getSort())
                .build();
    }

    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        long durationMinutes = java.time.Duration.between(start, end).toMinutes();
        long hours = durationMinutes / 60;
        long minutes = durationMinutes % 60;
        return String.format("%dh %02dm", hours, minutes);
    }
}
