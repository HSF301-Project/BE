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
    @Transactional
    public void checkInTicket(String ticketCode, UUID tripId) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy vé với mã này."));

        // Kiểm tra vé có thuộc chuyến đi này không
        if (!ticket.getBooking().getTrip().getId().equals(tripId)) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Vé này không thuộc chuyến đi hiện tại.");
        }

        // Kiểm tra trạng thái Booking (phải đã thanh toán/xác nhận)
        if (ticket.getBooking().getStatus() != BookingStatusEnum.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Booking chưa được xác nhận hoặc đã bị hủy.");
        }

        // Kiểm tra xem đã check-in chưa
        if ("CHECKED_IN".equals(ticket.getStatus())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Vé này đã được check-in trước đó.");
        }

        // Cập nhật trạng thái
        ticket.setStatus("CHECKED_IN");
        ticketRepository.save(ticket);
    }
}
