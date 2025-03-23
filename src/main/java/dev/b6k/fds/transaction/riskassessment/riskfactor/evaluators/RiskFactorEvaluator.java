package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;

import java.util.Set;

public interface RiskFactorEvaluator {
    Set<RiskFactor> evaluate(TransactionDetails transaction);
}