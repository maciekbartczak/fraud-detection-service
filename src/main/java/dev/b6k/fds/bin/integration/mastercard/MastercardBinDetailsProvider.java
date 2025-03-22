package dev.b6k.fds.bin.integration.mastercard;

import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.integration.mastercard.bin.api.ApiException;
import dev.b6k.fds.integration.mastercard.bin.api.BinLookupApi;
import dev.b6k.fds.integration.mastercard.bin.model.BinResource;
import dev.b6k.fds.integration.mastercard.bin.model.SearchByAccountRange;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@RequiredArgsConstructor
@IfBuildProperty(name = "fds.integration.mastercard.bin.enabled", stringValue = "true")
class MastercardBinDetailsProvider implements BinDetailsProvider {
    private final BinLookupApi client;

    @Override
    public BinDetails getBinDetails(Bin bin) {
        var searchByAccountRange = new SearchByAccountRange();
        searchByAccountRange.accountRange(bin.value());

        try {
            var response = client.searchByAccountRangeResources(searchByAccountRange);
            Log.debugv("Received BIN details from Mastercard API: {0}", response);

            return makeBinDetails(response);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    private BinDetails makeBinDetails(List<BinResource> response) {
        return Optional.ofNullable(response)
                .flatMap(it -> it.stream().findFirst())
                .map(it -> BinDetails.builder()
                        .bin(Bin.of(it.getBinNum()))
                        .customerName(it.getCustomerName())
                        .country(new BinDetails.Country(it.getCountry().getAlpha3(), it.getCountry().getName()))
                        .billingCurrency(it.getBillingCurrencyDefault())
                        .fundingSource(mapFundingSource(it.getFundingSource()))
                        .consumerType(mapConsumerType(it.getConsumerType()))
                        .localUse(it.getLocalUse())
                        .build())
                .orElseThrow();
    }

    private BinDetails.ConsumerType mapConsumerType(String consumerType) {
        return switch (consumerType) {
            case "CONSUMER" -> BinDetails.ConsumerType.CONSUMER;
            case "CORPORATE" -> BinDetails.ConsumerType.CORPORATE;
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