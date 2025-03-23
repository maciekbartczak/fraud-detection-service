package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.bin.BinNotFoundException;
import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.transaction.TransactionDetails;
import dev.b6k.fds.transaction.riskassessment.Score;
import dev.b6k.fds.transaction.riskassessment.riskfactor.RiskFactor;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
class BinDetailsBasedRiskFactorEvaluator implements RiskFactorEvaluator {
    private final BinDetailsProvider binDetailsProvider;
    private final RiskFactor.Weight weight;
    private final Score prepaidCardRiskScore;
    private final Score foreignCardRiskScore;
    private final Score highRiskCountryScore;
    private final Set<CountryCode> highRiskCountries;

    BinDetailsBasedRiskFactorEvaluator(
            BinDetailsProvider binDetailsProvider,
            @ConfigProperty(name = "fds.transaction.risk-assessment.bin.weight", defaultValue = "0.5")
            double weight,
            @ConfigProperty(name = "fds.transaction.risk-assessment.bin.prepaid-card-risk-score", defaultValue = "25")
            int prepaidCardRiskScore,
            @ConfigProperty(name = "fds.transaction.risk-assessment.bin.foreign-card-risk-score", defaultValue = "50")
            int foreignCardRiskScore,
            @ConfigProperty(name = "fds.transaction.risk-assessment.bin.high-risk-country-risk-score", defaultValue = "75")
            int highRiskCountryRiskScore,
            @ConfigProperty(name = "fds.transaction.risk-assessment.bin.high-risk-countries", defaultValue = "RUS,BLR,IRN,PRK,CUB,VEN")
            Set<String> highRiskCountries
    ) {
        this.binDetailsProvider = binDetailsProvider;
        this.weight = RiskFactor.Weight.of(weight);
        this.prepaidCardRiskScore = Score.of(prepaidCardRiskScore);
        this.foreignCardRiskScore = Score.of(foreignCardRiskScore);
        this.highRiskCountryScore = Score.of(highRiskCountryRiskScore);
        this.highRiskCountries = highRiskCountries.stream()
                .map(CountryCode::of)
                .collect(Collectors.toSet());
    }


    @Override
    public Set<RiskFactor> evaluate(TransactionDetails transaction) {
        var riskFactors = new HashSet<RiskFactor>();
        var binDetails = switch (binDetailsProvider.getBinDetails(transaction.bin())) {
            case BinDetailsProvider.Result.Success success -> success.details();
            case BinDetailsProvider.Result.NoData noData -> throw new BinNotFoundException(noData.reason());
        };

        if (binDetails.fundingSource() == BinDetails.FundingSource.PREPAID) {
            var riskFactor = RiskFactor.builder()
                    .score(prepaidCardRiskScore)
                    .weight(weight)
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
                    .score(foreignCardRiskScore)
                    .weight(weight)
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

        if (highRiskCountries.contains(issuerCountry)) {
            var riskFactor = RiskFactor.builder()
                    .score(highRiskCountryScore)
                    .weight(weight)
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
