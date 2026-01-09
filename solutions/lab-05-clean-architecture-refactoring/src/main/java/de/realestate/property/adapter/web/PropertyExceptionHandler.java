package de.realestate.property.adapter.web;

import de.realestate.property.domain.model.PropertyNotFoundException;
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
        problem.setTitle("Property not found");
        problem.setProperty("propertyId", ex.getPropertyId());
        return problem;
    }
}
