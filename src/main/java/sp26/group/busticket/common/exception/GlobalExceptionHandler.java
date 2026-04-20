package sp26.group.busticket.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Bắt lỗi nghiệp vụ từ Service
    @ExceptionHandler(AppException.class)
    public String handleAppException(AppException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.warn("Business Exception: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/home");
    }

    // Bắt lỗi 403 - Cố tình truy cập trang không đủ quyền
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, RedirectAttributes redirectAttributes) {
        log.warn("Access Denied: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền truy cập khu vực này!");
        return "redirect:/home";
    }

    // Bắt lỗi 404 - Gõ sai URL
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(NoResourceFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Trang bạn tìm không tồn tại!");
        return "redirect:/home";
    }

    // Bắt mọi lỗi Exception còn lại
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleUnexpectedException(Exception ex) {
        log.error("Unexpected Error: ", ex);
        return "Đã xảy ra lỗi hệ thống: " + ex.getMessage();
    }
}
