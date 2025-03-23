package dev.b6k.fds.transaction.riskassessment;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.model.RiskAssessmentRequest;
import dev.b6k.fds.model.RiskAssessmentResponse;
import dev.b6k.fds.model.RiskAssessmentResponseRiskFactorsInner;
import dev.b6k.fds.rest.RiskAssessmentApi;
import dev.b6k.fds.transaction.TransactionDetails;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

@RequiredArgsConstructor
class RiskAssessmentHttpEndpoint implements RiskAssessmentApi {
    private final RiskAssessmentService riskAssessmentService;

    @Override
    public RiskAssessmentResponse assessTransactionRisk(RiskAssessmentRequest request) {
        var transactionDetails = TransactionDetails.builder()
                .bin(new Bin(request.getBin()))
                .amount(request.getAmount())
                .currency(Currency.of(request.getCurrency()))
                .countryCode(CountryCode.of(request.getLocation().getCountryCode()))
                .build();

        var result = riskAssessmentService.assessTransactionRisk(transactionDetails);

        return RiskAssessmentResponse.builder()
                .riskScore(result.score().value())
                .riskFactors(result.riskFactorDescriptions().stream()
                        .map(it -> RiskAssessmentResponseRiskFactorsInner.builder()
                                .factorName(it.code())
                                .factorDescription(it.message())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
