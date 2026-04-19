package sp26.group.busticket.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // Bắt lỗi nghiệp vụ từ Service
    @ExceptionHandler(AppException.class)
    public ModelAndView handleAppException(AppException ex) {
        log.warn("Business Exception: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error/500"); 
        mav.addObject("errorMessage", ex.getErrorCode().getDefaultMessage());
        return mav;
    }

    // Bắt lỗi 403 - Cố tình truy cập trang không đủ quyền (Ví dụ User mò vào trang admin)
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());
        ModelAndView mav = new ModelAndView("error/403");
        mav.addObject("errorMessage", "Bạn không có quyền truy cập khu vực này!");
        return mav;
    }

    // Bắt lỗi 404 - Gõ sai URL
    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNotFound(NoResourceFoundException ex) {
        return new ModelAndView("error/404");
    }

    // Bắt mọi lỗi Exception còn lại (NullPointer, Database sập...)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpectedException(Exception ex) {
        log.error("Unexpected Error: ", ex);
        ModelAndView mav = new ModelAndView("error/500");
        mav.addObject("errorMessage", ErrorCode.UNEXPECTED_ERROR.getDefaultMessage());
        return mav;
    }
}
