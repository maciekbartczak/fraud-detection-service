package dev.b6k.fds.transaction.riskassessment.delivery;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.model.TransactionRiskAssessmentRequest;
import dev.b6k.fds.model.TransactionRiskAssessmentResponse;
import dev.b6k.fds.model.TransactionRiskAssessmentResponseRiskFactorsInner;
import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.riskassessment.RiskLevel;
import dev.b6k.fds.transaction.riskassessment.TransactionRiskAssessment;
import jakarta.validation.ValidationException;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@UtilityClass
class TransactionRiskAssessmentHttpEndpointHelper {
    static void validateAmount(BigDecimal amount) {
        if (amount.scale() != 2) {
            throw new ValidationException("Amount must have two decimal places");
        }
    }

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
                .riskLevel(getRiskLevel(result.riskLevel()))
                .riskFactors(result.riskFactorDescriptions().stream()
                        .map(it -> TransactionRiskAssessmentResponseRiskFactorsInner.builder()
                                .factorName(it.code())
                                .factorDescription(it.message())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private static TransactionRiskAssessmentResponse.RiskLevelEnum getRiskLevel(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW -> TransactionRiskAssessmentResponse.RiskLevelEnum.LOW;
            case MEDIUM -> TransactionRiskAssessmentResponse.RiskLevelEnum.MEDIUM;
            case HIGH -> TransactionRiskAssessmentResponse.RiskLevelEnum.HIGH;
        };
    }
}