package sp26.group.busticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.entity.RouteStop;

import java.util.List;
import java.util.UUID;

public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {

    /** Lấy toàn bộ điểm dừng của một tuyến, sắp xếp theo thứ tự. */
    List<RouteStop> findByRouteIdOrderByStopOrderAsc(UUID routeId);

    /** Xoá hết điểm dừng cũ trước khi lưu lại danh sách mới. */
    void deleteAllByRouteId(UUID routeId);
}
