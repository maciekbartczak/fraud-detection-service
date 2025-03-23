package dev.b6k.fds.transaction.riskassessment;

import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import lombok.Builder;

import java.util.List;

@Builder
public record RiskAssessment(Score score, List<RiskFactor.Description> riskFactorDescriptions) {
}
