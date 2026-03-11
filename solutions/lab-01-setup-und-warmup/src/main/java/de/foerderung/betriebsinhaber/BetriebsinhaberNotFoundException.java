package de.foerderung.betriebsinhaber;

public class BetriebsinhaberNotFoundException extends RuntimeException {

    private final Long betriebsinhaberId;

    public BetriebsinhaberNotFoundException(Long betriebsinhaberId) {
        super("Kein Betriebsinhaber mit ID %d gefunden".formatted(betriebsinhaberId));
        this.betriebsinhaberId = betriebsinhaberId;
    }

    public Long getBetriebsinhaberId() {
        return betriebsinhaberId;
    }
}
