package sp26.group.busticket.modules.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sp26.group.busticket.modules.entity.Trip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByRoute_DepartureLocation_NameAndRoute_ArrivalLocation_NameAndDepartureTimeBetween(
            String from, String to, LocalDateTime startOfDay, LocalDateTime endOfDay);
    
    boolean existsByCoach_Id(UUID coachId);

    // Tìm kiếm phân trang theo điểm khởi hành hoặc mã chuyến
    @Query("SELECT t FROM Trip t WHERE " +
            "t.route.startLocation LIKE %:query% OR " +
            "t.route.endLocation LIKE %:query% OR " +
            "CAST(t.id AS string) LIKE %:query%")
    Page<Trip> findAllBySearch(String query, Pageable pageable);

    // Lấy tất cả chuyến đi trong ngày hôm nay
    @Query("SELECT t FROM Trip t WHERE t.departureTime >= :startOfDay AND t.departureTime <= :endOfDay")
    List<Trip> findAllTripsToday(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
