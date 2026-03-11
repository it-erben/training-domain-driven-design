package de.foerderung.betriebsinhaber;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BetriebsinhaberExceptionHandler {

    @ExceptionHandler(BetriebsinhaberNotFoundException.class)
    public ProblemDetail handleNotFound(BetriebsinhaberNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Betriebsinhaber nicht gefunden");
        problem.setProperty("betriebsinhaberId", ex.getBetriebsinhaberId());
        return problem;
    }

    @ExceptionHandler(BetriebsinhaberBusinessRuleException.class)
    public ProblemDetail handleBusinessRuleViolation(BetriebsinhaberBusinessRuleException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Fachliche Regel verletzt");
        return problem;
    }
}
