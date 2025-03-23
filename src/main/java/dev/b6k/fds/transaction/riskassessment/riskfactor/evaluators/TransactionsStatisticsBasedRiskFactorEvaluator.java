package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.TransactionRepository;
import dev.b6k.fds.transaction.riskassessment.Score;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
@RequiredArgsConstructor
class TransactionsStatisticsBasedRiskFactorEvaluator implements RiskFactorEvaluator {
    private static final RiskFactor.Weight WEIGHT = new RiskFactor.Weight(0.3);
    private static final int UNUSUAL_AMOUNT_RISK_SCORE = 30;
    private static final int FIRST_TRANSACTION_RISK_SCORE = 10;
    private static final BigDecimal UNUSUAL_AMOUNT_MULTIPLIER = new BigDecimal("3.0");

    private final TransactionRepository transactionRepository;

    @Override
    public Set<RiskFactor> evaluate(TransactionDetails transaction) {
        var riskFactors = new HashSet<RiskFactor>();
        var pastTransactionsStatistics = transactionRepository.getTransactionsStatistics(transaction.bin());

        if (pastTransactionsStatistics.totalCount() == 0) {
            riskFactors.add(RiskFactor.builder()
                    .score(Score.of(FIRST_TRANSACTION_RISK_SCORE))
                    .weight(WEIGHT)
                    .description(RiskFactor.Description.builder()
                            .code("FIRST_TRANSACTION")
                            .message("First transaction with this card")
                            .build()
                    ).build());

            // If this is the first transaction, we won't have any other useful statistics, return early.
            return riskFactors;
        }

        var unusualAmountThreshold = pastTransactionsStatistics.averageAmount().multiply(UNUSUAL_AMOUNT_MULTIPLIER);
        if (transaction.amount().compareTo(unusualAmountThreshold) > 0) {
            riskFactors.add(RiskFactor.builder()
                    .score(Score.of(UNUSUAL_AMOUNT_RISK_SCORE))
                    .weight(WEIGHT)
                    .description(RiskFactor.Description.builder()
                            .code("UNUSUAL_AMOUNT")
                            .message(
                                    String.format("Transaction amount %s is %s times higher than average (%s)",
                                            transaction.amount(),
                                            UNUSUAL_AMOUNT_MULTIPLIER,
                                            pastTransactionsStatistics.averageAmount())
                            )
                            .build()
                    ).build());
        }

        return riskFactors;
    }
}