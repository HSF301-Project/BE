package sp26.group.busticket.modules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sp26.group.busticket.infrastructure.persistence.BaseEntity;
import sp26.group.busticket.modules.enumType.PaymentStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "payment_method", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String paymentMethod;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private PaymentStatusEnum status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
