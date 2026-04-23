package sp26.group.busticket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.dto.booking.request.BookingFormDTO;
import sp26.group.busticket.dto.booking.response.SeatDisplayDTO;
import sp26.group.busticket.service.BookingService;
import sp26.group.busticket.service.SeatService;
import sp26.group.busticket.service.TripService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp26.group.busticket.service.AccountService;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class BookingController {

    private final SeatService seatService;
    private final BookingService bookingService;
    private final TripService tripService;
    private final AccountService accountService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @GetMapping("/{tripId}")
    public String showChooseSeat(@PathVariable UUID tripId, Model model) {
        var trip = tripService.getTripEntityById(tripId);
        
        List<SeatDisplayDTO> lowerDeckSeats = seatService.getSeatsByTripAndFloor(tripId, 1);
        List<SeatDisplayDTO> upperDeckSeats = seatService.getSeatsByTripAndFloor(tripId, 2);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM • HH:mm");
        
        var tripDTO = tripService.getTripBookingData(tripId);
        
        model.addAttribute("trip", tripDTO);
        model.addAttribute("unitPrice", trip.getPriceBase());
        model.addAttribute("lowerDeckSeats", lowerDeckSeats);
        model.addAttribute("upperDeckSeats", upperDeckSeats);
        
        if (!model.containsAttribute("bookingForm")) {
            model.addAttribute("bookingForm", new BookingFormDTO());
        }

        try {
            String email = getCurrentUserEmail();
            var currentAccount = accountService.getAccountByEmail(email);
            model.addAttribute("currentUser", currentAccount);
        } catch (AppException e) {
        }

        return "Passenger/basic/choose_seat";
    }

    @PostMapping("/confirm")
    public String confirmBooking(@ModelAttribute("bookingForm") BookingFormDTO form,
                                 @RequestParam UUID tripId,
                                 Model model) {
        try {
            String email = getCurrentUserEmail();
            var currentAccount = accountService.getAccountByEmail(email);
            // We need the entity for bookingService.createBooking
            // But wait, the service should handle finding the entity.
            // I'll update bookingService.createBooking to take email.
            UUID bookingId = bookingService.createBookingByEmail(tripId, form, email);
            return "redirect:/booking/payment?bookingId=" + bookingId;
        } catch (AppException e) {
            var trip = tripService.getTripEntityById(tripId);
            List<SeatDisplayDTO> lowerDeckSeats = seatService.getSeatsByTripAndFloor(tripId, 1);
            List<SeatDisplayDTO> upperDeckSeats = seatService.getSeatsByTripAndFloor(tripId, 2);
            
            var tripDTO = tripService.getTripBookingData(tripId);

            model.addAttribute("trip", tripDTO);
            model.addAttribute("unitPrice", trip.getPriceBase());
            model.addAttribute("lowerDeckSeats", lowerDeckSeats);
            model.addAttribute("upperDeckSeats", upperDeckSeats);
            model.addAttribute("bookingForm", form);
            model.addAttribute("errorMessage", e.getMessage());
            return "Passenger/basic/choose_seat";
        }
    }

    @GetMapping("/payment")
    public String showPayment(@RequestParam UUID bookingId, Model model) {
        String email = getCurrentUserEmail();
        var account = accountService.getAccountByEmail(email);
        model.addAttribute("payment", bookingService.getPaymentInfo(bookingId, account.getId()));
        return "Passenger/basic/payment";
    }

    @PostMapping("/payment/process")
    public String processPayment(@RequestParam UUID bookingId, 
                                @RequestParam String paymentMethod, 
                                RedirectAttributes redirectAttributes) {
        try {
            String email = getCurrentUserEmail();
            var account = accountService.getAccountByEmail(email);
            bookingService.processPayment(bookingId, paymentMethod, account.getId());
            return "redirect:/booking/success?bookingId=" + bookingId;
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/home";
        }
    }

    @GetMapping("/success")
    public String showSuccess(@RequestParam UUID bookingId, Model model) {
        String email = getCurrentUserEmail();
        var account = accountService.getAccountByEmail(email);
        model.addAttribute("ticket", bookingService.getBookingSuccessInfo(bookingId, account.getId()));
        return "Passenger/basic/comfirm_payment";
    }

    @GetMapping("/my-trips")
    public String showMyTrips(@RequestParam(required = false, defaultValue = "upcoming") String tab, Model model) {
        String email = getCurrentUserEmail();
        var account = accountService.getAccountByEmail(email);
        // UserProfileDTO might need account entity or just email
        // I'll update bookingService.getUserProfile to take email or just use the DTO
        model.addAttribute("user", bookingService.getUserProfileByEmail(email));
        model.addAttribute("trips", bookingService.getMyTrips(account.getId(), tab));
        model.addAttribute("activeTab", tab);
        return "Passenger/basic/my_trip";
    }

    @GetMapping("/roundtrip")
    public String showChooseSeatRoundtrip(@RequestParam UUID outboundId, @RequestParam UUID returnId, Model model) {
        var outbound = tripService.getTripEntityById(outboundId);
        var returnTrip = tripService.getTripEntityById(returnId);

        var outboundDTO = tripService.getTripBookingData(outboundId);
        var returnDTO = tripService.getTripBookingData(returnId);

        model.addAttribute("outboundTrip", outboundDTO);
        model.addAttribute("returnTrip", returnDTO);
        model.addAttribute("outboundLowerDeck", seatService.getSeatsByTripAndFloor(outboundId, 1));
        model.addAttribute("outboundUpperDeck", seatService.getSeatsByTripAndFloor(outboundId, 2));
        model.addAttribute("returnLowerDeck", seatService.getSeatsByTripAndFloor(returnId, 1));
        model.addAttribute("returnUpperDeck", seatService.getSeatsByTripAndFloor(returnId, 2));
        model.addAttribute("outboundUnitPrice", outbound.getPriceBase());
        model.addAttribute("returnUnitPrice", returnTrip.getPriceBase());

        BookingFormDTO form = new BookingFormDTO();
        form.setRoundTrip(true);
        form.setReturnTripId(returnId);
        model.addAttribute("bookingForm", form);

        try {
            String email = getCurrentUserEmail();
            var currentAccount = accountService.getAccountByEmail(email);
            model.addAttribute("currentUser", currentAccount);
        } catch (AppException e) {}

        return "Passenger/basic/choose_seat_roundtrip";
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Vui lòng đăng nhập để tiếp tục");
        }
        return auth.getName();
    }

    @PostMapping("/{bookingId}/cancel")
    public String cancelBooking(@PathVariable UUID bookingId, 
                               @RequestParam(required = false, defaultValue = "false") boolean isPaymentPage,
                               RedirectAttributes redirectAttributes) {
        try {
            String email = getCurrentUserEmail();
            var account = accountService.getAccountByEmail(email);
            
            if (isPaymentPage) {
                 BookingFormDTO prefilledForm = bookingService.getBookingFormFromBooking(bookingId, account.getId());
                 UUID tripId = bookingService.cancelBooking(bookingId, account.getId());
                 
                 try {
                     String passengersJson = objectMapper.writeValueAsString(prefilledForm.getPassengers());
                     redirectAttributes.addFlashAttribute("prefilledPassengersJson", passengersJson);
                 } catch (Exception e) {
                     log.error("Error converting passengers to JSON", e);
                 }

                 redirectAttributes.addFlashAttribute("bookingForm", prefilledForm);
                 redirectAttributes.addFlashAttribute("successMessage", "Đã hủy giao dịch thanh toán. Bạn có thể chọn lại ghế hoặc chỉnh sửa thông tin.");
                 return "redirect:/booking/" + tripId;
             } else {
                bookingService.cancelBooking(bookingId, account.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Hủy đặt vé thành công!");
                return "redirect:/booking/my-trips";
            }
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/booking/my-trips";
        }
    }

    @GetMapping("/ticket/{bookingCode}")
    public String viewTicket(@PathVariable String bookingCode, Model model) {
        try {
            String email = getCurrentUserEmail();
            var account = accountService.getAccountByEmail(email);
            var ticketDetail = bookingService.getTicketDetailByBookingCode(bookingCode, account.getId());
            model.addAttribute("ticket", ticketDetail);
            return "Passenger/basic/view_ticket";
        } catch (AppException e) {
            return "redirect:/booking/my-trips";
        }
    }
}
