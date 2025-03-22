package dev.b6k.fds;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class MockCertificateTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "fds.integration.mastercard.bin.api-key", "mock-api-key",
                "fds.integration.mastercard.bin.signing-key.path", "src/test/resources/mock_certificate.p12",
                "fds.integration.mastercard.bin.signing-key.alias", "mock_certificate",
                "fds.integration.mastercard.bin.signing-key.password", "changeit"
        );
    }
}
