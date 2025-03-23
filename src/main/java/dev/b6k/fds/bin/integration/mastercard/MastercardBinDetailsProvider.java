package dev.b6k.fds.bin.integration.mastercard;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.integration.mastercard.bin.api.ApiException;
import dev.b6k.fds.integration.mastercard.bin.api.BinLookupApi;
import dev.b6k.fds.integration.mastercard.bin.model.BinResource;
import dev.b6k.fds.integration.mastercard.bin.model.SearchByAccountRange;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.cache.CacheResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
@IfBuildProperty(name = "fds.integration.bin.provider", stringValue = "mastercard")
class MastercardBinDetailsProvider implements BinDetailsProvider {
    private final BinLookupApi client;

    @Override
    @CacheResult(cacheName = "bin-details-cache")
    public Result getBinDetails(Bin bin) {
        var searchByAccountRange = new SearchByAccountRange();
        searchByAccountRange.accountRange(bin.asBigDecimal());

        try {
            var response = client.searchByAccountRangeResources(searchByAccountRange);
            Log.debugv("Received BIN details from Mastercard API: {0}", response);

            return Optional.ofNullable(response)
                    .flatMap(it -> it.stream().findFirst())
                    .<Result>map(it -> new Result.Success(makeBinDetails(it)))
                    .orElseGet(() -> new Result.NoData("No data found for the given BIN in the Mastercard API"));
        } catch (ApiException e) {
            Log.error("Failed to retrieve BIN details from Mastercard API", e);
            throw new RuntimeException(e);
        }
    }

    private BinDetails makeBinDetails(BinResource bin) {
        return BinDetails.builder()
                .bin(Bin.of(bin.getBinNum()))
                .issuer(new BinDetails.Issuer(
                        bin.getCustomerName(),
                        new BinDetails.Country(CountryCode.of(bin.getCountry().getAlpha3()), bin.getCountry().getName())
                ))
                .billingCurrency(Currency.of(bin.getBillingCurrencyDefault()))
                .fundingSource(mapFundingSource(bin.getFundingSource()))
                .accountHolderType(mapAccountHolderType(bin.getConsumerType()))
                .domesticUseOnly(bin.getLocalUse())
                .build();
    }

    private BinDetails.AccountHolderType mapAccountHolderType(String consumerType) {
        return switch (consumerType) {
            case "CONSUMER" -> BinDetails.AccountHolderType.CONSUMER;
            case "CORPORATE" -> BinDetails.AccountHolderType.CORPORATE;
            default -> throw new IllegalStateException("Unexpected value: " + consumerType);
        };
    }

    private BinDetails.FundingSource mapFundingSource(String fundingSource) {
        return switch (fundingSource) {
            case "DEBIT" -> BinDetails.FundingSource.DEBIT;
            case "CREDIT" -> BinDetails.FundingSource.CREDIT;
            case "PREPAID" -> BinDetails.FundingSource.PREPAID;
            case "NONE" -> BinDetails.FundingSource.NONE;
            default -> throw new IllegalStateException("Unexpected value: " + fundingSource);
        };
    }
}