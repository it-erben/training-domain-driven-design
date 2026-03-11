package de.foerderung.betriebsinhaber.adapter.web;

import de.foerderung.betriebsinhaber.domain.model.BetriebsinhaberNichtGefundenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BetriebsinhaberExceptionHandler {

    @ExceptionHandler(BetriebsinhaberNichtGefundenException.class)
    public ProblemDetail handleNotFound(BetriebsinhaberNichtGefundenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Betriebsinhaber nicht gefunden");
        problem.setProperty("betriebsinhaberId", ex.getBetriebsinhaberId());
        return problem;
    }
}
