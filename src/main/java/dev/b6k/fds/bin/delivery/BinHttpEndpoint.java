package dev.b6k.fds.bin.delivery;

import dev.b6k.fds.bin.Bin;
import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.model.GetBinDetailsResponse;
import dev.b6k.fds.rest.BinApi;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RolesAllowed("api")
@RequiredArgsConstructor
class BinHttpEndpoint implements BinApi {
    private final BinDetailsProvider binDetailsProvider;

    @Override
    public GetBinDetailsResponse getBinDetails(String bin) {
        var detailsResult = binDetailsProvider.getBinDetails(Bin.of(bin));

        return BinHttpEndpointHelper.toResponse(detailsResult);
    }
}