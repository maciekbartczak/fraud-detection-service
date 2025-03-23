package dev.b6k.fds.bin.integration.mastercard;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    @ConfigProperty(name = "fds.integration.mastercard.bin.base-url")
    String baseUrl;
    @ConfigProperty(name = "fds.integration.mastercard.bin.api-key")
    String apiKey;
    @ConfigProperty(name = "fds.integration.mastercard.bin.signing-key.path")
    String keyPath;
    @ConfigProperty(name = "fds.integration.mastercard.bin.signing-key.alias")
    String keyAlias;
    @ConfigProperty(name = "fds.integration.mastercard.bin.signing-key.password")
    String keyPassword;

    @ApplicationScoped
    BinLookupApi binLookupApi(MastercardBinApiAuthorizationFilter authorizationFilter) {
        return RestClientBuilder.newBuilder()
                .baseUri(URI.create(baseUrl))
                .register(authorizationFilter)
                .build(BinLookupApi.class);
    }

    @ApplicationScoped
    MastercardBinApiAuthorizationFilter authorizationFilter(ObjectMapper objectMapper, PrivateKey signingKey) {
        return new MastercardBinApiAuthorizationFilter(objectMapper, signingKey, apiKey);
    }

    @Singleton
    PrivateKey signingKey() {
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
