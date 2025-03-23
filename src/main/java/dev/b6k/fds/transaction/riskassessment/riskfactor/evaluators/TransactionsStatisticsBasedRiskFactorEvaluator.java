package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.TransactionRepository;
import dev.b6k.fds.transaction.riskassessment.Score;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
class TransactionsStatisticsBasedRiskFactorEvaluator implements RiskFactorEvaluator {
    private final RiskFactor.Weight weight;
    private final Score unusualAmountRiskScore;
    private final Score firstTransactionRiskScore;
    private final BigDecimal unusualAmountMultiplier;

    private final TransactionRepository transactionRepository;

    TransactionsStatisticsBasedRiskFactorEvaluator(
            TransactionRepository transactionRepository,
            @ConfigProperty(name = "fds.transaction.risk-assessment.statistics.weight", defaultValue = "0.5")
            double weight,
            @ConfigProperty(name = "fds.transaction.risk-assessment.statistics.unusual-amount-risk-score", defaultValue = "50")
            int unusualAmountRiskScore,
            @ConfigProperty(name = "fds.transaction.risk-assessment.statistics.first-transaction-risk-score", defaultValue = "25")
            int firstTransactionRiskScore,
            @ConfigProperty(name = "fds.transaction.risk-assessment.statistics.unusual-amount-multiplier", defaultValue = "3.0")
            double unusualAmountMultiplier
    ) {
        this.transactionRepository = transactionRepository;
        this.weight = RiskFactor.Weight.of(weight);
        this.unusualAmountRiskScore = Score.of(unusualAmountRiskScore);
        this.firstTransactionRiskScore = Score.of(firstTransactionRiskScore);
        this.unusualAmountMultiplier = BigDecimal.valueOf(unusualAmountMultiplier);
    }

    @Override
    public Set<RiskFactor> evaluate(TransactionDetails transaction) {
        var riskFactors = new HashSet<RiskFactor>();
        var pastTransactionsStatistics = transactionRepository.getTransactionsStatistics(transaction.bin());

        if (pastTransactionsStatistics.totalCount() == 0) {
            riskFactors.add(RiskFactor.builder()
                    .score(firstTransactionRiskScore)
                    .weight(weight)
                    .description(RiskFactor.Description.builder()
                            .code("FIRST_TRANSACTION")
                            .message("First transaction with this card")
                            .build()
                    ).build());

            // If this is the first transaction, we won't have any other useful statistics, return early.
            return riskFactors;
        }

        var unusualAmountThreshold = pastTransactionsStatistics.averageAmount().multiply(unusualAmountMultiplier);
        if (transaction.amount().compareTo(unusualAmountThreshold) > 0) {
            riskFactors.add(RiskFactor.builder()
                    .score(unusualAmountRiskScore)
                    .weight(weight)
                    .description(RiskFactor.Description.builder()
                            .code("UNUSUAL_AMOUNT")
                            .message(
                                    String.format("Transaction amount %s is %s times higher than average (%s)",
                                            transaction.amount(),
                                            unusualAmountMultiplier,
                                            pastTransactionsStatistics.averageAmount())
                            )
                            .build()
                    ).build());
        }

        return riskFactors;
    }
}