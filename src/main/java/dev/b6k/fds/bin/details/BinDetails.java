package dev.b6k.fds.bin.details;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import lombok.Builder;

import java.util.Objects;

@Builder
public record BinDetails(
        Bin bin,
        Issuer issuer,
        Currency billingCurrency,
        FundingSource fundingSource,
        AccountHolderType accountHolderType,
        boolean domesticUseOnly
) {
    public BinDetails {
        Objects.requireNonNull(bin, "Bin must not be null");
        Objects.requireNonNull(issuer, "Issuer must not be null");
        Objects.requireNonNull(billingCurrency, "Billing currency must not be null");
        Objects.requireNonNull(fundingSource, "Funding source must not be null");
        Objects.requireNonNull(accountHolderType, "Account holder type must not be null");
    }
    public record Country(CountryCode code, String name) {
        public Country {
            Objects.requireNonNull(code, "Country code must not be null");
            Objects.requireNonNull(name, "Country name must not be null");
        }
    }

    public record Issuer(String name, Country country) {
        public Issuer {
            Objects.requireNonNull(name, "Issuer name must not be null");
            Objects.requireNonNull(country, "Issuer country must not be null");
        }
    }

    public enum FundingSource {
        DEBIT,
        CREDIT,
        PREPAID,
        NONE
    }

    public enum AccountHolderType {
        CONSUMER,
        CORPORATE
    }
}