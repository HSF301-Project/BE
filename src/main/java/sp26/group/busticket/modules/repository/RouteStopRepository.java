package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sp26.group.busticket.modules.entity.RouteStop;

import java.util.List;
import java.util.UUID;

public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {

    /** Lấy toàn bộ điểm dừng của một tuyến, sắp xếp theo thứ tự. */
    List<RouteStop> findByRouteIdOrderByStopOrderAsc(UUID routeId);

    /** Xoá hết điểm dừng cũ trước khi lưu lại danh sách mới. */
    void deleteAllByRouteId(UUID routeId);
}
