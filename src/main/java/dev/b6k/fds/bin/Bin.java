package dev.b6k.fds.bin;

import java.math.BigDecimal;

public record Bin(BigDecimal value) {
    public Bin {
        if (value == null) {
            throw new IllegalArgumentException("Bin number cannot be null");
        }
    }
}
