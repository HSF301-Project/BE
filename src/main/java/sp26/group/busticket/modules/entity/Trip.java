package sp26.group.busticket.modules.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import sp26.group.busticket.modules.enumType.TripStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sp26.group.busticket.infrastructure.persistence.BaseEntity;

/**
 * Một chuyến xe cụ thể được lên lịch chạy theo một tuyến (Route).
 *
 * Các điểm dừng đón/trả và toàn bộ hành trình được quản lý
 * thông qua Route.stops (danh sách RouteStop), không lưu lại
 * dưới dạng text trong Trip nữa.
 */
@Entity
@Table(name = "trips")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Trip extends BaseEntity {

    /** Tuyến đường chạy (bao gồm cả danh sách điểm dừng). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    /** Xe thực hiện chuyến. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    /** Tài xế chính. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Account driver;

    /** Tài xế thứ hai (Bắt buộc cho các chuyến > 4 tiếng). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_driver_id")
    private Account secondDriver;

    /** Phụ xe / phụ lái (Nhân viên phục vụ). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_id")
    private Account assistant;

    /** Thời điểm xuất bến (giờ địa phương). */
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    /** Thời điểm dự kiến đến bến cuối (giờ địa phương). */
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    /** Giá vé cơ bản của chuyến (VND). */
    @Column(name = "price_base", nullable = false)
    private BigDecimal priceBase;

    /** Số điện thoại liên hệ của chuyến (để khách gọi hỏi). */
    @Column(name = "contact_phone_number", nullable = false)
    private String contactPhoneNumber;

    /** Trạng thái hiện tại của chuyến. */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TripStatusEnum tripStatus = TripStatusEnum.SCHEDULED;

    /** Thời điểm xuất bến thực tế. */
    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    /** Thời điểm đến đích thực tế. */
    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;
}
