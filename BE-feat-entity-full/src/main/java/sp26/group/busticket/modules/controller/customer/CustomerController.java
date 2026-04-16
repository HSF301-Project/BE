package sp26.group.busticket.modules.controller.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp26.group.busticket.modules.dto.booking.request.BookingRequestDTO;
import sp26.group.busticket.modules.dto.booking.response.AvailableSeatsResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.BookingResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.TripDetailResponseDTO;
import sp26.group.busticket.modules.entity.AccountEntity;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.repository.LocationRepository;
import sp26.group.busticket.modules.repository.RouteRepository;
import sp26.group.busticket.modules.repository.TripRepository;
import sp26.group.busticket.modules.service.BookingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class CustomerController {

    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final LocationRepository locationRepository;
    private final BookingService bookingService;
    private final AccountRepository accountRepository;

    @GetMapping("/search")
    public String searchForm(Model model) {
        model.addAttribute("routes", routeRepository.findAllWithLocations());
        model.addAttribute("locations", locationRepository.findAll());
        return "customer/booking-search";
    }

    @GetMapping("/trips")
    public String searchTrips(
            @RequestParam Integer departureId,
            @RequestParam Integer arrivalId,
            @RequestParam LocalDate date,
            Model model) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        var trips = tripRepository.findByDepartureTimeBetween(start, end);
        
        final Integer depId = departureId;
        final Integer arrId = arrivalId;
        
        trips = trips.stream()
                .filter(t -> t.getRoute().getDepartureLocation().getId().equals(depId)
                        && t.getRoute().getArrivalLocation().getId().equals(arrId))
                .toList();

        model.addAttribute("trips", trips);
        model.addAttribute("departureId", departureId);
        model.addAttribute("arrivalId", arrivalId);
        model.addAttribute("selectedDate", date);
        return "customer/booking-trips";
    }

    @GetMapping("/trip/{tripId}/seats")
    public String selectSeats(@PathVariable Integer tripId, Model model) {
        TripDetailResponseDTO tripDetail = bookingService.getTripDetails(tripId);
        AvailableSeatsResponseDTO availableSeats = bookingService.getAvailableSeats(tripId);
        
        model.addAttribute("trip", tripDetail);
        model.addAttribute("availableSeats", availableSeats);
        return "customer/booking-seats";
    }

    @PostMapping("/trip/{tripId}/booking")
    public String createBooking(
            @PathVariable Integer tripId,
            @RequestParam List<Integer> seatIds,
            @RequestParam List<String> passengerNames,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String phone,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            AccountEntity user = null;
            if (phone != null && !phone.isEmpty()) {
                user = accountRepository.findByPhone(phone).orElse(null);
            }

            List<BookingRequestDTO.PassengerInfoDTO> passengers = new ArrayList<>();
            for (int i = 0; i < seatIds.size(); i++) {
                BookingRequestDTO.PassengerInfoDTO passenger = new BookingRequestDTO.PassengerInfoDTO();
                passenger.setSeatId(seatIds.get(i));
                passenger.setPassengerName(passengerNames.get(i));
                passengers.add(passenger);
            }

            BookingRequestDTO request = new BookingRequestDTO();
            request.setTripId(tripId);
            request.setPassengers(passengers);
            request.setPaymentMethod(paymentMethod);

            BookingResponseDTO booking = bookingService.createBooking(user, request);
            
            booking = bookingService.processPayment(booking.getBookingId(), paymentMethod);
            
            redirectAttributes.addFlashAttribute("booking", booking);
            return "redirect:/booking/confirmation/" + booking.getBookingId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/booking/trip/" + tripId + "/seats";
        }
    }

    @GetMapping("/confirmation/{bookingId}")
    public String confirmation(@PathVariable Integer bookingId, Model model) {
        BookingResponseDTO booking = bookingService.getBookingDetails(bookingId);
        model.addAttribute("booking", booking);
        return "customer/booking-confirmation";
    }

    @GetMapping("/my-tickets")
    public String myTickets(@RequestParam String phone, Model model) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByPhone(phone);
        model.addAttribute("bookings", bookings);
        model.addAttribute("phone", phone);
        return "customer/my-tickets";
    }
}
