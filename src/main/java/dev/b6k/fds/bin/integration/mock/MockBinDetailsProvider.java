package dev.b6k.fds.bin.integration.mock;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.bin.details.BinDetailsProvider;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProperty(name = "fds.integration.bin.provider", stringValue = "mock", enableIfMissing = true)
class MockBinDetailsProvider implements BinDetailsProvider {
    @Override
    public Result getBinDetails(Bin bin) {
        return new Result.Success(
                BinDetails.builder()
                        .bin(bin)
                        .issuer(new BinDetails.Issuer("Issuer", new BinDetails.Country(CountryCode.of("RUS"), "Russia")))
                        .billingCurrency(Currency.of("RBL"))
                        .fundingSource(BinDetails.FundingSource.DEBIT)
                        .accountHolderType(BinDetails.AccountHolderType.CONSUMER)
                        .domesticUseOnly(false)
                        .build()
        );
    }
}
