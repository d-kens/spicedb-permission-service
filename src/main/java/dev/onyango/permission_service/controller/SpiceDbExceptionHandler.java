package dev.onyango.permission_service.controller;

import dev.onyango.permission_service.spicedb.SpiceDbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SpiceDbExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(SpiceDbExceptionHandler.class);

    @ExceptionHandler(SpiceDbException.class)
    public ProblemDetail handleSpiceDb(SpiceDbException ex) {
        HttpStatus status = switch (ex.getReason()) {
            case INVALID_REQUEST -> HttpStatus.BAD_REQUEST;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        if (status.is5xxServerError()) {
            log.error(ex.getMessage(), ex);
        } else {
            log.warn(ex.getMessage());
        }

        // Only echo SpiceDB's detail for client errors; server-side failures stay generic.
        String detail = status.is4xxClientError()
                ? ex.getCause().getMessage()
                : "Permission backend request failed";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        return problem;
    }
}
