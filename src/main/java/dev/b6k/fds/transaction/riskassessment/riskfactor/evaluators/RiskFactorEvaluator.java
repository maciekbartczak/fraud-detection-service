package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.TransactionEntity;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import lombok.Builder;

import java.util.List;
import java.util.Set;

public interface RiskFactorEvaluator {
    Set<RiskFactor> evaluate(EvaluationContext context);

    @Builder
    record EvaluationContext(
            TransactionDetails transactionDetails,
            BinDetails binDetails,
            List<TransactionEntity> pastTransactions
    ) {}
}