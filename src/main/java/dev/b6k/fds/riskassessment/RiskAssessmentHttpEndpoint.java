package dev.b6k.fds.riskassessment;

import dev.b6k.fds.model.RiskAssessmentRequest;
import dev.b6k.fds.rest.RiskAssessmentApi;
import jakarta.ws.rs.core.Response;

public class RiskAssessmentHttpEndpoint implements RiskAssessmentApi {
    @Override
    public Response assessTransactionRisk(RiskAssessmentRequest riskAssessmentRequest) {
        return null;
    }
}
