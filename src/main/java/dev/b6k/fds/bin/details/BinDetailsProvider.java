package dev.b6k.fds.bin.details;

import dev.b6k.fds.bin.Bin;

public interface BinDetailsProvider {
    GetBinDetailsResult getBinDetails(Bin bin);

    sealed interface GetBinDetailsResult {
        record Success(BinDetails details) implements GetBinDetailsResult {}
        record NoData(String reason) implements GetBinDetailsResult {}
    }
}

