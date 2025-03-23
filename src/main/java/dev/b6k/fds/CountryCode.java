package dev.b6k.fds;

/**
 * @param value Country code in ISO 3166-1 alpha-3 format
 */
public record CountryCode(String value) {
    public CountryCode {
        if (value == null) {
            throw new IllegalArgumentException("Country code must not be null");
        }

        if (!value.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Country code must be a 3-letter uppercase string");
        }
    }

    public static CountryCode of(String value) {
        return new CountryCode(value);
    }
}
