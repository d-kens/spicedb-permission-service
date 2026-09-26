package dev.onyango.permission_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidBody(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    // Nested fields (e.g. items[0].permission) are prefixed with their container path
                    if (error instanceof FieldError fe && fe.getField().contains(".")) {
                        return fe.getField().substring(0, fe.getField().lastIndexOf('.')) + " " + fe.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .toList();
        return validationProblem(errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleInvalidMethodArgs(HandlerMethodValidationException ex) {
        List<String> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> {
                    String prefix = result instanceof ParameterErrors pe && pe.getContainerIndex() != null
                            ? "[" + pe.getContainerIndex() + "] "
                            : "";
                    return result.getResolvableErrors().stream().map(e -> prefix + e.getDefaultMessage());
                })
                .toList();
        return validationProblem(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is missing or not valid JSON");
        problem.setTitle("Malformed request");
        return problem;
    }

    private ProblemDetail validationProblem(List<String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        problem.setTitle("Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }
}
