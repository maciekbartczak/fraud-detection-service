package dev.b6k.fds.configuration;

import dev.b6k.fds.model.ErrorResponse;
import dev.b6k.fds.model.ErrorResponseErrorsInner;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable throwable) {
        var errorResponse = ErrorResponse.builder()
                .errors(List.of(
                        ErrorResponseErrorsInner.builder()
                                .code("INTERNAL_SERVER_ERROR")
                                .message("The server encountered an internal error. Please try again later.")
                                .build()
                ))
                .build();

        return Response.serverError().entity(errorResponse).build();
    }
}
