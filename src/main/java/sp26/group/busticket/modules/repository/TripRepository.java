package sp26.group.busticket.modules.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sp26.group.busticket.modules.entity.Trip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    @Query("SELECT t FROM Trip t WHERE " +
            "(LOWER(t.route.departureLocation.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.route.arrivalLocation.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.driver.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "CONCAT('', t.id) LIKE CONCAT('%', :query, '%')) " +
            "AND (:status IS NULL OR :status = 'ALL' " +
            "OR (:status = 'RUNNING' AND t.tripStatus = 'DEPARTED') " +
            "OR (:status = 'SCHEDULED' AND t.tripStatus = 'SCHEDULED') " +
            "OR (:status = 'DEPARTING_SOON' AND t.tripStatus = 'SCHEDULED' AND t.departureTime BETWEEN :now AND :soonTime))")
    Page<Trip> findAllBySearchAndStatus(@Param("query") String query, 
                                        @Param("status") String status, 
                                        @Param("now") LocalDateTime now, 
                                        @Param("soonTime") LocalDateTime soonTime, 
                                        Pageable pageable);

    // Query dành cho khách hàng tìm kiếm chuyến đi
    List<Trip> findByRoute_DepartureLocation_NameAndRoute_ArrivalLocation_NameAndDepartureTimeBetween(
            String from, String to, LocalDateTime start, LocalDateTime end);

    List<Trip> findByCoach_Id(UUID coachId);

    boolean existsByCoach_Id(UUID coachId);

    @Query("SELECT t FROM Trip t WHERE t.departureTime BETWEEN :start AND :end")
    List<Trip> findAllTripsToday(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
