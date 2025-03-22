package dev.b6k.fds.bin;

import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.model.GetBinDetailsResponse;
import dev.b6k.fds.rest.BinApi;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
class BinHttpEndpoint implements BinApi {
    private final BinDetailsProvider binDetailsProvider;

    @Override
    public GetBinDetailsResponse getBinDetails(BigDecimal binNumber) {
        var bin = new Bin(binNumber);

        var details = binDetailsProvider.getBinDetails(bin);
        var response = new GetBinDetailsResponse();
        response.setBin(bin.value().toString());
        response.setCountry("Poland");

        return response;
    }
}