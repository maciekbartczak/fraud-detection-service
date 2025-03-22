package dev.b6k.fds.integration.mastercard.bin;

import com.mastercard.developer.utils.AuthenticationUtils;
import dev.b6k.fds.integration.mastercard.bin.api.BinLookupApi;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.net.URI;
import java.security.PrivateKey;

@RequiredArgsConstructor
@IfBuildProperty(name = "fds.integration.mastercard.bin.enabled", stringValue = "true")
class MastercardApiClientConfiguration {
    private final MastercardBinApiAuthorizationFilter mastercardBinApiAuthorizationFilter;

    @ConfigProperty(name = "fds.integration.mastercard.bin.base-url")
    String baseUrl;

    @ApplicationScoped
    BinLookupApi binLookupApi() {
        return RestClientBuilder.newBuilder()
                .baseUri(URI.create(baseUrl))
                .register(mastercardBinApiAuthorizationFilter)
                .build(BinLookupApi.class);
    }

    @Singleton
    PrivateKey getSigningKey(
            @ConfigProperty(name = "fds.integration.mastercard.bin.signing-key.path") String keyPath,
            @ConfigProperty(name = "fds.integration.mastercard.bin.signing-key.alias") String keyAlias,
            @ConfigProperty(name = "fds.integration.mastercard.bin.signing-key.password") String keyPassword
    ) {
        try {
            Log.infov("Loading signing key from {0}", keyPath);
            var key = AuthenticationUtils.loadSigningKey(keyPath, keyAlias, keyPassword);
            Log.info("Signing key loaded successfully");
            return key;
        } catch (Exception e) {
            Log.error("Failed to load signing key", e);
            throw new RuntimeException(e);
        }
    }
}
