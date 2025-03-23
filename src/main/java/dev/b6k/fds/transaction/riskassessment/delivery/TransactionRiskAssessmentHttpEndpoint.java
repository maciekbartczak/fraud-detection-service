package dev.b6k.fds.transaction.riskassessment.delivery;

import dev.b6k.fds.model.TransactionRiskAssessmentRequest;
import dev.b6k.fds.model.TransactionRiskAssessmentResponse;
import dev.b6k.fds.rest.TransactionRiskAssessmentApi;
import dev.b6k.fds.transaction.riskassessment.TransactionRiskAssessmentService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class TransactionRiskAssessmentHttpEndpoint implements TransactionRiskAssessmentApi {
    private final TransactionRiskAssessmentService transactionRiskAssessmentService;

    @Override
    public TransactionRiskAssessmentResponse assessTransactionRisk(TransactionRiskAssessmentRequest request) {
        var transactionDetails = TransactionRiskAssessmentHttpEndpointHelper.toTransactionDetails(request);
        var result = transactionRiskAssessmentService.assessTransactionRisk(transactionDetails);

        return TransactionRiskAssessmentHttpEndpointHelper.toResponse(result);
    }
}