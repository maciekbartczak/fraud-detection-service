package dev.b6k.fds;


import java.util.Objects;

/**
 * @param code Currency code in ISO 4217 format
 */
public record Currency(String code) {
    public Currency {
        Objects.requireNonNull(code, "Currency code must not be null");

        if (!code.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Currency code must be a 3-letter uppercase string");
        }
    }

    public static Currency of(String code) {
        return new Currency(code);
    }
}