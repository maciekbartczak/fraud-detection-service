package dev.b6k.fds.transaction.riskassessment.delivery;

import dev.b6k.fds.model.TransactionRiskAssessmentRequest;
import dev.b6k.fds.model.TransactionRiskAssessmentResponse;
import dev.b6k.fds.rest.TransactionRiskAssessmentApi;
import dev.b6k.fds.transaction.riskassessment.TransactionRiskAssessmentService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;

@RolesAllowed("api")
@RequiredArgsConstructor
class TransactionRiskAssessmentHttpEndpoint implements TransactionRiskAssessmentApi {
    private final TransactionRiskAssessmentService transactionRiskAssessmentService;

    @Override
    public TransactionRiskAssessmentResponse assessTransactionRisk(TransactionRiskAssessmentRequest request) {
        TransactionRiskAssessmentHttpEndpointHelper.validateAmount(request.getAmount());

        var transactionDetails = TransactionRiskAssessmentHttpEndpointHelper.toTransactionDetails(request);
        var result = transactionRiskAssessmentService.assessTransactionRisk(transactionDetails);

        return TransactionRiskAssessmentHttpEndpointHelper.toResponse(result);
    }
}