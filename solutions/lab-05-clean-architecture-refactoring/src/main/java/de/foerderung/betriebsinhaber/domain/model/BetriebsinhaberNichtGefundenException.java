package de.foerderung.betriebsinhaber.domain.model;

public class BetriebsinhaberNichtGefundenException extends RuntimeException {

    private final Long betriebsinhaberId;

    public BetriebsinhaberNichtGefundenException(Long betriebsinhaberId) {
        super("Kein Betriebsinhaber mit ID %d gefunden".formatted(betriebsinhaberId));
        this.betriebsinhaberId = betriebsinhaberId;
    }

    public Long getBetriebsinhaberId() {
        return betriebsinhaberId;
    }
}
