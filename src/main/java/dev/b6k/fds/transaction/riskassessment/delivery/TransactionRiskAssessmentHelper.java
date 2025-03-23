package dev.b6k.fds.transaction.riskassessment.delivery;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.model.TransactionRiskAssessmentRequest;
import dev.b6k.fds.model.TransactionRiskAssessmentResponse;
import dev.b6k.fds.model.TransactionRiskAssessmentResponseRiskFactorsInner;
import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.riskassessment.TransactionRiskAssessment;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

@UtilityClass
class TransactionRiskAssessmentHelper {
    static TransactionDetails toTransactionDetails(TransactionRiskAssessmentRequest request) {
        return TransactionDetails.builder()
                .bin(new Bin(request.getBin()))
                .amount(request.getAmount())
                .currency(Currency.of(request.getCurrency()))
                .countryCode(CountryCode.of(request.getLocation().getCountryCode()))
                .build();
    }

    static TransactionRiskAssessmentResponse toResponse(TransactionRiskAssessment result) {
        return TransactionRiskAssessmentResponse.builder()
                .riskScore(result.score().value())
                .riskFactors(result.riskFactorDescriptions().stream()
                        .map(it -> TransactionRiskAssessmentResponseRiskFactorsInner.builder()
                                .factorName(it.code())
                                .factorDescription(it.message())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}