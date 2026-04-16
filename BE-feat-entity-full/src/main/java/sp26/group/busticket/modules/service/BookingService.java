package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.booking.request.BookingRequestDTO;
import sp26.group.busticket.modules.dto.booking.response.AvailableSeatsResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.BookingResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.TripDetailResponseDTO;
import sp26.group.busticket.modules.entity.AccountEntity;

import java.util.List;

public interface BookingService {
    
    TripDetailResponseDTO getTripDetails(Integer tripId);
    
    AvailableSeatsResponseDTO getAvailableSeats(Integer tripId);
    
    BookingResponseDTO createBooking(AccountEntity user, BookingRequestDTO request);
    
    BookingResponseDTO processPayment(Integer bookingId, String paymentMethod);
    
    BookingResponseDTO getBookingDetails(Integer bookingId);
    
    List<BookingResponseDTO> getBookingsByPhone(String phone);
}
