package dev.b6k.fds;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class MastercardBinApiTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "fds.integration.bin.provider", "mastercard",
                "fds.integration.bin.mastercard.api-key", "mock-api-key",
                "fds.integration.bin.mastercard.signing-key.path", "src/test/resources/mock_certificate.p12",
                "fds.integration.bin.mastercard.signing-key.alias", "mock_certificate",
                "fds.integration.bin.mastercard.signing-key.password", "changeit"
        );
    }
}
