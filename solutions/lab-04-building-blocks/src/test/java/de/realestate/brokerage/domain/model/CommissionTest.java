package de.realestate.brokerage.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommissionTest {

    @Test
    void validCommission_createsSuccessfully() {
        var commission = new Commission(new BigDecimal("3.57"));

        assertThat(commission.percentage()).isEqualByComparingTo("3.57");
    }

    @Test
    void zeroPercentage_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new Commission(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativePercentage_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new Commission(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void over100Percentage_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new Commission(new BigDecimal("100.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exactly100Percentage_isValid() {
        var commission = new Commission(new BigDecimal("100"));

        assertThat(commission.percentage()).isEqualByComparingTo("100");
    }

    @Test
    void nullPercentage_throwsNullPointerException() {
        assertThatThrownBy(() -> new Commission(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void twoCommissionsWithSameValues_areEqual() {
        var a = new Commission(new BigDecimal("3.57"));
        var b = new Commission(new BigDecimal("3.57"));

        assertThat(a).isEqualTo(b);
    }

}
