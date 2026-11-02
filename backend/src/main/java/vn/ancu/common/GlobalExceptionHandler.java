package vn.ancu.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;

/**
 * Global exception handler — trả lỗi theo RFC 7807 (ProblemDetail).
 * Một nguồn lỗi không được làm hỏng cả báo cáo; controller phải xử lý
 * fallback trước khi lỗi bubble lên đây.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://ancu.vn/errors/validation"));
        problem.setTitle("Tham số không hợp lệ");
        problem.setDetail(ex.getBindingResult().getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst().orElse("Dữ liệu đầu vào không hợp lệ"));
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create("https://ancu.vn/errors/internal"));
        problem.setTitle("Lỗi hệ thống");
        // Không lộ stack trace ra ngoài
        problem.setDetail("Đã xảy ra lỗi. Vui lòng thử lại sau.");
        return problem;
    }
}

