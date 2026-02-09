package be.wiserisk.hlabmonitor.monitor.infrastructure.adapter.in.rest;

import be.wiserisk.hlabmonitor.monitor.domain.exception.TargetDuplicatedException;
import be.wiserisk.hlabmonitor.monitor.domain.exception.TargetNotFoundException;
import be.wiserisk.hlabmonitor.monitor.domain.exception.UnsupportedEnumException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(TargetNotFoundException.class)
    public ProblemDetail targetNotFound(TargetNotFoundException ex, HttpServletRequest req) {
        log.info(ex.getMessage(), ex, req);
        return build(ex, req, HttpStatus.NOT_FOUND,
                "Target not found",
                "TARGET_NOT_FOUND",
                "https://example.com/problems/target-not-found");
    }

    @ExceptionHandler(TargetDuplicatedException.class)
    public ProblemDetail targetDuplicated(TargetDuplicatedException ex, HttpServletRequest req) {
        log.info(ex.getMessage(), ex, req);
        return build(ex, req, HttpStatus.CONFLICT,
                "Target duplicated",
                "TARGET_DUPLICATED",
                "https://example.com/problems/target-duplicated");
    }

    @ExceptionHandler(UnsupportedEnumException.class)
    public ProblemDetail unsupportedEnum(UnsupportedEnumException ex, HttpServletRequest req) {
        log.warn(ex.getMessage(), ex, req);
        return build(ex, req, HttpStatus.BAD_REQUEST,
                "Unsupported enum value",
                "UNSUPPORTED_ENUM",
                "https://example.com/problems/unsupported-enum");
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ProblemDetail notImplemented(UnsupportedOperationException ex, HttpServletRequest req) {
        log.warn(ex.getMessage(), ex, req);
        return build(ex, req, HttpStatus.NOT_IMPLEMENTED,
                "Not implemented",
                "NOT_IMPLEMENTED",
                "https://example.com/problems/not-implemented");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail fallback(Exception ex, HttpServletRequest req) {
        log.error(ex.getMessage(), ex, req);
        return build(req, HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "INTERNAL_ERROR",
                "https://example.com/problems/internal-error",
                "Unexpected error");
    }

    private ProblemDetail build(
            Exception ex,
            HttpServletRequest req,
            HttpStatus status,
            String title,
            String code,
            String typeUri
    ) {
        return build(req, status, title, code, typeUri, ex.getMessage());
    }

    private ProblemDetail build(
            HttpServletRequest req,
            HttpStatus status,
            String title,
            String code,
            String typeUri,
            String detail
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail != null ? detail : title);
        pd.setTitle(title);
        pd.setType(URI.create(typeUri));
        pd.setInstance(URI.create(req.getRequestURI()));

        pd.setProperty("errorCode", code);
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("path", req.getRequestURI());
        return pd;
    }
}