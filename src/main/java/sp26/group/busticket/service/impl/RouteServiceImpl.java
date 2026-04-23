package sp26.group.busticket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.dto.route.request.RouteRequestDTO;
import sp26.group.busticket.dto.route.request.RouteStopRequestDTO;
import sp26.group.busticket.entity.Location;
import sp26.group.busticket.entity.Route;
import sp26.group.busticket.entity.RouteStop;
import sp26.group.busticket.enumType.StopTypeEnum;
import sp26.group.busticket.repository.LocationRepository;
import sp26.group.busticket.repository.RouteRepository;
import sp26.group.busticket.repository.RouteStopRepository;
import sp26.group.busticket.service.RouteService;

import java.util.ArrayList;
import java.util.Collections;
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

        Route route = internalSaveRoute(req, dep, arr);

        // Xử lý tạo tuyến khứ hồi nếu được tích chọn
        if (req.isCreateReturn()) {
            RouteRequestDTO returnReq = RouteRequestDTO.builder()
                    .routeCode(req.getReturnRouteCode())
                    .departureLocationId(req.getArrivalLocationId())
                    .arrivalLocationId(req.getDepartureLocationId())
                    .distanceKm(req.getReturnDistanceKm() != null ? req.getReturnDistanceKm() : req.getDistanceKm())
                    .durationMinutes(req.getReturnDurationMinutes() != null ? req.getReturnDurationMinutes() : req.getDurationMinutes())
                    .stops(req.getReturnStops())
                    .build();
            
            // Tìm tuyến khứ hồi cũ nếu có để update
            routeRepository.findByDepartureLocationAndArrivalLocation(arr, dep)
                    .ifPresent(existing -> returnReq.setId(existing.getId()));
            
            internalSaveRoute(returnReq, arr, dep);
        }

        return route;
    }

    private Route internalSaveRoute(RouteRequestDTO req, Location dep, Location arr) {
        Route route;
        if (req.getId() != null) {
            route = routeRepository.findById(req.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy tuyến đường."));
            route.setRouteCode(req.getRouteCode().trim());
            route.setDepartureLocation(dep);
            route.setArrivalLocation(arr);
            route.setDistance(req.getDistanceKm());
            route.setDuration(req.getDurationMinutes());
            
            if (route.getStops() != null) {
                route.getStops().clear();
            } else {
                route.setStops(new ArrayList<>());
            }
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
            List<RouteStop> savedStops = routeStopRepository.saveAll(stops);
            route.getStops().addAll(savedStops);
        }
        return routeRepository.save(route);
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
        // Fetch and force load stops
        Route forward = routeRepository.findById(routeId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy tuyến đường gốc."));
        
        // Đảm bảo stops được load
        if (forward.getStops() != null) {
            forward.getStops().size(); 
        }

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

        // Đảo ngược danh sách stops và tính toán lại thông số
        List<RouteStop> forwardStops = new ArrayList<>(forward.getStops());
        forwardStops.sort(Comparator.comparing(RouteStop::getStopOrder));

        float totalDist = forward.getDistance();
        int totalDuration = forward.getDuration();

        // 1. Tính toán các chặng (Segments) của tuyến đi
        List<Float> distSegments = new ArrayList<>();
        List<Integer> durationSegments = new ArrayList<>();
        
        float lastDist = 0;
        int lastDuration = 0;
        
        for (RouteStop fs : forwardStops) {
            distSegments.add(fs.getDistanceFromStart() - lastDist);
            durationSegments.add(fs.getOffsetMinutes() - lastDuration);
            lastDist = fs.getDistanceFromStart();
            lastDuration = fs.getOffsetMinutes();
        }
        // Chặng cuối cùng từ stop cuối đến bến đích
        distSegments.add(totalDist - lastDist);
        durationSegments.add(totalDuration - lastDuration);

        // 2. Đảo ngược các chặng và danh sách stops
        Collections.reverse(distSegments);
        Collections.reverse(durationSegments);
        Collections.reverse(forwardStops);

        for (int i = 0; i < forwardStops.size(); i++) {
            RouteStop fs = forwardStops.get(i);
            
            // Lấy khoảng cách chặng (Segment) từ danh sách đã đảo ngược
            float segmentDist = distSegments.get(i);
            int segmentDuration = durationSegments.get(i);

            StopTypeEnum reverseStopType = fs.getStopType();
            if (fs.getStopType() == StopTypeEnum.PICKUP) {
                reverseStopType = StopTypeEnum.DROPOFF;
            } else if (fs.getStopType() == StopTypeEnum.DROPOFF) {
                reverseStopType = StopTypeEnum.PICKUP;
            }

            backwardReq.getStops().add(RouteStopRequestDTO.builder()
                    .locationId(fs.getLocation().getId())
                    .stopType(reverseStopType)
                    .distanceFromStart(segmentDist) // Lưu khoảng cách chặng
                    .offsetMinutes(segmentDuration) // Lưu thời gian chặng
                    .notes(fs.getNotes())
                    .build());
        }
        
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
