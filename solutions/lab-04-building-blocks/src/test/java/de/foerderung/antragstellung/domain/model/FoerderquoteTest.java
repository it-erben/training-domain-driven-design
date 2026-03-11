package de.foerderung.antragstellung.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoerderquoteTest {

    @Test
    void shouldCreateValidFoerderquote() {
        var quote = new Foerderquote(new BigDecimal("0.35"));

        assertThat(quote.prozentsatz()).isEqualByComparingTo("0.35");
    }

    @Test
    void shouldAllowZero() {
        var quote = new Foerderquote(BigDecimal.ZERO);

        assertThat(quote.prozentsatz()).isEqualByComparingTo("0");
    }

    @Test
    void shouldAllowOne() {
        var quote = new Foerderquote(BigDecimal.ONE);

        assertThat(quote.prozentsatz()).isEqualByComparingTo("1");
    }

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> new Foerderquote(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNegative() {
        assertThatThrownBy(() -> new Foerderquote(new BigDecimal("-0.1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectGreaterThanOne() {
        assertThatThrownBy(() -> new Foerderquote(new BigDecimal("1.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualByValue() {
        var a = new Foerderquote(new BigDecimal("0.50"));
        var b = new Foerderquote(new BigDecimal("0.50"));

        assertThat(a).isEqualTo(b);
    }
}
