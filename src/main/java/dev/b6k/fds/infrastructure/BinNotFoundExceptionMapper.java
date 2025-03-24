package dev.b6k.fds.infrastructure;

import dev.b6k.fds.bin.BinNotFoundException;
import dev.b6k.fds.model.ErrorResponse;
import dev.b6k.fds.model.ErrorResponseErrorsInner;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
class BinNotFoundExceptionMapper implements ExceptionMapper<BinNotFoundException> {
    @Override
    public Response toResponse(BinNotFoundException e) {
        var errorResponse = ErrorResponse.builder()
                .errors(List.of(
                        ErrorResponseErrorsInner.builder()
                                .code("NOT_FOUND")
                                .message(e.getMessage())
                                .build()
                )).build();

        return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
    }
}