package dev.b6k.fds.bin;

import dev.b6k.fds.bin.details.BinDetails;
import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.model.GetBinDetailsResponse;
import dev.b6k.fds.model.GetBinDetailsResponseCountry;
import dev.b6k.fds.rest.BinApi;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
class BinHttpEndpoint implements BinApi {
    private final BinDetailsProvider binDetailsProvider;

    @Override
    public GetBinDetailsResponse getBinDetails(BigDecimal binNumber) {
        var bin = new Bin(binNumber);

        var details = binDetailsProvider.getBinDetails(bin);

        return makeGetBinDetailsResponse(details);
    }

    private GetBinDetailsResponse makeGetBinDetailsResponse(BinDetails details) {
        return GetBinDetailsResponse.builder()
                .bin(details.bin().value())
                .customerName(details.customerName())
                .country(GetBinDetailsResponseCountry.builder()
                        .code(details.country().code())
                        .name(details.country().name())
                        .build())
                .billingCurrency(details.billingCurrency())
                .fundingSource(mapFundingSource(details.fundingSource()))
                .consumerType(mapConsumerType(details.consumerType()))
                .localUse(details.localUse())
                .build();
    }

    private GetBinDetailsResponse.FundingSourceEnum mapFundingSource(BinDetails.FundingSource fundingSource) {
        return switch (fundingSource) {
            case DEBIT -> GetBinDetailsResponse.FundingSourceEnum.DEBIT;
            case CREDIT -> GetBinDetailsResponse.FundingSourceEnum.CREDIT;
            case PREPAID -> GetBinDetailsResponse.FundingSourceEnum.PREPAID;
            case NONE -> GetBinDetailsResponse.FundingSourceEnum.NONE;
        };
    }

    private GetBinDetailsResponse.ConsumerTypeEnum mapConsumerType(BinDetails.ConsumerType consumerType) {
        return switch (consumerType) {
            case CONSUMER -> GetBinDetailsResponse.ConsumerTypeEnum.CONSUMER;
            case CORPORATE -> GetBinDetailsResponse.ConsumerTypeEnum.CORPORATE;
        };
    }
}