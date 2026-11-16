package vn.ancu.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;

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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://ancu.vn/errors/validation"));
        problem.setTitle("Tham số không hợp lệ");
        problem.setDetail("Tham số '" + ex.getName() + "' không hợp lệ: " + ex.getValue());
        return problem;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://ancu.vn/errors/validation"));
        problem.setTitle("Thiếu tham số bắt buộc");
        problem.setDetail("Thiếu tham số: " + ex.getParameterName());
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://ancu.vn/errors/validation"));
        problem.setTitle("Giá trị ngoài phạm vi cho phép");
        problem.setDetail(ex.getMessage());
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

