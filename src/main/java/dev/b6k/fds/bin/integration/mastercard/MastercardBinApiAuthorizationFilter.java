package dev.b6k.fds.bin.integration.mastercard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.developer.oauth.OAuth;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

@RequiredArgsConstructor
class MastercardBinApiAuthorizationFilter implements ClientRequestFilter {
    private final ObjectMapper objectMapper;
    private final PrivateKey signingKey;
    private final String apiKey;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        var entity = requestContext.getEntity();

        var payload = objectMapper.writeValueAsString(entity);
        var uri = requestContext.getUri();
        var method = requestContext.getMethod();
        var charset = StandardCharsets.UTF_8;

        var authHeader = OAuth.getAuthorizationHeader(uri, method, payload, charset, apiKey, signingKey);
        requestContext.getHeaders().add("Authorization", authHeader);
    }
}