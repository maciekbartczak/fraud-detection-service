package dev.b6k.fds;

import java.util.Objects;

/**
 * @param value Country code in ISO 3166-1 alpha-3 format
 */
public record CountryCode(String value) {
    public CountryCode {
        Objects.requireNonNull(value, "Country code must not be null");

        if (!value.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Country code must be a 3-letter uppercase string");
        }
    }

    public static CountryCode of(String value) {
        return new CountryCode(value);
    }
}