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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

    private final sp26.group.busticket.modules.service.LocationService locationService;
    private final sp26.group.busticket.modules.service.RouteService routeService;

    @GetMapping
    public String infrastructure(@RequestParam(name = "tab", defaultValue = "stations") String tab,
                                 @RequestParam(name = "type", required = false) String type,
                                 @AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("activeTab", tab);
        model.addAttribute("activeType", type);

        // Thống kê thực tế từ DB
        long totalLocations = locationService.countLocations();
        long totalCities = locationService.countDistinctCities();

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
        List<Location> stations;
        if (type == null || type.equals("ALL")) {
            stations = listLocations();
        } else {
            stations = locationService.getLocationsByType(type).stream()
                    .sorted(Comparator.comparing(Location::getCity).thenComparing(Location::getName))
                    .toList();
        }
        
        model.addAttribute("stations", stations);
        model.addAttribute("routes", routeService.getAllRoutes());
        model.addAttribute("activePage", "infrastructure");
        return "Admin/infrastructure_setup";
    }

    // ==================== STATIONS ====================

    @GetMapping("/stations/new")
    public String newStation(Model model) {
        model.addAttribute("location", new Location());
        model.addAttribute("activePage", "infrastructure");
        return "Admin/station-form";
    }

    @GetMapping("/stations/{id}/edit")
    public String editStation(@PathVariable java.util.UUID id, Model model) {
        Location location = locationService.getLocationById(id);
        if (location == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy địa điểm.");
        }
        model.addAttribute("location", location);
        model.addAttribute("activePage", "infrastructure");
        return "Admin/station-form";
    }

    @PostMapping("/stations/save")
    public String saveStation(@Valid @ModelAttribute("location") Location location,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "infrastructure");
            return "Admin/station-form";
        }

        // Kiểm tra trùng địa chỉ
        Location existing = locationService.findByAddress(location.getAddress());
        if (existing != null) {
            if (location.getId() == null || !existing.getId().equals(location.getId())) {
                bindingResult.rejectValue("address", "duplicate", "Địa chỉ này đã tồn tại trong hệ thống.");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("activePage", "infrastructure");
            return "Admin/station-form";
        }

        if (location.getId() != null) {
            Location current = locationService.getLocationById(location.getId());
            if (current == null) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy địa điểm.");
            }
            current.setName(location.getName());
            current.setCity(location.getCity());
            current.setAddress(location.getAddress());
            current.setLocationType(location.getLocationType());
            current.setNotes(location.getNotes());
            locationService.saveLocation(current);
        } else {
            locationService.saveLocation(location);
        }
        redirectAttributes.addFlashAttribute("message", "Địa điểm '" + location.getName() + "' đã được lưu thành công!");
        return "redirect:/admin/infrastructure?tab=stations";
    }

    // ==================== ROUTES ====================

    @GetMapping("/routes/new")
    public String newRoute(Model model) {
        model.addAttribute("routeRequest", RouteRequestDTO.builder().build());
        model.addAttribute("terminals", listTerminals());
        model.addAttribute("locations", listLocations());
        model.addAttribute("activePage", "infrastructure");
        return "Admin/route-form";
    }

    @GetMapping("/routes/{id}/edit")
    public String editRoute(@PathVariable java.util.UUID id, Model model) {
        Route route = routeService.getRouteById(id);
        
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
        model.addAttribute("terminals", listTerminals());
        model.addAttribute("locations", listLocations());
        model.addAttribute("activePage", "infrastructure");
        return "Admin/route-form";
    }

    @PostMapping("/routes/save")
    public String saveRoute(@Valid @ModelAttribute("routeRequest") RouteRequestDTO req,
                            BindingResult result,
                            @RequestParam(value = "createReturn", defaultValue = "false") boolean createReturn,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("terminals", listTerminals());
            model.addAttribute("locations", listLocations());
            return "Admin/route-form";
        }

        try {
            Route route = routeService.saveRoute(req);
            if (createReturn) {
                routeService.createReturnRoute(route.getId());
            }
            redirectAttributes.addFlashAttribute("message",
                    "Tuyến đường '" + route.getRouteCode() + "' đã được lưu thành công!");
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("terminals", listTerminals());
            model.addAttribute("locations", listLocations());
            return "Admin/route-form";
        }

        return "redirect:/admin/infrastructure?tab=routes";
    }

    @GetMapping("/routes/smart-waypoints")
    @ResponseBody
    public List<Location> getSmartWaypoints(@RequestParam java.util.UUID depId, @RequestParam java.util.UUID arrId) {
        return routeService.getSmartWaypoints(depId, arrId);
    }

    @PostMapping("/routes/calculate")
    @ResponseBody
    public RouteRequestDTO calculateMetrics(@RequestBody RouteRequestDTO req) {
        return routeService.calculateMetrics(req);
    }

    @GetMapping("/routes/generate-code")
    @ResponseBody
    public Map<String, String> generateCode(@RequestParam java.util.UUID depId, @RequestParam java.util.UUID arrId) {
        return Map.of("code", routeService.generateRouteCode(depId, arrId));
    }

    // =====================================================================
    // Helpers
    // =====================================================================


    private List<Location> listLocations() {
        return locationService.getAllLocations().stream()
                .sorted(Comparator.comparing(Location::getCity).thenComparing(Location::getName))
                .toList();
    }

    private List<Location> listTerminals() {
        return locationService.getLocationsByType("TERMINAL").stream()
                .sorted(Comparator.comparing(Location::getCity).thenComparing(Location::getName))
                .toList();
    }
}
