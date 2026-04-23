package sp26.group.busticket.service;

import sp26.group.busticket.dto.route.request.RouteRequestDTO;
import sp26.group.busticket.entity.Location;
import sp26.group.busticket.entity.Route;

import java.util.List;
import java.util.UUID;

public interface RouteService {
    List<Route> getAllRoutes();
    Route getRouteById(UUID id);
    Route saveRoute(RouteRequestDTO req);
    void deleteRoute(UUID id);
    
    // Smart features
    List<Location> getSmartWaypoints(UUID departureLocationId, UUID arrivalLocationId);
    RouteRequestDTO calculateMetrics(RouteRequestDTO req);
    String generateRouteCode(UUID departureLocationId, UUID arrivalLocationId);
    void createReturnRoute(UUID routeId);
}
