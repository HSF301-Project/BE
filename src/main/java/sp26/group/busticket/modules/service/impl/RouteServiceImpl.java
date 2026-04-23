package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.route.request.RouteRequestDTO;
import sp26.group.busticket.modules.dto.trip.response.TripStopEtaDTO;
import sp26.group.busticket.modules.entity.Location;
import sp26.group.busticket.modules.entity.Route;
import sp26.group.busticket.modules.entity.RouteStop;
import sp26.group.busticket.modules.repository.LocationRepository;
import sp26.group.busticket.modules.repository.RouteRepository;
import sp26.group.busticket.modules.service.RouteService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final LocationRepository locationRepository;

    @Override
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
    public Optional<Route> getRouteById(UUID id) {
        return routeRepository.findById(id);
    }

    @Override
    public Route getRouteEntityById(UUID id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND));
    }

    @Override
    public RouteRequestDTO getRouteRequestById(UUID id) {
        Route route = getRouteEntityById(id);
        return RouteRequestDTO.builder()
                .id(route.getId())
                .departureLocationId(route.getDepartureLocation().getId())
                .arrivalLocationId(route.getArrivalLocation().getId())
                .distanceKm(route.getDistance())
                .durationMinutes(route.getDuration())
                .build();
    }

    @Override
    @Transactional
    public Route saveRoute(RouteRequestDTO request) {
        Location dep = locationRepository.findById(request.getDepartureLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        Location arr = locationRepository.findById(request.getArrivalLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));

        Route route;
        if (request.getId() != null) {
            route = getRouteEntityById(request.getId());
        } else {
            route = new Route();
        }

        route.setDepartureLocation(dep);
        route.setArrivalLocation(arr);
        route.setDistance(request.getDistanceKm());
        route.setDuration(request.getDurationMinutes());

        return routeRepository.save(route);
    }

    @Override
    public List<TripStopEtaDTO> buildRouteTimeline(sp26.group.busticket.modules.entity.Trip trip, DateTimeFormatter timeOnly) {
        List<StopWithKm> stops = new ArrayList<>();
        stops.add(new StopWithKm(trip.getRoute().getDepartureLocation().getName(), 0f, "START", null));

        if (trip.getRoute().getStops() != null) {
            for (RouteStop rs : trip.getRoute().getStops()) {
                String stopType = rs.getStopType() != null ? rs.getStopType().name() : "BOTH";
                stops.add(new StopWithKm(
                        rs.getLocation().getName(),
                        rs.getDistanceFromStart(),
                        "INTERMEDIATE",
                        new StopMeta(stopType, rs.getOffsetMinutes())));
            }
        }

        Float totalKm = trip.getRoute().getDistance();
        if (totalKm == null || totalKm <= 0) totalKm = 1f;
        stops.add(new StopWithKm(trip.getRoute().getArrivalLocation().getName(), totalKm, "END", null));

        long totalMinutes = java.time.Duration.between(trip.getDepartureTime(), trip.getArrivalTime()).toMinutes();
        if (totalMinutes <= 0 && trip.getRoute().getDuration() != null) totalMinutes = trip.getRoute().getDuration();
        if (totalMinutes <= 0) totalMinutes = 60;

        boolean canUseKm = stops.stream().filter(s -> s.km != null).count() >= 2;
        int segmentsFallback = Math.max(stops.size() - 1, 1);

        List<TripStopEtaDTO> result = new ArrayList<>();

        for (int i = 0; i < stops.size(); i++) {
            StopWithKm s = stops.get(i);
            long offsetMinutes;
            if (s.meta != null && s.meta.offsetMinutes != null) {
                offsetMinutes = s.meta.offsetMinutes;
            } else if (canUseKm && s.km != null) {
                offsetMinutes = Math.round(totalMinutes * (s.km / totalKm));
            } else {
                offsetMinutes = Math.round((double) i * totalMinutes / segmentsFallback);
            }

            LocalDateTime eta = trip.getDepartureTime().plusMinutes(offsetMinutes);
            String stopType = s.type;
            String pointTypeLabel = switch (s.type.toUpperCase()) {
                case "PICKUP" -> "Chỉ đón";
                case "DROPOFF" -> "Chỉ trả";
                default -> "Đón & trả";
            };
            result.add(TripStopEtaDTO.builder()
                    .stopName(s.name)
                    .etaTime(eta.format(timeOnly))
                    .stopType(stopType)
                    .pointType(s.type.toUpperCase())
                    .pointTypeLabel(pointTypeLabel)
                    .offsetMinutes((int) offsetMinutes)
                    .formattedOffset(formatDuration((int) offsetMinutes))
                    .build());
        }
        return result;
    }

    private String formatDuration(int totalMinutes) {
        if (totalMinutes <= 0) return "0 phút";
        int days = totalMinutes / (24 * 60);
        int hours = (totalMinutes % (24 * 60)) / 60;
        int mins = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" ngày ");
        if (hours > 0) sb.append(hours).append(" giờ ");
        if (mins > 0 || sb.length() == 0) sb.append(mins).append(" phút");
        return sb.toString().trim();
    }

    private static class StopWithKm {
        final String name;
        final Float km;
        final String type;
        final StopMeta meta;

        private StopWithKm(String name, Float km, String type, StopMeta meta) {
            this.name = name;
            this.km = km;
            this.type = type;
            this.meta = meta;
        }
    }

    private static class StopMeta {
        final String pointType;
        final Integer offsetMinutes;

        private StopMeta(String pointType, Integer offsetMinutes) {
            this.pointType = pointType;
            this.offsetMinutes = offsetMinutes;
        }
    }
}
