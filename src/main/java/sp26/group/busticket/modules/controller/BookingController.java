package sp26.group.busticket.modules.controller;

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
import sp26.group.busticket.modules.dto.account.response.UserProfileDTO;
import sp26.group.busticket.modules.dto.booking.request.BookingFormDTO;
import sp26.group.busticket.modules.dto.booking.response.MyTripResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.SeatDisplayDTO;
import sp26.group.busticket.modules.dto.trip.response.TripBookingResponseDTO;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.entity.Trip;
import sp26.group.busticket.modules.mapper.TripMapper;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.repository.TripRepository;
import sp26.group.busticket.modules.service.BookingService;
import sp26.group.busticket.modules.service.SeatService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final TripRepository tripRepository;
    private final SeatService seatService;
    private final TripMapper tripMapper;
    private final BookingService bookingService;
    private final AccountRepository accountRepository;

    @GetMapping("/{tripId}")
    public String showChooseSeat(@PathVariable UUID tripId, Model model) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        List<SeatDisplayDTO> lowerDeckSeats = seatService.getSeatsByTripAndFloor(tripId, 1);
        List<SeatDisplayDTO> upperDeckSeats = seatService.getSeatsByTripAndFloor(tripId, 2);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM • HH:mm");
        
        TripBookingResponseDTO tripDTO = tripMapper.toTripBookingResponseDTO(trip);
        tripDTO.setDepartureDateTimeLabel(trip.getDepartureTime().format(dateTimeFormatter));
        tripDTO.setArrivalDateTimeLabel(trip.getArrivalTime().format(dateTimeFormatter));
        
        model.addAttribute("trip", tripDTO);
        model.addAttribute("unitPrice", trip.getPriceBase());
        model.addAttribute("lowerDeckSeats", lowerDeckSeats);
        model.addAttribute("upperDeckSeats", upperDeckSeats);
        model.addAttribute("bookingForm", new BookingFormDTO());

        // Thêm thông tin user vào model để FE tự động điền
        try {
            Account currentAccount = getCurrentAccount();
            model.addAttribute("currentUser", currentAccount);
        } catch (AppException e) {
            // User chưa login, bỏ qua
        }

        return "Passenger/basic/choose_seat";
    }

    @PostMapping("/confirm")
    public String confirmBooking(@ModelAttribute("bookingForm") BookingFormDTO form,
                                 @RequestParam UUID tripId,
                                 Model model) {
        try {
            Account currentAccount = getCurrentAccount();
            UUID bookingId = bookingService.createBooking(tripId, form, currentAccount);
            return "redirect:/booking/payment?bookingId=" + bookingId;
        } catch (AppException e) {
            // Nếu có lỗi (ví dụ trùng ghế), quay lại trang chọn ghế và hiển thị thông báo
            Trip trip = tripRepository.findById(tripId).orElseThrow();
            List<SeatDisplayDTO> lowerDeckSeats = seatService.getSeatsByTripAndFloor(tripId, 1);
            List<SeatDisplayDTO> upperDeckSeats = seatService.getSeatsByTripAndFloor(tripId, 2);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM • HH:mm");
            TripBookingResponseDTO tripDTO = tripMapper.toTripBookingResponseDTO(trip);
            tripDTO.setDepartureDateTimeLabel(trip.getDepartureTime().format(dateTimeFormatter));
            tripDTO.setArrivalDateTimeLabel(trip.getArrivalTime().format(dateTimeFormatter));

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
        model.addAttribute("payment", bookingService.getPaymentInfo(bookingId));
        return "Passenger/basic/payment";
    }

    @PostMapping("/payment/process")
    public String processPayment(@RequestParam UUID bookingId, 
                                @RequestParam String paymentMethod, 
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.processPayment(bookingId, paymentMethod);
            return "redirect:/booking/success?bookingId=" + bookingId;
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/home";
        }
    }

    @GetMapping("/success")
    public String showSuccess(@RequestParam UUID bookingId, Model model) {
        model.addAttribute("ticket", bookingService.getBookingSuccessInfo(bookingId));
        return "Passenger/basic/comfirm_payment";
    }

    @GetMapping("/my-trips")
    public String showMyTrips(@RequestParam(required = false, defaultValue = "upcoming") String tab, Model model) {
        Account currentAccount = getCurrentAccount();
        model.addAttribute("user", bookingService.getUserProfile(currentAccount));
        model.addAttribute("trips", bookingService.getMyTrips(currentAccount.getId(), tab));
        model.addAttribute("activeTab", tab);
        return "Passenger/basic/my_trip";
    }

    private Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Vui lòng đăng nhập để tiếp tục");
        }
        return accountRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
