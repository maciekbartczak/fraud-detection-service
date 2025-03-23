package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.TransactionRepository;
import dev.b6k.fds.transaction.TransactionRepository.TransactionStatistics.NoTransactions;
import dev.b6k.fds.transaction.TransactionRepository.TransactionStatistics.WithTransactions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransactionsStatisticsBasedRiskFactorEvaluatorTest {
    private static final int FIRST_TRANSACTION_RISK_SCORE = 25;
    private static final int UNUSUAL_AMOUNT_RISK_SCORE = 50;
    private static final double WEIGHT = 0.5;
    private static final double UNUSUAL_AMOUNT_MULTIPLIER = 3.0;

    @Test
    void returnFirstTransactionRiskFactorWhenNoTransactionsExist() {
        // given
        var transactionRepository = mock(TransactionRepository.class);
        when(transactionRepository.getTransactionsStatistics(any())).thenReturn(new NoTransactions());
        var transaction = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        var evaluator = new TransactionsStatisticsBasedRiskFactorEvaluator(
                transactionRepository,
                WEIGHT,
                UNUSUAL_AMOUNT_RISK_SCORE,
                FIRST_TRANSACTION_RISK_SCORE,
                UNUSUAL_AMOUNT_MULTIPLIER
        );

        // when
        var riskFactors = evaluator.evaluate(transaction);

        // then
        assertThat(riskFactors)
                .hasSize(1)
                .first()
                .satisfies(riskFactor -> {
                    assertEquals("FIRST_TRANSACTION", riskFactor.description().code());
                    assertEquals("First transaction with this card", riskFactor.description().message());
                    assertEquals(FIRST_TRANSACTION_RISK_SCORE, riskFactor.score().value());
                    assertEquals(WEIGHT, riskFactor.weight().value());
                });
    }

    @Test
    void returnUnusualAmountRiskFactorWhenTransactionAmountExceedsThreshold() {
        // given
        var averageAmount = BigDecimal.valueOf(100);
        var transactionRepository = mock(TransactionRepository.class);
        when(transactionRepository.getTransactionsStatistics(any()))
                .thenReturn(new WithTransactions(5, averageAmount));

        var evaluator = new TransactionsStatisticsBasedRiskFactorEvaluator(
                transactionRepository,
                WEIGHT,
                UNUSUAL_AMOUNT_RISK_SCORE,
                FIRST_TRANSACTION_RISK_SCORE,
                UNUSUAL_AMOUNT_MULTIPLIER
        );

        var unusualAmount = averageAmount
                .multiply(BigDecimal.valueOf(UNUSUAL_AMOUNT_MULTIPLIER))
                .add(BigDecimal.valueOf(10));

        var transaction = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(unusualAmount)
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        // when
        var riskFactors = evaluator.evaluate(transaction);

        // then
        assertThat(riskFactors)
                .hasSize(1)
                .first()
                .satisfies(riskFactor -> {
                    assertEquals("UNUSUAL_AMOUNT", riskFactor.description().code());
                    assertEquals(
                            "Transaction amount %s is considered higher than average (%s)".formatted(unusualAmount, averageAmount),
                            riskFactor.description().message()
                    );
                    assertEquals(UNUSUAL_AMOUNT_RISK_SCORE, riskFactor.score().value());
                    assertEquals(WEIGHT, riskFactor.weight().value());
                });
    }

    @Test
    void shouldReturnNoRiskFactorsWhenTransactionAmountWithinThreshold() {
        // given
        var averageAmount = BigDecimal.valueOf(100);
        var transactionRepository = mock(TransactionRepository.class);
        when(transactionRepository.getTransactionsStatistics(any()))
                .thenReturn(new WithTransactions(5, averageAmount));

        var evaluator = new TransactionsStatisticsBasedRiskFactorEvaluator(
                transactionRepository,
                WEIGHT,
                UNUSUAL_AMOUNT_RISK_SCORE,
                FIRST_TRANSACTION_RISK_SCORE,
                UNUSUAL_AMOUNT_MULTIPLIER
        );

        var usualAmount = averageAmount.add(BigDecimal.valueOf(12.45));
        var transaction = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(usualAmount)
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        // when
        var riskFactors = evaluator.evaluate(transaction);

        // then
        assertThat(riskFactors).isEmpty();
    }
}