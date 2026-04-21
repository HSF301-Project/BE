package sp26.group.busticket.modules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sp26.group.busticket.infrastructure.persistence.BaseEntity;
import sp26.group.busticket.modules.enumType.StopTypeEnum;

/**
 * Điểm dừng đón/trả trung gian thuộc một tuyến đường.
 *
 * Ví dụ cho tuyến Sài Gòn → Đà Lạt:
 *   stopOrder=1 | location=An Sương     | stopType=PICKUP  | offsetMinutes=45
 *   stopOrder=2 | location=Bảo Lộc      | stopType=BOTH    | offsetMinutes=270
 *   stopOrder=3 | location=TP. Đà Lạt   | stopType=DROPOFF | offsetMinutes=390
 */
@Entity
@Table(name = "route_stops")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RouteStop extends BaseEntity {

    /** Tuyến đường chứa điểm dừng này. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    /**
     * Địa điểm dừng – lấy từ bảng locations.
     * Đảm bảo tái sử dụng dữ liệu, có đầy đủ địa chỉ, toạ độ.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    /**
     * Thứ tự dừng trong tuyến (1-based, tăng dần từ bến xuất phát).
     * Bến xuất phát = 0, các điểm trung gian = 1..N-1, bến đích = N.
     */
    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    /**
     * Loại hoạt động tại điểm dừng này:
     *   PICKUP  – chỉ đón khách
     *   DROPOFF – chỉ trả khách
     *   BOTH    – vừa đón vừa trả
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stop_type", nullable = false, columnDefinition = "NVARCHAR(16)")
    private StopTypeEnum stopType;

    /**
     * Số phút dự kiến kể từ giờ xuất bến đến khi xe đến điểm dừng này.
     * Dùng để tính ETA thực tế cho từng chuyến.
     */
    @Column(name = "offset_minutes")
    private Integer offsetMinutes;

    /** Khoảng cách từ bến đầu đến điểm dừng này (km). */
    @Column(name = "distance_from_start")
    private Float distanceFromStart;

    /** Ghi chú thêm cho điểm dừng (địa chỉ cụ thể bến đón trên đường, v.v.). */
    @Column(columnDefinition = "NVARCHAR(300)")
    private String notes;
}
