package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.riskassessment.Score;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
class AmountBasedRiskFactorEvaluator implements RiskFactorEvaluator {
    private final RiskFactor.Weight weight;
    private final Score highAmountRiskScore;
    private final Score roundAmountRiskScore;
    private final BigDecimal transactionHighAmountThreshold;

    AmountBasedRiskFactorEvaluator(
            @ConfigProperty(name = "fds.transaction.risk-assessment.amount.weight", defaultValue = "0.25")
            double weight,
            @ConfigProperty(name = "fds.transaction.risk-assessment.amount.high-amount-risk-score", defaultValue = "50")
            int highAmountRiskScore,
            @ConfigProperty(name = "fds.transaction.risk-assessment.amount.round-amount-risk-score", defaultValue = "10")
            int roundAmountRiskScore,
            @ConfigProperty(name = "fds.transaction.risk-assessment.amount.high-amount-threshold", defaultValue = "1000.00")
            BigDecimal transactionHighAmountThreshold
    ) {
        this.weight = RiskFactor.Weight.of(weight);
        this.highAmountRiskScore = Score.of(highAmountRiskScore);
        this.roundAmountRiskScore = Score.of(roundAmountRiskScore);
        this.transactionHighAmountThreshold = transactionHighAmountThreshold;
    }

    @Override
    public Set<RiskFactor> evaluate(TransactionDetails transaction) {
        var riskFactors = new HashSet<RiskFactor>();
        var amount = transaction.amount();

        if (amount.compareTo(transactionHighAmountThreshold) > 0) {
            var riskFactor = RiskFactor.builder()
                    .score(highAmountRiskScore)
                    .weight(weight)
                    .description(RiskFactor.Description.builder()
                            .code("HIGH_TRANSACTION_AMOUNT")
                            .message(
                                    String.format("Transaction amount %s is higher than threshold %s",
                                            amount,
                                            transactionHighAmountThreshold)
                            ).build()
                    ).build();
            riskFactors.add(riskFactor);
        }

        if (amount.remainder(BigDecimal.TEN).compareTo(BigDecimal.ZERO) == 0) {
            var riskFactor = RiskFactor.builder()
                    .score(roundAmountRiskScore)
                    .weight(weight)
                    .description(RiskFactor.Description.builder()
                            .code("ROUND_TRANSACTION_AMOUNT")
                            .message(String.format("Transaction amount %s is a round number", amount))
                            .build()
                    ).build();
            riskFactors.add(riskFactor);
        }

        return riskFactors;
    }
}