package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.entity.Ticket;
import sp26.group.busticket.modules.enumType.BookingStatusEnum;
import sp26.group.busticket.modules.repository.TicketRepository;
import sp26.group.busticket.modules.service.TicketService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public long countTicketsByTrip(UUID tripId) {
        return ticketRepository.countByBooking_Trip_Id(tripId);
    }

    @Override
    public long countCheckedInTicketsByTrip(UUID tripId) {
        return ticketRepository.countByBooking_Trip_IdAndStatus(tripId, "CHECKED_IN");
    }

    @Override
    @Transactional
    public void checkInTicket(String ticketCode, UUID tripId) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy vé với mã này."));

        if (!ticket.getBooking().getTrip().getId().equals(tripId)) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Vé này không thuộc chuyến đi hiện tại.");
        }

        if (ticket.getBooking().getStatus() != BookingStatusEnum.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Booking chưa được xác nhận hoặc đã bị hủy.");
        }

        if ("CHECKED_IN".equals(ticket.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Vé này đã được check-in trước đó.");
        }

        ticket.setStatus("CHECKED_IN");
        ticketRepository.save(ticket);
    }

    @Override
    @Transactional
    public void toggleCheckIn(UUID tripId, String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy vé."));

        if (!ticket.getBooking().getTrip().getId().equals(tripId)) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Vé không thuộc chuyến đi này.");
        }

        if ("CHECKED_IN".equals(ticket.getStatus())) {
            ticket.setStatus("PENDING");
        } else {
            ticket.setStatus("CHECKED_IN");
        }
        ticketRepository.save(ticket);
    }
}
