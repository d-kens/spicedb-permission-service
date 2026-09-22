package dev.onyango.permission_service.controller;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
                .map(MessageSourceResolvable::getDefaultMessage)
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

    private ProblemDetail validationProblem(List<String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        problem.setTitle("Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }
}
