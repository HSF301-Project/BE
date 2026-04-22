package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.account.response.UserProfileDTO;
import sp26.group.busticket.modules.dto.booking.request.BookingFormDTO;
import sp26.group.busticket.modules.dto.booking.request.StaffBookingRequestDTO;
import sp26.group.busticket.modules.dto.booking.response.MyTripResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.PaymentResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.TicketConfirmationDTO;
import sp26.group.busticket.modules.dto.booking.response.TicketDetailResponseDTO;
import sp26.group.busticket.modules.entity.Account;

import java.util.List;
import java.util.UUID;

public interface BookingService {
    UUID createBooking(UUID tripId, BookingFormDTO form, Account currentAccount);
    UUID createStaffBooking(UUID tripId, StaffBookingRequestDTO form, Account staffAccount);
    void linkGuestBookingsToAccount(Account account);
    PaymentResponseDTO getPaymentInfo(UUID bookingId, UUID accountId);
    void processPayment(UUID bookingId, String paymentMethod, UUID accountId);
    TicketConfirmationDTO getBookingSuccessInfo(UUID bookingId, UUID accountId);
    TicketConfirmationDTO getStaffBookingSuccessInfo(UUID bookingId, UUID staffId);
    List<MyTripResponseDTO> getMyTrips(UUID accountId, String tab);
    UserProfileDTO getUserProfile(Account account);
    UUID cancelBooking(UUID bookingId, UUID accountId);
    BookingFormDTO getBookingFormFromBooking(UUID bookingId, UUID accountId);
    TicketDetailResponseDTO getTicketDetailByBookingCode(String bookingCode, UUID accountId);
}
