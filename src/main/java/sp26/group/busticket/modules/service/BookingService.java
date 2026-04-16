package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.account.response.UserProfileDTO;
import sp26.group.busticket.modules.dto.booking.request.BookingFormDTO;
import sp26.group.busticket.modules.dto.booking.response.MyTripResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.PaymentResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.TicketConfirmationDTO;
import sp26.group.busticket.modules.entity.Account;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    UUID createBooking(UUID tripId, BookingFormDTO form, Account currentAccount);
    PaymentResponseDTO getPaymentInfo(UUID bookingId);
    void processPayment(UUID bookingId, String paymentMethod);
    TicketConfirmationDTO getBookingSuccessInfo(UUID bookingId);
    List<MyTripResponseDTO> getMyTrips(UUID accountId, String tab);
    UserProfileDTO getUserProfile(Account account);
}
