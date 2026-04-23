package sp26.group.busticket.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.dto.booking.request.StaffBookingRequestDTO;
import sp26.group.busticket.service.BookingService;
import sp26.group.busticket.service.TripService;
import sp26.group.busticket.service.AccountService;

@Controller
@RequestMapping("/staff/booking")
@RequiredArgsConstructor
public class StaffBookingController {

    private final TripService tripService;
    private final BookingService bookingService;
    private final AccountService accountService;

    @GetMapping("/{tripId}")
    public String showStaffBooking(@PathVariable UUID tripId, Model model) {
        var tripData = tripService.getTripBookingData(tripId);
        model.addAttribute("trip", tripData);
        model.addAttribute("bookingRequest", new StaffBookingRequestDTO());
        return "staff/booking-form";
    }

    @PostMapping("/{tripId}")
    public String processStaffBooking(@PathVariable UUID tripId,
                                      @Valid @ModelAttribute("bookingRequest") StaffBookingRequestDTO form,
                                      BindingResult result,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("trip", tripService.getTripBookingData(tripId));
            return "staff/booking-form";
        }

        try {
            UUID bookingId = bookingService.createStaffBookingByEmail(tripId, form, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Đặt vé tại quầy thành công!");
            return "redirect:/staff/booking/success?bookingId=" + bookingId;
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("trip", tripService.getTripBookingData(tripId));
            return "staff/booking-form";
        }
    }

    @GetMapping("/success")
    public String showStaffSuccess(@RequestParam UUID bookingId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        var staff = accountService.getAccountByEmail(userDetails.getUsername());
        model.addAttribute("ticket", bookingService.getStaffBookingSuccessInfo(bookingId, staff.getId()));
        return "staff/booking-success";
    }
}
