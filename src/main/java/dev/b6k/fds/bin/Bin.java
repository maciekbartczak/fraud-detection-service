package dev.b6k.fds.bin;

import java.math.BigDecimal;

public record Bin(BigDecimal value) {
    public Bin {
        if (value == null) {
            throw new IllegalArgumentException("Bin number cannot be null");
        }

        if (value.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Bin number must be an integer without decimal places");
        }

        var length = value.toBigInteger().toString().length();
        if (length < 6 || length > 11) {
            throw new IllegalArgumentException("Bin number must be between 6 and 11 digits long");
        }
    }

    public static Bin of(String binNumber) {
        try {
            return new Bin(new BigDecimal(binNumber));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid bin number", e);
        }
    }
}
