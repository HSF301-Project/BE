package sp26.group.busticket.modules.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.modules.dto.coach.request.CoachRequestDTO;
import sp26.group.busticket.modules.service.CoachService;

import java.util.UUID;

@Controller
@RequestMapping("/admin/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    @GetMapping
    public String listCoaches(Model model) {
        model.addAttribute("coaches", coachService.getAllCoaches());
        return "Admin/coach-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("coachDTO", new CoachRequestDTO());
        model.addAttribute("title", "Thêm xe mới");
        return "Admin/coach-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        model.addAttribute("coachDTO", coachService.getCoachById(id));
        model.addAttribute("title", "Cập nhật thông tin xe");
        model.addAttribute("isEdit", true);
        model.addAttribute("coachId", id);
        return "Admin/coach-form";
    }

    @PostMapping("/save")
    public String saveCoach(@Valid @ModelAttribute("coachDTO") CoachRequestDTO request,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", request.getPlateNumber() != null ? "Cập nhật thông tin xe" : "Thêm xe mới");
            return "Admin/coach-form";
        }

        try {
            coachService.createCoach(request);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin xe thành công!");
            return "redirect:/admin/coaches";
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "Admin/coach-form";
        }
    }

    @PostMapping("/update/{id}")
    public String updateCoach(@PathVariable UUID id,
                              @Valid @ModelAttribute("coachDTO") CoachRequestDTO request,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Cập nhật thông tin xe");
            model.addAttribute("isEdit", true);
            model.addAttribute("coachId", id);
            return "Admin/coach-form";
        }

        try {
            coachService.updateCoach(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin xe thành công!");
            return "redirect:/admin/coaches";
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("coachId", id);
            return "Admin/coach-form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCoach(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            coachService.deleteCoach(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa xe thành công!");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/coaches";
    }
}
