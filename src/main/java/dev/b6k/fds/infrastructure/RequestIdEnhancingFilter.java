package dev.b6k.fds.infrastructure;

import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

@Provider
@Priority(1)
class RequestIdEnhancingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_KEY = RequestIdUtil.REQUEST_ID_MDC_KEY;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var requestId = Optional.ofNullable(requestContext.getHeaderString(REQUEST_ID_HEADER))
                .filter(it -> !it.isBlank())
                .orElseGet(() -> {
                    Log.info("Request ID not provided, falling back to a generated value");
                    return UUID.randomUUID().toString();
                });

        requestContext.setProperty(REQUEST_ID_KEY, requestId);
        MDC.put(REQUEST_ID_KEY, requestId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Optional.ofNullable(requestContext.getProperty(REQUEST_ID_KEY))
                .ifPresent(requestId -> responseContext.getHeaders().add(REQUEST_ID_HEADER, requestId.toString()));

        MDC.remove(REQUEST_ID_KEY);
    }
}