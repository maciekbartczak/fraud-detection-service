package dev.b6k.fds.bin.delivery;

import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.model.GetBinDetailsResponse;
import dev.b6k.fds.model.GetBinDetailsResponseIssuerCountry;
import lombok.experimental.UtilityClass;

@UtilityClass
class BinHttpEndpointHelper {
    public static GetBinDetailsResponse makeGetBinDetailsResponse(BinDetails details) {
        return GetBinDetailsResponse.builder()
                .bin(details.bin().value())
                .issuerName(details.issuer().name())
                .issuerCountry(GetBinDetailsResponseIssuerCountry.builder()
                        .code(details.issuer().country().code())
                        .name(details.issuer().country().name())
                        .build())
                .billingCurrency(details.billingCurrency().code())
                .fundingSource(mapFundingSource(details.fundingSource()))
                .consumerType(mapConsumerType(details.accountHolderType()))
                .localUse(details.domesticUseOnly())
                .build();
    }

    private static GetBinDetailsResponse.FundingSourceEnum mapFundingSource(BinDetails.FundingSource fundingSource) {
        return switch (fundingSource) {
            case DEBIT -> GetBinDetailsResponse.FundingSourceEnum.DEBIT;
            case CREDIT -> GetBinDetailsResponse.FundingSourceEnum.CREDIT;
            case PREPAID -> GetBinDetailsResponse.FundingSourceEnum.PREPAID;
            case NONE -> GetBinDetailsResponse.FundingSourceEnum.NONE;
        };
    }

    private static GetBinDetailsResponse.ConsumerTypeEnum mapConsumerType(BinDetails.AccountHolderType accountHolderType) {
        return switch (accountHolderType) {
            case CONSUMER -> GetBinDetailsResponse.ConsumerTypeEnum.CONSUMER;
            case CORPORATE -> GetBinDetailsResponse.ConsumerTypeEnum.CORPORATE;
        };
    }
}
