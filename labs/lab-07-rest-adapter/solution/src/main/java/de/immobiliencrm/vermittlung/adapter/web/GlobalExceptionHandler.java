package de.immobiliencrm.vermittlung.adapter.web;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import de.immobiliencrm.vermittlung.domain.model.VermittlungsvorgangNichtGefundenException;

/**
 * Global exception handler that translates domain and validation exceptions
 * into RFC 9457 ProblemDetail responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VermittlungsvorgangNichtGefundenException.class)
    public ProblemDetail handleNotFound(VermittlungsvorgangNichtGefundenException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Not Found");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, details);
        problemDetail.setTitle("Validation Error");
        return problemDetail;
    }
}
