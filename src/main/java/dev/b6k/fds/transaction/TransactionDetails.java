package dev.b6k.fds.transaction;

import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.bin.details.BinDetails;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Objects;

@Builder
public record TransactionDetails(Bin bin, BigDecimal amount, BinDetails.Currency currency, String countryCode) {
    public TransactionDetails {
        Objects.requireNonNull(bin, "Bin cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Objects.requireNonNull(countryCode, "Country code cannot be null");
    }
}
