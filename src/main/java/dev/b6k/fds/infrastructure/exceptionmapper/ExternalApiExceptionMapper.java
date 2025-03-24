package dev.b6k.fds.infrastructure.exceptionmapper;

import dev.b6k.fds.bin.integration.ExternalApiException;
import dev.b6k.fds.model.ErrorResponse;
import dev.b6k.fds.model.ErrorResponseErrorsInner;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
class ExternalApiExceptionMapper implements ExceptionMapper<ExternalApiException> {
    @Override
    public Response toResponse(ExternalApiException e) {
        var errorResponse = ErrorResponse.builder()
                .errors(List.of(
                        ErrorResponseErrorsInner.builder()
                                .code("EXTERNAL_API_ERROR")
                                .message(e.getMessage())
                                .build()
                )).build();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
    }
}