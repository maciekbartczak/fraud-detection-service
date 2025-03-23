package dev.b6k.fds.transaction;

import dev.b6k.fds.CountryCode;
import dev.b6k.fds.Currency;
import dev.b6k.fds.bin.Bin;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Objects;

@Builder
public record TransactionDetails(Bin bin, BigDecimal amount, Currency currency, CountryCode countryCode) {
    public TransactionDetails {
        Objects.requireNonNull(bin, "Bin cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(countryCode, "Country code cannot be null");
    }
}
