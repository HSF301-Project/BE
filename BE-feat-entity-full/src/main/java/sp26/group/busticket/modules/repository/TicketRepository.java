package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.TicketEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Integer> {
    
    List<TicketEntity> findByBookingId(Integer bookingId);
    
    Optional<TicketEntity> findByTicketCode(String ticketCode);
    
    @Query("SELECT t FROM TicketEntity t JOIN FETCH t.booking b " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.trip tr " +
           "LEFT JOIN FETCH tr.route r " +
           "LEFT JOIN FETCH r.departureLocation " +
           "LEFT JOIN FETCH r.arrivalLocation " +
           "LEFT JOIN FETCH tr.coach " +
           "LEFT JOIN FETCH t.seat " +
           "WHERE t.ticketCode = :ticketCode")
    Optional<TicketEntity> findByTicketCodeWithDetails(@Param("ticketCode") String ticketCode);
    
    @Query("SELECT DISTINCT t FROM TicketEntity t JOIN FETCH t.booking b " +
           "LEFT JOIN FETCH b.user u " +
           "LEFT JOIN FETCH b.trip tr " +
           "LEFT JOIN FETCH tr.route r " +
           "LEFT JOIN FETCH r.departureLocation " +
           "LEFT JOIN FETCH r.arrivalLocation " +
           "LEFT JOIN FETCH tr.coach " +
           "LEFT JOIN FETCH t.seat " +
           "WHERE u.phone = :phone OR u IS NULL")
    List<TicketEntity> findByAccountPhoneWithDetails(@Param("phone") String phone);
}
