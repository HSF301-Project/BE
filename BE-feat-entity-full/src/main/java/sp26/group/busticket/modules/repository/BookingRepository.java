package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.BookingEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Integer> {

    @Query("SELECT b FROM BookingEntity b LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.trip tr " +
           "LEFT JOIN FETCH tr.route r " +
           "LEFT JOIN FETCH r.departureLocation " +
           "LEFT JOIN FETCH r.arrivalLocation " +
           "LEFT JOIN FETCH tr.coach " +
           "WHERE b.id = :bookingId")
    Optional<BookingEntity> findByIdWithDetails(@Param("bookingId") Integer bookingId);

    @Query("SELECT b FROM BookingEntity b LEFT JOIN FETCH b.user " +
           "WHERE b.id = :bookingId")
    Optional<BookingEntity> findByIdWithUser(@Param("bookingId") Integer bookingId);

    @Query("SELECT b FROM BookingEntity b LEFT JOIN FETCH b.user u " +
           "WHERE u.phone = :phone OR u IS NULL")
    List<BookingEntity> findByUserPhone(@Param("phone") String phone);
}
