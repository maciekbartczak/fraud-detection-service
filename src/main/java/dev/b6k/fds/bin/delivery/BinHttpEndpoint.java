package dev.b6k.fds.bin.delivery;

import dev.b6k.fds.bin.Bin;
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

        return BinHttpEndpointHelper.makeGetBinDetailsResponse(details);
    }

}