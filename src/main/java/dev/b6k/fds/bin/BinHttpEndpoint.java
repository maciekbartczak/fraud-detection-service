package dev.b6k.fds.bin;

import dev.b6k.fds.model.GetBINDetailsResponse;
import dev.b6k.fds.rest.BinApi;

public class BinHttpEndpoint implements BinApi {
    @Override
    public GetBINDetailsResponse getBINDetails(String binNumber) {
        var response = new GetBINDetailsResponse();
        response.setBin(binNumber);
        response.setCountry("Poland");

        return response;
    }
}
