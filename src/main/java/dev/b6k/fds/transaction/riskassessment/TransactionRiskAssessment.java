package dev.b6k.fds.transaction.riskassessment;

import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import lombok.Builder;

import java.util.List;
import java.util.Objects;

@Builder
public record TransactionRiskAssessment(Score score, RiskLevel riskLevel, List<RiskFactor.Description> riskFactorDescriptions) {
    public TransactionRiskAssessment {
        Objects.requireNonNull(score, "Score cannot be null");
        Objects.requireNonNull(riskLevel, "Level cannot be null");
        Objects.requireNonNull(riskFactorDescriptions, "Risk factor descriptions cannot be null");
    }
}
