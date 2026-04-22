package sp26.group.busticket.modules.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sp26.group.busticket.infrastructure.persistence.BaseEntity;

/**
 * Đại diện cho một địa điểm vật lý trong mạng lưới vận tải.
 * Dùng làm:
 *   - Điểm khởi hành / điểm đến của Route
 *   - Điểm dừng đón / trả trung gian trong RouteStop
 */
@Entity
@Table(name = "locations")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Location extends BaseEntity {

    /** Tên hiển thị (ví dụ: "Bến xe Miền Đông", "Trạm An Sương"). */
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    /** Tỉnh / thành phố trực thuộc TW. */
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String city;

    /** Địa chỉ chi tiết (số nhà, đường, phường/xã, quận/huyện). */
    @Column(columnDefinition = "NVARCHAR(500)")
    private String address;

    /** Loại địa điểm: TERMINAL (bến chính), STOP (trạm dừng). */
    @Column(name = "location_type", nullable = false, columnDefinition = "NVARCHAR(32)")
    private String locationType; // TERMINAL | STOP

    /** Ghi chú thêm (giờ hoạt động, số điện thoại bến xe, v.v.). */
    @Column(columnDefinition = "NVARCHAR(500)")
    private String notes;
}
