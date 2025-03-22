package dev.b6k.fds.bin;

import dev.b6k.fds.bin.details.BinDetailsProvider;
import dev.b6k.fds.model.GetBINDetailsResponse;
import dev.b6k.fds.rest.BinApi;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class BinHttpEndpoint implements BinApi {
    private final BinDetailsProvider binDetailsProvider;

    @Override
    public GetBINDetailsResponse getBINDetails(String binNumber) {
        validateBinNumber(binNumber);

        var details = binDetailsProvider.getBINDetails(binNumber);
        var response = new GetBINDetailsResponse();
        response.setBin(binNumber);
        response.setCountry("Poland");

        return response;
    }

    private void validateBinNumber(String binNumber) {
        if (binNumber == null || !binNumber.matches("^\\d{6,8}$")) {
            throw new ValidationException("BIN must be between 6 and 8 digits");
        }
    }
}