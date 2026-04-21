package sp26.group.busticket.modules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.modules.dto.trip.TripAdminConstants;
import sp26.group.busticket.modules.dto.trip.request.TripRequestDTO;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.enumType.TripStatusEnum;
import sp26.group.busticket.modules.repository.AccountRepository;
import sp26.group.busticket.modules.repository.RouteRepository;
import sp26.group.busticket.modules.service.AccountService;
import sp26.group.busticket.modules.service.CoachService;
import sp26.group.busticket.modules.service.TripService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

import java.beans.PropertyEditorSupport;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    @InitBinder("tripRequest")
    public void initTripRequestBinder(WebDataBinder binder) {
        for (String field : new String[]{"assistantId", "driverId"}) {
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

    @GetMapping("/users")
    public String listUsers(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                            @org.springframework.web.bind.annotation.RequestParam(required = false) String role,
                            Model model) {
        model.addAttribute("users", accountService.getAllAccounts(search, role));
        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("title", "Quản lý Nhân viên");
        model.addAttribute("activePage", "staff");
        return "Admin/user-list";
    }

    @GetMapping("/customers")
    public String listCustomers(@org.springframework.web.bind.annotation.RequestParam(required = false) String search,
                                Model model) {
        // Force role to USER
        model.addAttribute("users", accountService.getAllAccounts(search, "USER"));
        model.addAttribute("search", search);
        model.addAttribute("title", "Quản lý Khách hàng");
        model.addAttribute("activePage", "customer");
        return "Admin/customer-list";
    }

    @PostMapping("/users/status/{id}")
    public String changeUserStatus(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        accountService.changeStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái người dùng thành công!");
        return "redirect:/admin/users";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Account admin = accountRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("adminUser", admin);
        model.addAttribute("totalCoaches", coachService.getAllCoaches().size());
        return "Admin/dashboard";
    }

    @GetMapping("/trips/detail/{id}")
    public String getTripDetail(@PathVariable UUID id, Model model) {
        model.addAttribute("trip", coachService.getAdminTripDetail(id));
        model.addAttribute("title", "Sơ đồ hành khách");
        return "Admin/trip-details";
    }

    @GetMapping("/trips")
    public String tripList(@RequestParam(required = false) String q,
                           @RequestParam(required = false, defaultValue = "ALL") String status,
                           @RequestParam(defaultValue = "1") int page,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        attachAdminUser(userDetails, model);
        var data = tripService.getAdminDashboardData(q, status, page, 10);
        model.addAttribute("trips", data.getTrips());
        model.addAttribute("stats", data.getStats());
        model.addAttribute("pagination", Map.of(
                "currentPage", data.getCurrentPage(),
                "totalCount", data.getTotalCount(),
                "displayedCount", data.getDisplayedCount(),
                "hasNext", data.isHasNext()
        ));
        model.addAttribute("searchQuery", q != null ? q : "");
        model.addAttribute("currentStatus", status);
        return "Admin/trip_management";
    }

    @GetMapping("/trips/new")
    public String newTripForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        attachAdminUser(userDetails, model);
        model.addAttribute("tripRequest", TripRequestDTO.builder()
                .driverInputMode("existing")
                .status(TripStatusEnum.SCHEDULED)
                .build());
        enrichTripFormModel(model);
        model.addAttribute("editMode", false);
        return "Admin/trip-form";
    }

    @GetMapping("/trips/{id}/edit")
    public String editTripForm(@PathVariable UUID id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        attachAdminUser(userDetails, model);
        model.addAttribute("tripRequest", tripService.getTripForEdit(id));
        enrichTripFormModel(model);
        model.addAttribute("editMode", true);
        return "Admin/trip-form";
    }

    @PostMapping("/trips/save")
    public String saveTrip(@Valid @ModelAttribute("tripRequest") TripRequestDTO requestDTO,
                           BindingResult result,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        attachAdminUser(userDetails, model);
        if (result.hasErrors()) {
            enrichTripFormModel(model);
            model.addAttribute("editMode", requestDTO.getId() != null);
            return "Admin/trip-form";
        }
        try {
            if (requestDTO.getId() == null) {
                var createdDriver = tripService.createTrip(requestDTO);
                redirectAttributes.addFlashAttribute("message", "Thêm mới chuyến đi thành công!");
                createdDriver.ifPresent(uuid -> redirectAttributes.addFlashAttribute("newDriverNotice",
                        "Đã tạo tài khoản tài xế mới (id: " + uuid + "). Mật khẩu tạm thời: "
                                + TripAdminConstants.NEW_DRIVER_TEMP_PASSWORD + " — vui lòng yêu cầu tài xế đổi mật khẩu sau khi đăng nhập."));
            } else {
                var createdDriver = tripService.updateTrip(requestDTO.getId(), requestDTO);
                redirectAttributes.addFlashAttribute("message", "Cập nhật chuyến đi thành công!");
                createdDriver.ifPresent(uuid -> redirectAttributes.addFlashAttribute("newDriverNotice",
                        "Đã tạo tài khoản tài xế mới (id: " + uuid + "). Mật khẩu tạm thời: "
                                + TripAdminConstants.NEW_DRIVER_TEMP_PASSWORD + " — vui lòng yêu cầu tài xế đổi mật khẩu sau khi đăng nhập."));
            }
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            enrichTripFormModel(model);
            model.addAttribute("editMode", requestDTO.getId() != null);
            return "Admin/trip-form";
        }
        return "redirect:/admin/trips";
    }

    private void attachAdminUser(UserDetails userDetails, Model model) {
        Account admin = accountRepository.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("adminUser", admin);
    }

    private void enrichTripFormModel(Model model) {
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("coaches", coachService.getAllCoaches());
        model.addAttribute("drivers", tripService.listAssignableDrivers());
        model.addAttribute("assistants", accountRepository.findByRoleAndStatusOrderByFullNameAsc("STAFF", StatusEnum.ACTIVE));
        model.addAttribute("tripStatuses", TripStatusEnum.values());
    }
}
