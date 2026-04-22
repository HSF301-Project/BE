package sp26.group.busticket.modules.controller;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.booking.request.StaffBookingRequestDTO;
import sp26.group.busticket.modules.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.service.BookingService;
import sp26.group.busticket.modules.service.LocationService;
import sp26.group.busticket.modules.service.SeatService;
import sp26.group.busticket.modules.service.TripService;

@Controller
@RequestMapping("/staff/booking")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('STAFF')") // User rule says staff booking
public class StaffBookingController {

    private final BookingService bookingService;
    private final TripService tripService;
    private final SeatService seatService;
    private final LocationService locationService;
    private final AccountRepository accountRepository;

    // 1. Xem danh sách chuyến đi (Staff Dashboard)
    @GetMapping("/trips")
    public String listTrips(@ModelAttribute TripSearchRequestDTO searchDTO, Model model) {
        if (searchDTO.getDate() == null || searchDTO.getDate().isBlank()) {
            searchDTO.setDate(LocalDate.now().toString());
        }
        
        model.addAttribute("locations", locationService.getAllLocations());
        
        if (searchDTO.getFrom() == null || searchDTO.getTo() == null || searchDTO.getFrom().isBlank() || searchDTO.getTo().isBlank()) {
            model.addAttribute("trips", Collections.emptyList());
        } else {
            model.addAttribute("trips", tripService.searchTrips(searchDTO).getTrips());
        }
        return "staff/trip-list";
    }

    // 2. Chọn ghế và nhập thông tin khách
    @GetMapping("/create/{tripId}")
    public String showCreateForm(@PathVariable UUID tripId, Model model) {
        populateCreateFormModel(tripId, model);
        model.addAttribute("bookingDTO", new StaffBookingRequestDTO());
        return "staff/booking-form";
    }

    // 3. Xử lý đặt vé
    @PostMapping("/create/{tripId}")
    public String processBooking(@PathVariable UUID tripId,
                                 @Valid @ModelAttribute("bookingDTO") StaffBookingRequestDTO bookingDTO,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            populateCreateFormModel(tripId, model);
            return "staff/booking-form";
        }

        try {
            Account staff = accountRepository.findByEmail(userDetails.getUsername()).orElseThrow();
            UUID bookingId = bookingService.createStaffBooking(tripId, bookingDTO, staff);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt vé thành công! Mã Booking: " + bookingId);
            return "redirect:/staff/booking/success/" + bookingId;
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateCreateFormModel(tripId, model);
            return "staff/booking-form";
        }
    }

    private void populateCreateFormModel(UUID tripId, Model model) {
        BigDecimal unitPrice = tripService.getBasePriceByTripId(tripId);
        NumberFormat vnCurrency = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        model.addAttribute("tripId", tripId);
        model.addAttribute("lowerDeckSeats", seatService.getSeatsByTripAndFloor(tripId, 1));
        model.addAttribute("upperDeckSeats", seatService.getSeatsByTripAndFloor(tripId, 2));
        model.addAttribute("unitPrice", unitPrice);
        model.addAttribute("unitPriceFormatted", vnCurrency.format(unitPrice));
    }

    // 4. Màn hình thành công
    @GetMapping("/success/{bookingId}")
    public String showSuccess(@PathVariable UUID bookingId, 
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        Account staff = accountRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("ticket", bookingService.getBookingSuccessInfo(bookingId, staff.getId()));
        return "staff/booking-success";
    }
}
