package dev.b6k.fds.infrastructure;

import dev.b6k.fds.model.ErrorResponse;
import dev.b6k.fds.model.ErrorResponseErrorsInner;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Override
    public Response toResponse(ValidationException e) {
        var errorResponse = ErrorResponse.builder()
                .errors(List.of(
                        ErrorResponseErrorsInner.builder()
                                .code("VALIDATION_ERROR")
                                .message(e.getMessage())
                                .build()
                )).build();

        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }
}