package de.realestate.property;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PropertyExceptionHandler {

    @ExceptionHandler(PropertyNotFoundException.class)
    public ProblemDetail handleNotFound(PropertyNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Property not found");
        problem.setProperty("propertyId", ex.getPropertyId());
        return problem;
    }

    @ExceptionHandler(PropertyBusinessRuleException.class)
    public ProblemDetail handleBusinessRuleViolation(PropertyBusinessRuleException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Business Rule Violation");
        return problem;
    }

}
