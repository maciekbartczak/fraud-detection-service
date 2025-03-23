package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.bin.BinNotFoundException;
import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.riskassessment.Score;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
class BinDetailsBasedRiskFactorEvaluator implements RiskFactorEvaluator {
    private static final RiskFactor.Weight WEIGHT = new RiskFactor.Weight(0.5);
    private static final int PREPAID_CARD_RISK_SCORE = 25;
    private static final int FOREIGN_CARD_RISK_SCORE = 50;
    private static final int HIGH_RISK_COUNTRY_SCORE = 75;
    private static final Set<CountryCode> HIGH_RISK_COUNTRIES = Set.of("RUS", "BLR", "IRN", "PRK", "CUB", "VEN")
            .stream()
            .map(CountryCode::of)
            .collect(Collectors.toSet());

    private final BinDetailsProvider binDetailsProvider;

    @Override
    public Set<RiskFactor> evaluate(TransactionDetails transaction) {
        var riskFactors = new HashSet<RiskFactor>();
        var binDetails = switch (binDetailsProvider.getBinDetails(transaction.bin())) {
            case BinDetailsProvider.Result.Success success ->  success.details();
            case BinDetailsProvider.Result.NoData noData -> throw new BinNotFoundException(noData.reason());
        };

        if (binDetails.fundingSource() == BinDetails.FundingSource.PREPAID) {
            var riskFactor = RiskFactor.builder()
                    .score(Score.of(PREPAID_CARD_RISK_SCORE))
                    .weight(WEIGHT)
                    .description(
                            RiskFactor.Description.builder()
                                    .code("PREPAID_CARD")
                                    .message("Transaction is made with a prepaid card")
                                    .build()
                    ).build();
            riskFactors.add(riskFactor);
        }

        var issuerCountry = binDetails.issuer().country().code();
        var transactionCountry = transaction.countryCode();
        if (!issuerCountry.equals(transactionCountry)) {
            var riskFactor = RiskFactor.builder()
                    .score(Score.of(FOREIGN_CARD_RISK_SCORE))
                    .weight(WEIGHT)
                    .description(
                            RiskFactor.Description.builder()
                                    .code("FOREIGN_CARD")
                                    .message(String.format(
                                            "Card issuer country %s is different from transaction country %s",
                                            issuerCountry.value(),
                                            transactionCountry.value())
                                    ).build()
                    ).build();
            riskFactors.add(riskFactor);
        }

        if (HIGH_RISK_COUNTRIES.contains(issuerCountry)) {
            var riskFactor = RiskFactor.builder()
                    .score(Score.of(HIGH_RISK_COUNTRY_SCORE))
                    .weight(WEIGHT)
                    .description(
                            RiskFactor.Description.builder()
                                    .code("HIGH_RISK_COUNTRY")
                                    .message(String.format(
                                            "Card issuer country %s is in high risk country list",
                                            binDetails.issuer().country().code().value())
                                    ).build()
                    ).build();
            riskFactors.add(riskFactor);
        }

        return riskFactors;
    }
}
