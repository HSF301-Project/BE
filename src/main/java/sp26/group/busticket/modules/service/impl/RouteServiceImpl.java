package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.route.request.RouteRequestDTO;
import sp26.group.busticket.modules.dto.route.request.RouteStopRequestDTO;
import sp26.group.busticket.modules.entity.Location;
import sp26.group.busticket.modules.entity.Route;
import sp26.group.busticket.modules.entity.RouteStop;
import sp26.group.busticket.modules.repository.LocationRepository;
import sp26.group.busticket.modules.repository.RouteRepository;
import sp26.group.busticket.modules.repository.RouteStopRepository;
import sp26.group.busticket.modules.service.RouteService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final LocationRepository locationRepository;
    private final RouteStopRepository routeStopRepository;

    @Override
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
    public Route getRouteById(UUID id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy tuyến đường."));
    }

    @Override
    @Transactional
    public Route saveRoute(RouteRequestDTO req) {
        Location dep = locationRepository.findById(req.getDepartureLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Điểm khởi hành không hợp lệ."));
        Location arr = locationRepository.findById(req.getArrivalLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Điểm đến không hợp lệ."));

        Route route;
        if (req.getId() != null) {
            route = routeRepository.findById(req.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy tuyến đường."));
            route.setRouteCode(req.getRouteCode().trim());
            route.setDepartureLocation(dep);
            route.setArrivalLocation(arr);
            route.setDistance(req.getDistanceKm());
            route.setDuration(req.getDurationMinutes());
            routeStopRepository.deleteAll(route.getStops());
            route.getStops().clear();
        } else {
            route = Route.builder()
                    .routeCode(req.getRouteCode() != null ? req.getRouteCode().trim() : generateRouteCode(dep.getId(), arr.getId()))
                    .departureLocation(dep)
                    .arrivalLocation(arr)
                    .distance(req.getDistanceKm())
                    .duration(req.getDurationMinutes())
                    .stops(new ArrayList<>())
                    .build();
        }
        route = routeRepository.save(route);

        if (req.getStops() != null && !req.getStops().isEmpty()) {
            List<RouteStop> stops = new ArrayList<>();
            int order = 1;
            for (RouteStopRequestDTO stopDto : req.getStops()) {
                Location stopLoc = locationRepository.findById(stopDto.getLocationId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT,
                                "Địa điểm dừng không hợp lệ: " + stopDto.getLocationId()));
                stops.add(RouteStop.builder()
                        .route(route)
                        .location(stopLoc)
                        .stopOrder(order++)
                        .stopType(stopDto.getStopType())
                        .offsetMinutes(stopDto.getOffsetMinutes())
                        .distanceFromStart(stopDto.getDistanceFromStart())
                        .notes(stopDto.getNotes())
                        .build());
            }
            routeStopRepository.saveAll(stops);
        }

        return route;
    }

    @Override
    @Transactional
    public void deleteRoute(UUID id) {
        routeRepository.deleteById(id);
    }

    @Override
    public List<Location> getSmartWaypoints(UUID departureLocationId, UUID arrivalLocationId) {
        // Trả về tất cả địa điểm vì không còn tọa độ để lọc
        return locationRepository.findAll();
    }

    @Override
    public RouteRequestDTO calculateMetrics(RouteRequestDTO req) {
        // Không còn tọa độ để tự động tính toán metrics
        return req;
    }

    @Override
    public String generateRouteCode(UUID departureLocationId, UUID arrivalLocationId) {
        Location dep = locationRepository.findById(departureLocationId).orElseThrow();
        Location arr = locationRepository.findById(arrivalLocationId).orElseThrow();
        
        String depCode = getCityCode(dep.getCity());
        String arrCode = getCityCode(arr.getCity());
        
        long count = routeRepository.count();
        return String.format("%s-%s-%02d", depCode, arrCode, count + 1);
    }

    @Override
    @Transactional
    public void createReturnRoute(UUID routeId) {
        Route forward = routeRepository.findById(routeId).orElseThrow();
        
        // Kiểm tra xem đã có tuyến khứ hồi chưa
        Optional<Route> existingReturn = routeRepository.findByDepartureLocationAndArrivalLocation(
                forward.getArrivalLocation(), forward.getDepartureLocation());

        RouteRequestDTO backwardReq = RouteRequestDTO.builder()
                .id(existingReturn.map(Route::getId).orElse(null)) // Nếu có rồi thì update
                .departureLocationId(forward.getArrivalLocation().getId())
                .arrivalLocationId(forward.getDepartureLocation().getId())
                .distanceKm(forward.getDistance())
                .durationMinutes(forward.getDuration())
                .routeCode(existingReturn.map(Route::getRouteCode).orElseGet(() -> 
                        generateRouteCode(forward.getArrivalLocation().getId(), forward.getDepartureLocation().getId())))
                .stops(new ArrayList<>())
                .build();

        // Đảo ngược danh sách stops
        List<RouteStop> forwardStops = forward.getStops().stream()
                .sorted(Comparator.comparing(RouteStop::getStopOrder).reversed())
                .toList();

        for (RouteStop fs : forwardStops) {
            backwardReq.getStops().add(RouteStopRequestDTO.builder()
                    .locationId(fs.getLocation().getId())
                    .stopType(fs.getStopType())
                    .notes(fs.getNotes())
                    // Các chỉ số KM và Phút sẽ được tính lại hoặc đảo ngược
                    .build());
        }
        
        // Tính lại metrics cho chuẩn
        calculateMetrics(backwardReq);
        saveRoute(backwardReq);
    }

    private String getCityCode(String cityName) {
        if (cityName == null || cityName.isBlank()) return "XX";
        // Lấy các chữ cái đầu của các từ trong tên thành phố (Ví dụ: Hồ Chí Minh -> HCM)
        return java.util.Arrays.stream(cityName.split("\\s+"))
                .map(word -> String.valueOf(word.charAt(0)).toUpperCase())
                .collect(Collectors.joining());
    }
}
