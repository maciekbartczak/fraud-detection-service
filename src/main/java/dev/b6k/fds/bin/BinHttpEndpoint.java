package dev.b6k.fds.bin;

import dev.b6k.fds.rest.BinApi;
import jakarta.ws.rs.core.Response;

public class BinHttpEndpoint implements BinApi {
    @Override
    public Response getBINDetails(String binNumber) {
        return Response.ok("Example response").build();
    }
}
