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
import sp26.group.busticket.modules.service.LocationService;
import sp26.group.busticket.modules.service.RouteService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/infrastructure")
@RequiredArgsConstructor
public class AdminInfrastructureController {

    private final LocationService locationService;
    private final RouteService routeService;

    @GetMapping
    public String infrastructure(@RequestParam(name = "tab", defaultValue = "stations") String tab, 
                                 @AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("activeTab", tab);

        // Thống kê thực tế từ DB
        long totalLocations = locationService.countTotalStations();
        long totalCities = locationService.countTotalCities();

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
        model.addAttribute("stations", locationService.getLocationsSortedByCity());
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
        Location location = locationService.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy địa điểm."));
        model.addAttribute("location", location);
        model.addAttribute("activePage", "infrastructure");
        return "Admin/station-form";
    }

    @PostMapping("/stations/save")
    public String saveStation(@ModelAttribute("location") Location location,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        locationService.saveLocation(location);
        redirectAttributes.addFlashAttribute("message", "Địa điểm '" + location.getName() + "' đã được lưu thành công!");
        return "redirect:/admin/infrastructure?tab=stations";
    }

    // ==================== ROUTES ====================

    @GetMapping("/routes/new")
    public String newRoute(Model model) {
        model.addAttribute("routeRequest", RouteRequestDTO.builder().build());
        model.addAttribute("locations", locationService.getLocationsSortedByCity());
        model.addAttribute("activePage", "infrastructure");
        return "Admin/route-form";
    }

    @GetMapping("/routes/{id}/edit")
    public String editRoute(@PathVariable java.util.UUID id, Model model) {
        Route route = routeService.getRouteById(id)
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
        model.addAttribute("locations", locationService.getLocationsSortedByCity());
        model.addAttribute("activePage", "infrastructure");
        return "Admin/route-form";
    }

    @PostMapping("/routes/save")
    public String saveRoute(@Valid @ModelAttribute("routeRequest") RouteRequestDTO req,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("locations", locationService.getLocationsSortedByCity());
            return "Admin/route-form";
        }

        // Use RouteService to save route logic (Simplified for now, but RouteService.saveRoute is available)
        // I'll call routeService.saveRoute(req) eventually, but for now let's just use the repo if needed 
        // OR better, move all logic to routeService.
        routeService.saveRoute(req);

        redirectAttributes.addFlashAttribute("message",
                "Tuyến đường '" + req.getRouteCode() + "' đã được lưu thành công!");
        return "redirect:/admin/infrastructure?tab=routes";
    }

    // =====================================================================
    // Helpers
    // =====================================================================


}
