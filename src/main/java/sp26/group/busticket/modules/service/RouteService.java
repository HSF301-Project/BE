package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.route.request.RouteRequestDTO;
import sp26.group.busticket.modules.entity.Route;
import sp26.group.busticket.modules.dto.trip.response.TripStopEtaDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.format.DateTimeFormatter;

public interface RouteService {
    List<Route> getAllRoutes();
    Optional<Route> getRouteById(UUID id);
    Route getRouteEntityById(UUID id);
    RouteRequestDTO getRouteRequestById(UUID id);
    Route saveRoute(RouteRequestDTO request);
    List<TripStopEtaDTO> buildRouteTimeline(sp26.group.busticket.modules.entity.Trip trip, DateTimeFormatter timeOnly);
}
