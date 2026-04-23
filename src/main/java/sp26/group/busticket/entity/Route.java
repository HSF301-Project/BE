package sp26.group.busticket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Tuyến đường: xác định hành trình từ điểm đi → điểm đến
 * và danh sách các điểm dừng đón/trả dọc đường (RouteStop).
 */
@Entity
@Table(name = "routes")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Route extends BaseEntity {

    /** Mã tuyến nội bộ (ví dụ: SG-DL-01). */
    @Column(name = "route_code", columnDefinition = "NVARCHAR(64)")
    private String routeCode;

    /** Điểm xuất phát (bến đầu) – FK → locations. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_location_id", nullable = false)
    private Location departureLocation;

    /** Điểm kết thúc (bến cuối) – FK → locations. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_location_id", nullable = false)
    private Location arrivalLocation;

    /** Tổng khoảng cách (km). */
    @Column(nullable = false)
    private Float distance;

    /** Thời gian di chuyển tham khảo (phút). */
    @Column(nullable = false)
    private Integer duration;

    /**
     * Danh sách điểm dừng đón/trả dọc tuyến.
     * Thứ tự xác định bởi RouteStop.stopOrder (ASC).
     * Mỗi điểm dừng là một bản ghi trong bảng route_stops,
     * tham chiếu đến bảng locations.
     */
    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY,
               cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    private List<RouteStop> stops = new ArrayList<>();
}
