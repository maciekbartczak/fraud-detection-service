package dev.b6k.fds.integration.mastercard.bin;

import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.integration.mastercard.bin.api.ApiException;
import dev.b6k.fds.integration.mastercard.bin.api.BinLookupApi;
import dev.b6k.fds.integration.mastercard.bin.model.SearchByAccountRange;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@ApplicationScoped
@RequiredArgsConstructor
@IfBuildProperty(name = "fds.integration.mastercard.bin.enabled", stringValue = "true")
class MastercardBinDetailsProvider implements BinDetailsProvider {
    private final BinLookupApi client;

    @Override
    public BinDetails getBINDetails(String bin) {
        var searchByAccountRange = new SearchByAccountRange();
        searchByAccountRange.accountRange(new BigDecimal(bin));

        try {
            var response = client.searchByAccountRangeResources(searchByAccountRange);
            Log.debugv("Received BIN details from Mastercard API: {0}", response);
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
        return BinDetails.builder().build();
    }
}
