package dev.b6k.fds.transaction.riskassessment.delivery;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.model.TransactionRiskAssessmentRequest;
import dev.b6k.fds.model.TransactionRiskAssessmentResponse;
import dev.b6k.fds.model.TransactionRiskAssessmentResponseRiskFactorsInner;
import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.riskassessment.Score;
import dev.b6k.fds.transaction.riskassessment.TransactionRiskAssessment;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

@UtilityClass
class TransactionRiskAssessmentHttpEndpointHelper {
    // TODO: load this from configuration, possibly move to AssessmentService
    private static final int LOW_RISK_THRESHOLD = 30;
    private static final int MEDIUM_RISK_THRESHOLD = 60;

    static TransactionDetails toTransactionDetails(TransactionRiskAssessmentRequest request) {
        return TransactionDetails.builder()
                .bin(Bin.of(request.getBin()))
                .amount(request.getAmount())
                .currency(Currency.of(request.getCurrency()))
                .countryCode(CountryCode.of(request.getLocation().getCountryCode()))
                .build();
    }

    static TransactionRiskAssessmentResponse toResponse(TransactionRiskAssessment result) {
        return TransactionRiskAssessmentResponse.builder()
                .riskScore(result.score().value())
                .riskLevel(getRiskLevel(result.score()))
                .riskFactors(result.riskFactorDescriptions().stream()
                        .map(it -> TransactionRiskAssessmentResponseRiskFactorsInner.builder()
                                .factorName(it.code())
                                .factorDescription(it.message())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private static TransactionRiskAssessmentResponse.RiskLevelEnum getRiskLevel(Score score) {
        var scoreValue = score.value();

        if (scoreValue < LOW_RISK_THRESHOLD) {
            return TransactionRiskAssessmentResponse.RiskLevelEnum.LOW;
        }

        if (scoreValue < MEDIUM_RISK_THRESHOLD) {
            return TransactionRiskAssessmentResponse.RiskLevelEnum.MEDIUM;
        }

        return TransactionRiskAssessmentResponse.RiskLevelEnum.HIGH;
    }
}