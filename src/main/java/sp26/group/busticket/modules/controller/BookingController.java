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
import sp26.group.busticket.modules.service.TripService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class BookingController {

    private final TripRepository tripRepository;
    private final SeatService seatService;
    private final TripMapper tripMapper;
    private final BookingService bookingService;
    private final AccountRepository accountRepository;
    private final TripService tripService;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

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
        tripDTO.setStopEtas(tripService.getTripStopEtas(tripId));
        tripDTO.setExpired(trip.getDepartureTime().minusHours(1).isBefore(java.time.LocalDateTime.now()));
        
        model.addAttribute("trip", tripDTO);
        model.addAttribute("unitPrice", trip.getPriceBase());
        model.addAttribute("lowerDeckSeats", lowerDeckSeats);
        model.addAttribute("upperDeckSeats", upperDeckSeats);
        
        // Chỉ thêm mới nếu chưa có từ Flash Attribute (lúc hủy thanh toán)
        if (!model.containsAttribute("bookingForm")) {
            model.addAttribute("bookingForm", new BookingFormDTO());
        }

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
            tripDTO.setStopEtas(tripService.getTripStopEtas(tripId));
            tripDTO.setExpired(trip.getDepartureTime().minusHours(1).isBefore(java.time.LocalDateTime.now()));

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
        Account currentAccount = getCurrentAccount();
        model.addAttribute("payment", bookingService.getPaymentInfo(bookingId, currentAccount.getId()));
        return "Passenger/basic/payment";
    }

    @PostMapping("/payment/process")
    public String processPayment(@RequestParam UUID bookingId, 
                                @RequestParam String paymentMethod, 
                                RedirectAttributes redirectAttributes) {
        try {
            Account currentAccount = getCurrentAccount();
            bookingService.processPayment(bookingId, paymentMethod, currentAccount.getId());
            return "redirect:/booking/success?bookingId=" + bookingId;
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/home";
        }
    }

    @GetMapping("/success")
    public String showSuccess(@RequestParam UUID bookingId, Model model) {
        Account currentAccount = getCurrentAccount();
        model.addAttribute("ticket", bookingService.getBookingSuccessInfo(bookingId, currentAccount.getId()));
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

    @GetMapping("/roundtrip")
    public String showChooseSeatRoundtrip(@RequestParam UUID outboundId, @RequestParam UUID returnId, Model model) {
        Trip outbound = tripRepository.findById(outboundId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
        Trip returnTrip = tripRepository.findById(returnId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, dd/MM • HH:mm");
        DateTimeFormatter dateLabelFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", new java.util.Locale("vi", "VN"));

        // Outbound Info
        TripBookingResponseDTO outboundDTO = tripMapper.toTripBookingResponseDTO(outbound);
        outboundDTO.setDepartureDateTimeLabel(outbound.getDepartureTime().format(dateTimeFormatter));
        outboundDTO.setArrivalDateTimeLabel(outbound.getArrivalTime().format(dateTimeFormatter));
        var outboundStops = tripService.getTripStopEtas(outboundId);
        outboundDTO.setStopEtas(outboundStops);
        try {
            outboundDTO.setStopEtasJson(objectMapper.writeValueAsString(outboundStops));
        } catch (Exception e) {
            log.error("Error converting outbound stops to JSON", e);
            outboundDTO.setStopEtasJson("[]");
        }
        outboundDTO.setExpired(outbound.getDepartureTime().minusHours(1).isBefore(java.time.LocalDateTime.now()));

        // Return Info
        TripBookingResponseDTO returnDTO = tripMapper.toTripBookingResponseDTO(returnTrip);
        returnDTO.setDepartureDateTimeLabel(returnTrip.getDepartureTime().format(dateTimeFormatter));
        returnDTO.setArrivalDateTimeLabel(returnTrip.getArrivalTime().format(dateTimeFormatter));
        var returnStops = tripService.getTripStopEtas(returnId);
        returnDTO.setStopEtas(returnStops);
        try {
            returnDTO.setStopEtasJson(objectMapper.writeValueAsString(returnStops));
        } catch (Exception e) {
            log.error("Error converting return stops to JSON", e);
            returnDTO.setStopEtasJson("[]");
        }
        returnDTO.setExpired(returnTrip.getDepartureTime().minusHours(1).isBefore(java.time.LocalDateTime.now()));

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
            Account currentAccount = getCurrentAccount();
            model.addAttribute("currentUser", currentAccount);
        } catch (AppException e) {}

        return "Passenger/basic/choose_seat_roundtrip";
    }

    private Account getCurrentAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "Vui lòng đăng nhập để tiếp tục");
        }
        return accountRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @PostMapping("/{bookingId}/cancel")
    public String cancelBooking(@PathVariable UUID bookingId, 
                               @RequestParam(required = false, defaultValue = "false") boolean isPaymentPage,
                               RedirectAttributes redirectAttributes) {
        try {
            Account currentAccount = getCurrentAccount();
            
            if (isPaymentPage) {
                 // Lấy thông tin form trước khi hủy để người dùng có thể chỉnh sửa lại
                 BookingFormDTO prefilledForm = bookingService.getBookingFormFromBooking(bookingId, currentAccount.getId());
                 UUID tripId = bookingService.cancelBooking(bookingId, currentAccount.getId());
                 
                 // Chuyển đổi sang JSON ngay tại đây để View sử dụng trực tiếp
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
                bookingService.cancelBooking(bookingId, currentAccount.getId());
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
            Account currentAccount = getCurrentAccount();
            var ticketDetail = bookingService.getTicketDetailByBookingCode(bookingCode, currentAccount.getId());
            model.addAttribute("ticket", ticketDetail);
            return "Passenger/basic/view_ticket";
        } catch (AppException e) {
            return "redirect:/booking/my-trips";
        }
    }
}
