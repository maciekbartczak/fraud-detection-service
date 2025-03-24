package dev.b6k.fds.bin;

import java.math.BigDecimal;
import java.util.Objects;

public record Bin(String value) {
    public Bin {
        Objects.requireNonNull(value, "Bin cannot be null");

        if (!value.matches("^\\d{6,11}$")) {
            throw new IllegalArgumentException("Bin must be a number with 6 to 11 digits");
        }
    }

    public static Bin of(String bin) {
        return new Bin(bin);
    }

    public BigDecimal asBigDecimal() {
        return new BigDecimal(value);
    }
}