package dev.b6k.fds.bin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public record Bin(BigInteger value) {
    public Bin {
        Objects.requireNonNull(value, "Bin number cannot be null");

        var length = value.toString().length();
        if (length < 6 || length > 11) {
            throw new IllegalArgumentException("Bin number must be between 6 and 11 digits long");
        }
    }

    public static Bin of(String binNumber) {
        try {
            return new Bin(new BigInteger(binNumber));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid bin number", e);
        }
    }

    public static Bin of(BigDecimal bin) {
        if (bin.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Bin number must be an integer without decimal places");
        }

        return new Bin(bin.toBigInteger());
    }

    public BigDecimal asBigDecimal() {
        return new BigDecimal(value);
    }
}