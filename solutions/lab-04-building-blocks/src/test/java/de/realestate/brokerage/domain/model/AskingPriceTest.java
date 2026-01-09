package de.realestate.brokerage.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AskingPriceTest {

    @Test
    void validPrice_createsSuccessfully() {
        var price = new AskingPrice(new BigDecimal("350000"), "EUR");

        assertThat(price.amount()).isEqualByComparingTo("350000");
        assertThat(price.currency()).isEqualTo("EUR");
    }

    @Test
    void zeroAmount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new AskingPrice(BigDecimal.ZERO, "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeAmount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new AskingPrice(new BigDecimal("-1"), "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullAmount_throwsNullPointerException() {
        assertThatThrownBy(() -> new AskingPrice(null, "EUR"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void blankCurrency_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new AskingPrice(new BigDecimal("100000"), ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void twoPricesWithSameValues_areEqual() {
        var a = new AskingPrice(new BigDecimal("250000"), "EUR");
        var b = new AskingPrice(new BigDecimal("250000"), "EUR");

        assertThat(a).isEqualTo(b);
    }

}
