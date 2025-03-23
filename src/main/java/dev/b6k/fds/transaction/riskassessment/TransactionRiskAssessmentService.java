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

@ApplicationScoped
@RequiredArgsConstructor
public class TransactionRiskAssessmentService {
    private final TransactionRepository transactionRepository;
    private final DateTimeProvider dateTimeProvider;
    private final Instance<RiskFactorEvaluator> riskFactorEvaluators;

    private static final int MAX_RISK_SCORE = 100;

    @Transactional
    public TransactionRiskAssessment assessTransactionRisk(TransactionDetails transaction) {
        var riskFactors = riskFactorEvaluators.stream()
                .flatMap(evaluator -> evaluator.evaluate(transaction).stream())
                .toList();

        var riskScoreSum = riskFactors.stream()
                .mapToDouble(RiskFactor::getWeightedScore)
                .sum();
        var normalizedRiskScore = new Score(Math.min((int) Math.round(riskScoreSum), MAX_RISK_SCORE));

        var entity = makeEntity(transaction, normalizedRiskScore.value());
        transactionRepository.persist(entity);

        return TransactionRiskAssessment.builder()
                .score(normalizedRiskScore)
                .riskFactorDescriptions(riskFactors.stream().map(RiskFactor::description).toList())
                .build();
    }

    private TransactionEntity makeEntity(TransactionDetails transaction, int riskScore) {
        return TransactionEntity.builder()
                .bin(transaction.bin().value().toString())
                .amount(transaction.amount())
                .currency(transaction.currency().code())
                .countryCode(transaction.countryCode().value())
                .riskScore(riskScore)
                .timestamp(dateTimeProvider.now())
                .build();
    }
}