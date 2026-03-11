package de.foerderung.betriebsinhaber;

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
        problem.setTitle("Betriebsinhaber not found");
        problem.setProperty("betriebsinhaberId", ex.getBetriebsinhaberId());
        return problem;
    }
}
