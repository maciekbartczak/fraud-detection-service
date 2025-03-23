package dev.b6k.fds.bin.details;

import dev.b6k.fds.bin.Bin;
import lombok.Builder;

@Builder
public record BinDetails(
        Bin bin,
        Issuer issuer,
        Currency billingCurrency,
        FundingSource fundingSource,
        AccountHolderType accountHolderType,
        boolean domesticUseOnly
) {
    /**
     * @param code Country code in ISO 3166-1 alpha-3 format
     * @param name Human-readable country name
     */
    public record Country(String code, String name) {
        public Country {
            if (code == null || name == null) {
                throw new IllegalArgumentException("Country code and name must not be null");
            }

            if (!code.matches("^[A-Z]{3}$")) {
                throw new IllegalArgumentException("Country code must be a 3-letter uppercase string");
            }
        }
    }

    /**
     * @param code Currency code in ISO 4217 format
    */
    public record Currency(String code) {
        public Currency {
            if (code == null) {
                throw new IllegalArgumentException("Currency code must not be null");
            }

            if (!code.matches("^[A-Z]{3}$")) {
                throw new IllegalArgumentException("Currency code must be a 3-letter uppercase string");
            }
        }
    }

    public record Issuer(String name, Country country) {
        public Issuer {
            if (name == null || country == null) {
                throw new IllegalArgumentException("Issuer name and country must not be null");
            }
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
