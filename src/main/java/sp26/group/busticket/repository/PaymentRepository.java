package sp26.group.busticket.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.entity.Payment;
import sp26.group.busticket.enumType.PaymentStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND p.paidAt >= :startOfDay")
    BigDecimal sumTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay);

    long countByStatus(PaymentStatusEnum status);

    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Payment> findByTransactionIdContainingOrderByCreatedAtDesc(String transactionId, Pageable pageable);

    java.util.Optional<Payment> findByBooking_Id(UUID bookingId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID'")
    BigDecimal sumTotalRevenue();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'PAID' AND MONTH(p.paidAt) = :month AND YEAR(p.paidAt) = :year")
    BigDecimal sumMonthlyRevenue(@Param("month") int month, @Param("year") int year);

    @Query("SELECT MONTH(p.paidAt) as month, SUM(p.amount) as revenue FROM Payment p " +
           "WHERE p.status = 'PAID' AND YEAR(p.paidAt) = :year " +
           "GROUP BY MONTH(p.paidAt) ORDER BY MONTH(p.paidAt) ASC")
    List<Object[]> getMonthlyRevenueStats(@Param("year") int year);

    @Query("SELECT DAY(p.paidAt) as day, SUM(p.amount) as revenue FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.paidAt >= :since " +
           "GROUP BY DAY(p.paidAt) ORDER BY DAY(p.paidAt) ASC")
    List<Object[]> getDailyRevenueStats(@Param("since") LocalDateTime since);
}
