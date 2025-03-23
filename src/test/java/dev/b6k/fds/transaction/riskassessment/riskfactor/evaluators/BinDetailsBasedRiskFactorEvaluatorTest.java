package dev.b6k.fds.transaction.riskassessment.riskfactor.evaluators;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.bin.BinNotFoundException;
import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.transaction.TransactionDetails;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BinDetailsBasedRiskFactorEvaluatorTest {
    private static final double WEIGHT = 0.5;
    private static final int PREPAID_CARD_RISK_SCORE = 25;
    private static final int FOREIGN_CARD_RISK_SCORE = 50;
    private static final int HIGH_RISK_COUNTRY_RISK_SCORE = 75;
    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of("RUS", "BLR");

    private static final BinDetails.BinDetailsBuilder NO_RISK_BIN_DETAILS_BUILDER = BinDetails.builder()
            .issuer(new BinDetails.Issuer(
                    "Bank",
                    new BinDetails.Country(CountryCode.of("POL"), "Poland")
            ))
            .billingCurrency(Currency.of("PLN"))
            .fundingSource(BinDetails.FundingSource.CREDIT)
            .accountHolderType(BinDetails.AccountHolderType.CONSUMER)
            .domesticUseOnly(false);

    @Test
    void returnPrepaidCardRiskFactor() {
        // given
        var binDetailsProvider = mock(BinDetailsProvider.class);
        var binDetails = BinDetails.builder()
                .issuer(new BinDetails.Issuer(
                        "Bank",
                        new BinDetails.Country(CountryCode.of("POL"), "Poland")
                ))
                .billingCurrency(Currency.of("PLN"))
                .fundingSource(BinDetails.FundingSource.PREPAID)
                .accountHolderType(BinDetails.AccountHolderType.CONSUMER)
                .domesticUseOnly(false)
                .build();

        var transaction = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        when(binDetailsProvider.getBinDetails(Mockito.any()))
                .thenReturn(new BinDetailsProvider.Result.Success(binDetails));

        var evaluator = new BinDetailsBasedRiskFactorEvaluator(
                binDetailsProvider,
                WEIGHT,
                PREPAID_CARD_RISK_SCORE,
                FOREIGN_CARD_RISK_SCORE,
                HIGH_RISK_COUNTRY_RISK_SCORE,
                HIGH_RISK_COUNTRIES
        );

        // when
        var riskFactors = evaluator.evaluate(transaction);

        // then
        assertThat(riskFactors)
                .hasSize(1)
                .first()
                .satisfies(riskFactor -> {
                    assertEquals("PREPAID_CARD", riskFactor.description().code());
                    assertEquals("Transaction is made with a prepaid card", riskFactor.description().message());
                    assertEquals(PREPAID_CARD_RISK_SCORE, riskFactor.score().value());
                    assertEquals(WEIGHT, riskFactor.weight().value());
                });
    }

    @Test
    void returnForeignCardRiskFactor() {
        // given
        var binDetailsProvider = mock(BinDetailsProvider.class);
        var binDetails = BinDetails.builder()
                .issuer(new BinDetails.Issuer(
                        "Bank",
                        new BinDetails.Country(CountryCode.of("POL"), "Poland")
                ))
                .billingCurrency(Currency.of("PLN"))
                .fundingSource(BinDetails.FundingSource.CREDIT)
                .accountHolderType(BinDetails.AccountHolderType.CONSUMER)
                .domesticUseOnly(false)
                .build();

        var transaction = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.of("USD"))
                .countryCode(CountryCode.of("USA"))
                .build();

        when(binDetailsProvider.getBinDetails(Mockito.any()))
                .thenReturn(new BinDetailsProvider.Result.Success(binDetails));

        var evaluator = new BinDetailsBasedRiskFactorEvaluator(
                binDetailsProvider,
                WEIGHT,
                PREPAID_CARD_RISK_SCORE,
                FOREIGN_CARD_RISK_SCORE,
                HIGH_RISK_COUNTRY_RISK_SCORE,
                HIGH_RISK_COUNTRIES
        );

        // when
        var riskFactors = evaluator.evaluate(transaction);

        // then
        assertThat(riskFactors)
                .hasSize(1)
                .first()
                .satisfies(riskFactor -> {
                    assertEquals("FOREIGN_CARD", riskFactor.description().code());
                    assertEquals(
                            "Card issuer country POL is different from transaction country USA",
                            riskFactor.description().message()
                    );
                    assertEquals(FOREIGN_CARD_RISK_SCORE, riskFactor.score().value());
                    assertEquals(WEIGHT, riskFactor.weight().value());
                });
    }

    @Test
    void returnHighRiskCountryRiskFactor() {
        // given
        var binDetailsProvider = mock(BinDetailsProvider.class);
        var binDetails = BinDetails.builder()
                .issuer(new BinDetails.Issuer(
                        "Bank",
                        new BinDetails.Country(CountryCode.of("RUS"), "Russia")
                ))
                .billingCurrency(Currency.of("PLN"))
                .fundingSource(BinDetails.FundingSource.CREDIT)
                .accountHolderType(BinDetails.AccountHolderType.CONSUMER)
                .domesticUseOnly(false)
                .build();

        var transaction = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.of("RBL"))
                .countryCode(CountryCode.of("RUS"))
                .build();

        when(binDetailsProvider.getBinDetails(Mockito.any()))
                .thenReturn(new BinDetailsProvider.Result.Success(binDetails));

        var evaluator = new BinDetailsBasedRiskFactorEvaluator(
                binDetailsProvider,
                WEIGHT,
                PREPAID_CARD_RISK_SCORE,
                FOREIGN_CARD_RISK_SCORE,
                HIGH_RISK_COUNTRY_RISK_SCORE,
                HIGH_RISK_COUNTRIES
        );

        // when
        var riskFactors = evaluator.evaluate(transaction);

        // then
        assertThat(riskFactors)
                .hasSize(1)
                .first()
                .satisfies(riskFactor -> {
                    assertEquals("HIGH_RISK_COUNTRY", riskFactor.description().code());
                    assertEquals(
                            "Card issuer country RUS is in high risk country list",
                            riskFactor.description().message()
                    );
                    assertEquals(HIGH_RISK_COUNTRY_RISK_SCORE, riskFactor.score().value());
                    assertEquals(WEIGHT, riskFactor.weight().value());
                });
    }

    @Test
    void returnNoRiskFactors() {
        // given
        var binDetailsProvider = mock(BinDetailsProvider.class);
        var binDetails = BinDetails.builder()
                .issuer(new BinDetails.Issuer(
                        "Bank",
                        new BinDetails.Country(CountryCode.of("POL"), "Poland")
                ))
                .billingCurrency(Currency.of("PLN"))
                .fundingSource(BinDetails.FundingSource.CREDIT)
                .accountHolderType(BinDetails.AccountHolderType.CONSUMER)
                .domesticUseOnly(false)
                .build();

        var transaction = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        when(binDetailsProvider.getBinDetails(Mockito.any()))
                .thenReturn(new BinDetailsProvider.Result.Success(binDetails));

        var evaluator = new BinDetailsBasedRiskFactorEvaluator(
                binDetailsProvider,
                WEIGHT,
                PREPAID_CARD_RISK_SCORE,
                FOREIGN_CARD_RISK_SCORE,
                HIGH_RISK_COUNTRY_RISK_SCORE,
                HIGH_RISK_COUNTRIES
        );

        // when
        var riskFactors = evaluator.evaluate(transaction);

        // then
        assertThat(riskFactors).isEmpty();
    }

    @Test
    void throwBinNotFoundExceptionWhenNoBinDataIsAvailable() {
        // given
        var binDetailsProvider = mock(BinDetailsProvider.class);
        var transaction = TransactionDetails.builder()
                .bin(Bin.of("123456"))
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.of("PLN"))
                .countryCode(CountryCode.of("POL"))
                .build();

        when(binDetailsProvider.getBinDetails(Mockito.any()))
                .thenReturn(new BinDetailsProvider.Result.NoData("Bin not found"));

        var evaluator = new BinDetailsBasedRiskFactorEvaluator(
                binDetailsProvider,
                WEIGHT,
                PREPAID_CARD_RISK_SCORE,
                FOREIGN_CARD_RISK_SCORE,
                HIGH_RISK_COUNTRY_RISK_SCORE,
                HIGH_RISK_COUNTRIES
        );

        // when & then
        assertThrows(BinNotFoundException.class, () -> evaluator.evaluate(transaction));
    }
}