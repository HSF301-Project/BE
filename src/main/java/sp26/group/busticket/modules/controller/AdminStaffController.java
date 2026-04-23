package sp26.group.busticket.modules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.modules.dto.account.request.StaffCreateRequestDTO;
import sp26.group.busticket.modules.dto.account.request.StaffUpdateRequestDTO;
import sp26.group.busticket.modules.dto.account.response.StaffResponseDTO;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.service.StaffService;

import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
public class AdminStaffController {

    private final StaffService staffService;

    @GetMapping
    public String listStaff(@RequestParam(name = "q", required = false) String q,
                            @RequestParam(name = "search", required = false) String search,
                            @RequestParam(name = "role", required = false) String role,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "10") int size,
                            Model model) {
        String keyword = StringUtils.hasText(search) ? search.trim() : (StringUtils.hasText(q) ? q.trim() : null);
        String selectedRole = StringUtils.hasText(role) ? role.trim().toUpperCase(Locale.ROOT) : "ALL";

        var staffPage = staffService.getStaffPage(keyword, selectedRole, page, size);
        model.addAttribute("staffPage", staffPage);
        model.addAttribute("users", staffPage.getContent());
        model.addAttribute("search", keyword == null ? "" : keyword);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("role", selectedRole);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "admin/user-list";
    }

    @PostMapping("/status/{staffId}")
    public String toggleStaffStatus(@PathVariable UUID staffId,
                                    RedirectAttributes redirectAttributes) {
        try {
            staffService.toggleStatus(staffId);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái nhân viên thành công");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getErrorCode().getDefaultMessage());
        }
        return "redirect:/admin/staff";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("staffForm", new StaffCreateRequestDTO());
        model.addAttribute("statuses", StatusEnum.values());
        model.addAttribute("isEdit", false);
        return "admin/staff/form";
    }

    @PostMapping("/create")
    public String createStaff(@Valid @ModelAttribute("staffForm") StaffCreateRequestDTO request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", StatusEnum.values());
            model.addAttribute("isEdit", false);
            return "admin/staff/form";
        }

        try {
            staffService.createStaff(request);
            redirectAttributes.addFlashAttribute("successMessage", "Tao tai khoan staff thanh cong");
            return "redirect:/admin/staff";
        } catch (AppException e) {
            model.addAttribute("statuses", StatusEnum.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("errorMessage", e.getErrorCode().getDefaultMessage());
            return "admin/staff/form";
        }
    }

    @GetMapping("/edit/{staffId}")
    public String showEditForm(@PathVariable UUID staffId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        StaffResponseDTO staff = staffService.getStaffById(staffId).orElse(null);
        if (staff == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay staff");
            return "redirect:/admin/staff";
        }

        StaffUpdateRequestDTO form = StaffUpdateRequestDTO.builder()
                .email(staff.getEmail())
                .fullName(staff.getFullName())
                .phone(staff.getPhone())
                .status(staff.getStatus())
                .build();

        model.addAttribute("staffId", staffId);
        model.addAttribute("staffForm", form);
        model.addAttribute("statuses", StatusEnum.values());
        model.addAttribute("isEdit", true);
        return "admin/staff/form";
    }

    @PostMapping("/edit/{staffId}")
    public String updateStaff(@PathVariable UUID staffId,
                              @Valid @ModelAttribute("staffForm") StaffUpdateRequestDTO request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("staffId", staffId);
            model.addAttribute("statuses", StatusEnum.values());
            model.addAttribute("isEdit", true);
            return "admin/staff/form";
        }

        try {
            boolean updated = staffService.updateStaff(staffId, request).isPresent();
            if (!updated) {
                redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay staff");
                return "redirect:/admin/staff";
            }
            redirectAttributes.addFlashAttribute("successMessage", "Cap nhat staff thanh cong");
            return "redirect:/admin/staff";
        } catch (AppException e) {
            model.addAttribute("staffId", staffId);
            model.addAttribute("statuses", StatusEnum.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("errorMessage", e.getErrorCode().getDefaultMessage());
            return "admin/staff/form";
        }
    }

    @PostMapping("/delete/{staffId}")
    public String deleteStaff(@PathVariable UUID staffId, RedirectAttributes redirectAttributes) {
        boolean deleted = staffService.deleteStaff(staffId);
        if (!deleted) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khong tim thay staff");
            return "redirect:/admin/staff";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Xoa staff thanh cong");
        return "redirect:/admin/staff";
    }
}

