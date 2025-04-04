package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.transaction.TransactionDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AmountBasedRiskFactorEvaluatorTest {
    private AmountBasedRiskFactorEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new AmountBasedRiskFactorEvaluator(
                0.3,
                20,
                10,
                new BigDecimal("1500.20")
        );
    }

    @Test
    void returnHighTransactionAmountRiskWhenAmountExceedsThreshold() {
        // given
        var transactionDetails = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(new BigDecimal("1500.25"))
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        // when
        var result = this.evaluator.evaluate(transactionDetails);

        // then
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(riskFactor -> {
                    assertEquals(20, riskFactor.score().value());
                    assertEquals(0.3, riskFactor.weight().value());
                    assertEquals("HIGH_TRANSACTION_AMOUNT", riskFactor.description().code());
                    assertEquals(
                            "Transaction amount 1500.25 is higher than threshold 1500.20",
                            riskFactor.description().message()
                    );
                });
    }

    @Test
    void returnRoundAmountRiskWhenAmountExceedsThreshold() {
        // given
        var transactionDetails = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(new BigDecimal("80.00"))
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        // when
        var result = this.evaluator.evaluate(transactionDetails);

        // then
        assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(riskFactor -> {
                    assertEquals(10, riskFactor.score().value());
                    assertEquals(0.3, riskFactor.weight().value());
                    assertEquals("ROUND_TRANSACTION_AMOUNT", riskFactor.description().code());
                    assertEquals(
                            "Transaction amount 80.00 is a round number",
                            riskFactor.description().message()
                    );
                });
    }

    @Test
    void returnBothRiskFactors() {
        // given
        var transactionDetails = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(new BigDecimal("2000.00"))
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        // when
        var result = this.evaluator.evaluate(transactionDetails);

        // then
        assertThat(result)
                .hasSize(2)
                .extracting(it -> it.score().value())
                .containsExactlyInAnyOrder(20, 10);
    }

    @Test
    void doNotReturnAnyRiskFactors() {
        // given
        var transactionDetails = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(new BigDecimal("123.56"))
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        // when
        var result = this.evaluator.evaluate(transactionDetails);

        // then
        assertThat(result).isEmpty();
    }
}