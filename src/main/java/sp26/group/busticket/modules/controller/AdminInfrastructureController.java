package sp26.group.busticket.modules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.route.request.RouteRequestDTO;
import sp26.group.busticket.modules.dto.route.request.RouteStopRequestDTO;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.entity.Location;
import sp26.group.busticket.modules.entity.Route;
import sp26.group.busticket.modules.entity.RouteStop;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.repository.LocationRepository;
import sp26.group.busticket.modules.repository.RouteRepository;
import sp26.group.busticket.modules.repository.RouteStopRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/infrastructure")
@RequiredArgsConstructor
public class AdminInfrastructureController {

    private final AccountRepository accountRepository;
    private final LocationRepository locationRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;

    @GetMapping
    public String infrastructure(@RequestParam(name = "tab", defaultValue = "stations") String tab, 
                                 @AuthenticationPrincipal UserDetails userDetails, Model model) {
        attachAdminUser(userDetails, model);
        model.addAttribute("activeTab", tab);

        // Thống kê thực tế từ DB
        long totalLocations = locationRepository.count();
        long totalCities = locationRepository.findAll().stream()
                .map(Location::getCity).distinct().count();

        Map<String, Object> infraMap = new HashMap<>();
        infraMap.put("priorityRoutes", List.of());
        infraMap.put("totalCities", totalCities);
        infraMap.put("networkCoverage", totalCities > 0 ? Math.min(totalCities * 4, 100) : 0);
        infraMap.put("totalStations", totalLocations);
        infraMap.put("coachModelName", "—");
        infraMap.put("coachId", null);
        infraMap.put("lowerDeckCount", 0);
        infraMap.put("upperDeckCount", 0);
        infraMap.put("lowerDeckSeats", List.of());
        infraMap.put("upperDeckSeats", List.of());
        model.addAttribute("infra", infraMap);

        // Danh sách địa điểm và tuyến đường cho tab
        model.addAttribute("stations", listLocations());
        model.addAttribute("routes", routeRepository.findAll());
        return "Admin/infrastructure_setup";
    }

    // ==================== STATIONS ====================

    @GetMapping("/stations/new")
    public String newStation(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        attachAdminUser(userDetails, model);
        model.addAttribute("location", new Location());
        return "Admin/station-form";
    }

    @GetMapping("/stations/{id}/edit")
    public String editStation(@PathVariable java.util.UUID id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        attachAdminUser(userDetails, model);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy địa điểm."));
        model.addAttribute("location", location);
        return "Admin/station-form";
    }

    @PostMapping("/stations/save")
    public String saveStation(@ModelAttribute("location") Location location,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        attachAdminUser(userDetails, model);
        if (location.getId() != null) {
            Location existing = locationRepository.findById(location.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy địa điểm."));
            existing.setName(location.getName());
            existing.setCity(location.getCity());
            existing.setAddress(location.getAddress());
            existing.setLocationType(location.getLocationType());
            existing.setNotes(location.getNotes());
            locationRepository.save(existing);
        } else {
            locationRepository.save(location);
        }
        redirectAttributes.addFlashAttribute("message", "Địa điểm '" + location.getName() + "' đã được lưu thành công!");
        return "redirect:/admin/infrastructure?tab=stations";
    }

    // ==================== ROUTES ====================

    @GetMapping("/routes/new")
    public String newRoute(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        attachAdminUser(userDetails, model);
        model.addAttribute("routeRequest", RouteRequestDTO.builder().build());
        model.addAttribute("locations", listLocations());
        return "Admin/route-form";
    }

    @GetMapping("/routes/{id}/edit")
    public String editRoute(@PathVariable java.util.UUID id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        attachAdminUser(userDetails, model);
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy tuyến đường."));
        
        List<RouteStopRequestDTO> stopDTOs = route.getStops().stream()
                .sorted(Comparator.comparing(RouteStop::getStopOrder))
                .map(s -> RouteStopRequestDTO.builder()
                        .locationId(s.getLocation().getId())
                        .stopType(s.getStopType())
                        .offsetMinutes(s.getOffsetMinutes())
                        .distanceFromStart(s.getDistanceFromStart())
                        .notes(s.getNotes())
                        .build())
                .toList();

        RouteRequestDTO req = RouteRequestDTO.builder()
                .id(route.getId())
                .routeCode(route.getRouteCode())
                .departureLocationId(route.getDepartureLocation().getId())
                .arrivalLocationId(route.getArrivalLocation().getId())
                .distanceKm(route.getDistance())
                .durationMinutes(route.getDuration())
                .stops(new ArrayList<>(stopDTOs))
                .build();

        model.addAttribute("routeRequest", req);
        model.addAttribute("locations", listLocations());
        return "Admin/route-form";
    }

    @PostMapping("/routes/save")
    public String saveRoute(@Valid @ModelAttribute("routeRequest") RouteRequestDTO req,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        attachAdminUser(userDetails, model);
        if (result.hasErrors()) {
            model.addAttribute("locations", listLocations());
            return "Admin/route-form";
        }

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
                    .routeCode(req.getRouteCode().trim())
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

        redirectAttributes.addFlashAttribute("message",
                "Tuyến đường '" + req.getRouteCode() + "' đã được lưu thành công!");
        return "redirect:/admin/infrastructure?tab=routes";
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private void attachAdminUser(UserDetails userDetails, Model model) {
        Account admin = accountRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("adminUser", admin);
    }

    private List<Location> listLocations() {
        return locationRepository.findAll().stream()
                .sorted(Comparator.comparing(Location::getCity).thenComparing(Location::getName))
                .toList();
    }
}
