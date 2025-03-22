package dev.b6k.fds.bin.details;

import dev.b6k.fds.bin.Bin;
import lombok.Builder;

@Builder
public record BinDetails(
        Bin bin,
        String customerName,
        Country country,
        String billingCurrency,
        FundingSource fundingSource,
        ConsumerType consumerType,
        boolean localUse
) {
    public record Country(String code, String name) {
        public Country {
            if (code == null || name == null) {
                throw new IllegalArgumentException("Country code and name must not be null");
            }
        }
    }

    public enum FundingSource {
        DEBIT,
        CREDIT,
        PREPAID,
        NONE;
    }

    public enum ConsumerType {
        CONSUMER,
        CORPORATE;
    }
}
