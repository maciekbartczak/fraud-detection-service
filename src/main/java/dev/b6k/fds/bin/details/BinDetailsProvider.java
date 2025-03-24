package dev.b6k.fds.bin.details;

import dev.b6k.fds.bin.Bin;

public interface BinDetailsProvider {
    Result getBinDetails(Bin bin);

    sealed interface Result {
        record Success(BinDetails details) implements Result {}
        record NoData(String reason) implements Result {}
    }
}