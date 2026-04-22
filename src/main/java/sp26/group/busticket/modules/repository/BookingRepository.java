package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.modules.entity.Booking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUser_IdOrderByCreatedAtDesc(UUID userId);
    long countByUser_Id(UUID userId);
    Optional<Booking> findByBookingCode(String bookingCode);
}
