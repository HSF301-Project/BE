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
import sp26.group.busticket.modules.repository.CoachTypeRepository;
import sp26.group.busticket.modules.entity.CoachType;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/admin/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;
    private final CoachTypeRepository coachTypeRepository;
    private final sp26.group.busticket.modules.repository.CoachRepository coachRepository;

    @GetMapping
    public String listCoaches(Model model) {
        model.addAttribute("coaches", coachService.getAllCoaches());
        return "Admin/coach-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("coachDTO", new CoachRequestDTO());
        model.addAttribute("coachTypes", coachTypeRepository.findAll());
        model.addAttribute("title", "Thêm xe mới");
        return "Admin/coach-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        model.addAttribute("coachDTO", coachService.getCoachById(id));
        model.addAttribute("coachTypes", coachTypeRepository.findAll());
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
            model.addAttribute("coachTypes", coachTypeRepository.findAll());
            return "Admin/coach-form";
        }

        try {
            coachService.createCoach(request);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu thông tin xe thành công!");
            return "redirect:/admin/coaches";
        } catch (AppException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("coachTypes", coachTypeRepository.findAll());
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
            model.addAttribute("coachTypes", coachTypeRepository.findAll());
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
            model.addAttribute("coachTypes", coachTypeRepository.findAll());
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

    @GetMapping("/detail/{id}")
    public String getCoachDetail(@PathVariable UUID id, Model model) {
        model.addAttribute("coachDetail", coachService.getCoachDetails(id));
        model.addAttribute("title", "Chi tiết phương tiện");
        return "Admin/coach-detail";
    }

    @GetMapping("/types")
    public String listCoachTypes(Model model) {
        model.addAttribute("coachTypes", coachTypeRepository.findAll());
        model.addAttribute("title", "Danh sách phân loại xe");
        return "Admin/coach-type";
    }

    @PostMapping("/add-type")
    public String addCoachType(@RequestParam String name, @RequestParam(required = false) String description, RedirectAttributes redirectAttributes) {
        try {
            CoachType type = new CoachType();
            type.setName(name);
            type.setDescription(description != null ? description : "Người dùng thêm");
            coachTypeRepository.save(type);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm dòng xe mới: " + name);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dòng xe này đã tồn tại hoặc có lỗi xảy ra!");
        }
        return "redirect:/admin/coaches/types";
    }

    @PostMapping("/update-type/{id}")
    public String updateCoachType(@PathVariable UUID id, @RequestParam String name, @RequestParam(required = false) String description, RedirectAttributes redirectAttributes) {
        try {
            CoachType type = coachTypeRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu"));
            type.setName(name);
            type.setDescription(description);
            coachTypeRepository.save(type);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phân loại xe thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên phân loại này đã tồn tại hoặc có lỗi xảy ra!");
        }
        return "redirect:/admin/coaches/types";
    }

    @PostMapping("/delete-type/{id}")
    public String deleteCoachType(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            long coachCount = coachRepository.countByCoachType_Id(id);
            if (coachCount > 0) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Không thể xóa vì đang có " + coachCount + " xe thuộc phân loại này!");
                return "redirect:/admin/coaches/types";
            }
            
            coachTypeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phân loại xe thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa phân loại xe!");
        }
        return "redirect:/admin/coaches/types";
    }
}
