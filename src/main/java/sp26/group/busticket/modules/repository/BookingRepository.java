package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.Booking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUser_IdOrderByCreatedAtDesc(UUID userId);
    long countByUser_Id(UUID userId);
    Optional<Booking> findByBookingCode(String bookingCode);

    @Query("SELECT CONCAT(b.trip.route.departureLocation.name, ' → ', b.trip.route.arrivalLocation.name), " +
           "COUNT(b), SUM(b.totalAmount) " +
           "FROM Booking b " +
           "WHERE b.status = 'CONFIRMED' OR b.status = 'COMPLETED' " +
           "GROUP BY b.trip.route.departureLocation.name, b.trip.route.arrivalLocation.name " +
           "ORDER BY COUNT(b) DESC")
    List<Object[]> getTopRoutesByBookingCount();
}
