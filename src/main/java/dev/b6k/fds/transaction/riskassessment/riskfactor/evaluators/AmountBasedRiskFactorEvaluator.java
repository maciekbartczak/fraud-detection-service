package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.transaction.riskassessment.Score;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
class AmountBasedRiskFactorEvaluator implements RiskFactorEvaluator {
    private static final BigDecimal TRANSACTION_HIGH_AMOUNT_THRESHOLD = new BigDecimal("1000.00");
    private static final RiskFactor.Weight WEIGHT = new RiskFactor.Weight(0.3);
    public static final int HIGH_AMOUNT_RISK_SCORE = 20;
    public static final int ROUND_AMOUNT_RISK_SCORE = 10;

    @Override
    public Set<RiskFactor> evaluate(EvaluationContext context) {
        var riskFactors = new HashSet<RiskFactor>();
        var amount = context.transactionDetails().amount();

        if (amount.compareTo(TRANSACTION_HIGH_AMOUNT_THRESHOLD) > 0) {
            var riskFactor = RiskFactor.builder()
                    .score(Score.of(HIGH_AMOUNT_RISK_SCORE))
                    .weight(WEIGHT)
                    .description(RiskFactor.Description.builder()
                            .code("HIGH_TRANSACTION_AMOUNT")
                            .message(
                                    String.format("Transaction amount %s is higher than threshold %s",
                                            amount,
                                            TRANSACTION_HIGH_AMOUNT_THRESHOLD)
                            ).build()
                    );
            riskFactors.add(riskFactor.build());
        }

        if (amount.remainder(BigDecimal.TEN).compareTo(BigDecimal.ZERO) == 0) {
            var riskFactor = RiskFactor.builder()
                    .score(Score.of(ROUND_AMOUNT_RISK_SCORE))
                    .weight(WEIGHT)
                    .description(RiskFactor.Description.builder()
                            .code("ROUND_TRANSACTION_AMOUNT")
                            .message(String.format("Transaction amount %s is a round number", amount))
                            .build()
                    );
            riskFactors.add(riskFactor.build());
        }

        return riskFactors;
    }
}
