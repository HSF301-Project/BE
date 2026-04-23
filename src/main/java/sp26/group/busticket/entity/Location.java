package sp26.group.busticket.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
    @NotBlank(message = "Tên địa điểm không được để trống")
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;

    /** Tỉnh / thành phố trực thuộc TW. */
    @NotBlank(message = "Thành phố/Tỉnh không được để trống")
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String city;

    /** Địa chỉ chi tiết (số nhà, đường, phường/xã, quận/huyện). */
    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Column(columnDefinition = "NVARCHAR(500)")
    private String address;

    /** Loại địa điểm: TERMINAL (bến chính), STOP (trạm dừng). */
    @Column(name = "location_type", nullable = false, columnDefinition = "NVARCHAR(32)")
    private String locationType; // TERMINAL | STOP

    /** Ghi chú thêm (giờ hoạt động, số điện thoại bến xe, v.v.). */
    @Column(columnDefinition = "NVARCHAR(500)")
    private String notes;
}
