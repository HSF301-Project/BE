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
    
    @Query("SELECT t FROM TicketEntity t JOIN FETCH t.booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.trip tr " +
           "JOIN FETCH tr.route r " +
           "JOIN FETCH r.departureLocation JOIN FETCH r.arrivalLocation " +
           "JOIN FETCH tr.coach JOIN FETCH t.seat " +
           "WHERE t.ticketCode = :ticketCode")
    Optional<TicketEntity> findByTicketCodeWithDetails(@Param("ticketCode") String ticketCode);
    
    @Query("SELECT DISTINCT t FROM TicketEntity t JOIN FETCH t.booking b " +
           "JOIN FETCH b.user u " +
           "JOIN FETCH b.trip tr " +
           "JOIN FETCH tr.route r " +
           "JOIN FETCH r.departureLocation JOIN FETCH r.arrivalLocation " +
           "JOIN FETCH tr.coach JOIN FETCH t.seat " +
           "WHERE u.phone = :phone")
    List<TicketEntity> findByAccountPhoneWithDetails(@Param("phone") String phone);
}
