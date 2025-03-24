package dev.b6k.fds.transaction.riskassessment;

import dev.b6k.fds.DateTimeProvider;
import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.TransactionEntity;
import dev.b6k.fds.transaction.TransactionRepository;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators.RiskFactorEvaluator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@RequiredArgsConstructor
public class TransactionRiskAssessmentService {
    private static final int MAX_RISK_SCORE = 100;

    private final TransactionRepository transactionRepository;
    private final DateTimeProvider dateTimeProvider;
    private final Instance<RiskFactorEvaluator> riskFactorEvaluators;

    @ConfigProperty(name = "fds.transaction.risk-assessment.low-risk-threshold", defaultValue = "30")
    int lowRiskThreshold;
    @ConfigProperty(name = "fds.transaction.risk-assessment.medium-risk-threshold", defaultValue = "60")
    int mediumRiskThreshold;

    @Transactional
    public TransactionRiskAssessment assessTransactionRisk(TransactionDetails transaction) {
        var riskFactors = riskFactorEvaluators.stream()
                .flatMap(evaluator -> evaluator.evaluate(transaction).stream())
                .toList();

        var riskScoreSum = riskFactors.stream()
                .mapToDouble(RiskFactor::getWeightedScore)
                .sum();
        var normalizedRiskScore = new Score(Math.min((int) Math.round(riskScoreSum), MAX_RISK_SCORE));
        var riskLevel = getRiskLevel(normalizedRiskScore);

        var entity = makeEntity(transaction, normalizedRiskScore.value(), riskLevel);
        transactionRepository.persist(entity);

        return TransactionRiskAssessment.builder()
                .score(normalizedRiskScore)
                .riskLevel(riskLevel)
                .riskFactorDescriptions(riskFactors.stream().map(RiskFactor::description).toList())
                .build();
    }

    private RiskLevel getRiskLevel(Score normalizedRiskScore) {
        var value = normalizedRiskScore.value();

        if (value < lowRiskThreshold) {
            return RiskLevel.LOW;
        }

        if (value < mediumRiskThreshold) {
            return RiskLevel.MEDIUM;
        }

        return RiskLevel.HIGH;
    }

    private TransactionEntity makeEntity(TransactionDetails transaction, int riskScore, RiskLevel riskLevel) {
        return TransactionEntity.builder()
                .bin(transaction.bin().value().toString())
                .amount(transaction.amount())
                .currency(transaction.currency().code())
                .countryCode(transaction.countryCode().value())
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .timestamp(dateTimeProvider.now())
                .build();
    }
}