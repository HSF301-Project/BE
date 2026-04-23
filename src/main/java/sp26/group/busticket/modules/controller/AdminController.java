package sp26.group.busticket.modules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.modules.dto.trip.request.TripRequestDTO;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.enumType.TripStatusEnum;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.repository.RouteRepository;
import sp26.group.busticket.modules.service.AccountService;
import sp26.group.busticket.modules.service.CoachService;
import sp26.group.busticket.modules.service.FinanceService;
import sp26.group.busticket.modules.service.TripService;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @InitBinder("tripRequest")
    public void initTripRequestBinder(WebDataBinder binder) {
        for (String field : new String[]{"assistantId", "driverId", "secondDriverId", "returnDriverId", "returnSecondDriverId", "returnAssistantId"}) {
            binder.registerCustomEditor(UUID.class, field, new PropertyEditorSupport() {
                @Override
                public void setAsText(String text) throws IllegalArgumentException {
                    if (text == null || text.isBlank()) {
                        setValue(null);
                    } else {
                        setValue(UUID.fromString(text));
                    }
                }
            });
        }
    }

    private final AccountRepository accountRepository;
    private final RouteRepository routeRepository;
    private final CoachService coachService;
    private final TripService tripService;
    private final AccountService accountService;
    private final FinanceService financeService;

    @GetMapping("/users")
    public String listUsers(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                            @org.springframework.web.bind.annotation.RequestParam(required = false) String role,
                            Model model) {
        model.addAttribute("users", accountService.getAllAccounts(search, role));
        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("title", "Quản lý Nhân viên");
        model.addAttribute("activePage", "users");
        return "Admin/user-list";
    }

    @GetMapping("/customers")
    public String listCustomers(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                                Model model) {
        // Force role to USER
        model.addAttribute("users", accountService.getAllAccounts(search, "USER"));
        model.addAttribute("search", search);
        model.addAttribute("title", "Quản lý Khách hàng");
        model.addAttribute("activePage", "customers");
        return "Admin/customer-list";
    }

    @PostMapping("/users/status/{id}")
    public String changeUserStatus(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        accountService.changeStatus(id);
        
        // Fetch the account to determine where to redirect
        var account = accountRepository.findById(id).orElse(null);
        String redirectUrl = "/admin/users";
        if (account != null && "USER".equals(account.getRole())) {
            redirectUrl = "/admin/customers";
        }
        String message = "Cập nhật Trạng thái nhân viên thành công!";
        if (account != null && "USER".equals(account.getRole())) {
            message = "Cập nhật Trạng thái khách hàng thành công!";
        }
        
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("totalCoaches", coachService.getAllCoaches().size());
        
        var financeData = financeService.getFinanceDashboardData();
        model.addAttribute("todayRevenue", financeData.getTodayRevenueFormatted());
        model.addAttribute("successRate", financeData.getSuccessRate());
        
        model.addAttribute("title", "Bảng điều khiển");
        model.addAttribute("activePage", "dashboard");
        return "Admin/dashboard";
    }

    @GetMapping("/trips/detail/{id}")
    public String getTripDetail(@PathVariable UUID id, Model model) {
        model.addAttribute("trip", coachService.getAdminTripDetail(id));
        model.addAttribute("title", "Sơ đồ hành khách");
        model.addAttribute("activePage", "trips");
        return "Admin/trip-details";
    }

    @GetMapping("/trips")
    public String tripList(@RequestParam(required = false) String q,
                           @RequestParam(required = false, defaultValue = "ALL") String status,
                           @RequestParam(defaultValue = "1") int page,
                           Model model) {
        var data = tripService.getAdminDashboardData(q, status, page, 10);
        model.addAttribute("trips", data.getTrips());
        model.addAttribute("stats", data.getStats());
        Map<String, Object> pagination = new java.util.HashMap<>();
        pagination.put("currentPage", data.getCurrentPage());
        pagination.put("totalCount", data.getTotalCount());
        pagination.put("displayedCount", data.getDisplayedCount());
        pagination.put("hasNext", data.isHasNext());
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchQuery", q != null ? q : "");
        model.addAttribute("currentStatus", status);
        model.addAttribute("activePage", "trips");
        return "Admin/trip_management";
    }

    @GetMapping("/trips/new")
    public String newTripForm(Model model) {
        model.addAttribute("tripRequest", TripRequestDTO.builder()
                .status(TripStatusEnum.SCHEDULED)
                .build());
        enrichTripFormModel(model);
        model.addAttribute("editMode", false);
        model.addAttribute("activePage", "trips");
        return "Admin/trip-form";
    }

    @GetMapping("/trips/{id}/edit")
    public String editTripForm(@PathVariable UUID id,
                               Model model) {
        model.addAttribute("tripRequest", tripService.getTripForEdit(id));
        enrichTripFormModel(model);
        model.addAttribute("editMode", true);
        model.addAttribute("activePage", "trips");
        return "Admin/trip-form";
    }

    @PostMapping("/trips/save")
    public String saveTrip(@Valid @ModelAttribute("tripRequest") TripRequestDTO requestDTO,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            enrichTripFormModel(model);
            model.addAttribute("editMode", requestDTO.getId() != null);
            return "Admin/trip-form";
        }
        try {
            if (requestDTO.getId() == null) {
                tripService.createTrip(requestDTO);
                redirectAttributes.addFlashAttribute("message", "Thêm mới chuyến đi thành công!");
            } else {
                tripService.updateTrip(requestDTO.getId(), requestDTO);
                redirectAttributes.addFlashAttribute("message", "Cập nhật chuyến đi thành công!");
            }
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            enrichTripFormModel(model);
            model.addAttribute("editMode", requestDTO.getId() != null);
            return "Admin/trip-form";
        }
        return "redirect:/admin/trips";
    }


    private void enrichTripFormModel(Model model) {
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("coaches", coachService.getAllCoaches());
        model.addAttribute("drivers", tripService.listAssignableDrivers());
        model.addAttribute("assistants", accountRepository.findByRoleAndStatusOrderByFullNameAsc("STAFF", StatusEnum.ACTIVE));
        model.addAttribute("tripStatuses", TripStatusEnum.values());
    }

    // --- AJAX Endpoints for Dynamic Form ---
    @GetMapping("/trips/check-return-route")
    @ResponseBody
    public Map<String, Object> checkReturnRoute(@RequestParam UUID routeId) {
        Map<String, Object> res = new HashMap<>();
        Optional<UUID> returnRouteId = tripService.findReturnRouteId(routeId);
        res.put("hasReturn", returnRouteId.isPresent());
        if (returnRouteId.isPresent()) {
            res.put("returnRouteId", returnRouteId.get());
            routeRepository.findById(returnRouteId.get()).ifPresent(route -> {
                res.put("duration", route.getDuration());
                res.put("distance", route.getDistance());
            });
        }
        return res;
    }

    @GetMapping("/trips/check-availability")
    @ResponseBody
    public Map<String, Object> checkAvailability(@RequestParam(required = false) UUID driverId,
                                                @RequestParam(required = false) UUID coachId,
                                                @RequestParam(required = false) UUID routeId,
                                                @RequestParam LocalDateTime start,
                                                @RequestParam LocalDateTime end,
                                                @RequestParam(required = false) UUID excludeId) {
        Map<String, Object> res = new HashMap<>();
        if (driverId != null) {
            res.put("driverAvailable", tripService.isDriverAvailable(driverId, start, end, excludeId));
        }
        if (coachId != null) {
            res.put("coachAvailable", tripService.isCoachAvailable(coachId, start, end, routeId, excludeId));
        }
        return res;
    }

    @GetMapping("/trips/available-staff")
    @ResponseBody
    public Map<String, Object> getAvailableStaff(@RequestParam LocalDateTime start,
                                                @RequestParam LocalDateTime end,
                                                @RequestParam(required = false) UUID excludeId) {
        Map<String, Object> res = new HashMap<>();
        res.put("availableDriverIds", tripService.getAvailableDriverIds(start, end, excludeId));
        return res;
    }

    @GetMapping("/trips/available-coaches")
    @ResponseBody
    public Map<String, Object> getAvailableCoaches(@RequestParam LocalDateTime start,
                                                  @RequestParam LocalDateTime end,
                                                  @RequestParam(required = false) UUID routeId,
                                                  @RequestParam(required = false) UUID excludeId) {
        Map<String, Object> res = new HashMap<>();
        res.put("availableCoachIds", tripService.getAvailableCoachIds(start, end, routeId, excludeId));
        return res;
    }
}
