package sp26.group.busticket.modules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sp26.group.busticket.modules.dto.booking.request.BookingRequestDTO;
import sp26.group.busticket.modules.dto.booking.response.AvailableSeatsResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.BookingResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.TripDetailResponseDTO;
import sp26.group.busticket.modules.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/trips/{tripId}")
    public ResponseEntity<TripDetailResponseDTO> getTripDetails(@PathVariable Integer tripId) {
        return ResponseEntity.ok(bookingService.getTripDetails(tripId));
    }

    @GetMapping("/trips/{tripId}/seats")
    public ResponseEntity<AvailableSeatsResponseDTO> getAvailableSeats(@PathVariable Integer tripId) {
        return ResponseEntity.ok(bookingService.getAvailableSeats(tripId));
    }

    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Valid @RequestBody BookingRequestDTO request) {
        return ResponseEntity.ok(bookingService.createBooking(null, request));
    }

    @PostMapping("/{bookingId}/payment")
    public ResponseEntity<BookingResponseDTO> processPayment(
            @PathVariable Integer bookingId,
            @RequestParam String paymentMethod) {
        return ResponseEntity.ok(bookingService.processPayment(bookingId, paymentMethod));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDTO> getBookingDetails(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(bookingService.getBookingDetails(bookingId));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getBookingsByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(bookingService.getBookingsByPhone(phone));
    }
}
