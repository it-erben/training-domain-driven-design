package de.foerderung.antragstellung.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoerderbetragTest {

    @Test
    void shouldCreateValidFoerderbetrag() {
        var betrag = new Foerderbetrag(new BigDecimal("10000"), "EUR");

        assertThat(betrag.betrag()).isEqualByComparingTo("10000");
        assertThat(betrag.waehrung()).isEqualTo("EUR");
    }

    @Test
    void shouldAllowZeroBetrag() {
        var betrag = new Foerderbetrag(BigDecimal.ZERO, "EUR");

        assertThat(betrag.betrag()).isEqualByComparingTo("0");
    }

    @Test
    void shouldRejectNullBetrag() {
        assertThatThrownBy(() -> new Foerderbetrag(null, "EUR"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNegativeBetrag() {
        assertThatThrownBy(() -> new Foerderbetrag(new BigDecimal("-1"), "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNullWaehrung() {
        assertThatThrownBy(() -> new Foerderbetrag(BigDecimal.TEN, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectBlankWaehrung() {
        assertThatThrownBy(() -> new Foerderbetrag(BigDecimal.TEN, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualByValue() {
        var a = new Foerderbetrag(new BigDecimal("500"), "EUR");
        var b = new Foerderbetrag(new BigDecimal("500"), "EUR");

        assertThat(a).isEqualTo(b);
    }
}
