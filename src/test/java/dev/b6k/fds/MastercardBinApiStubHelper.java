package dev.b6k.fds;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.b6k.fds.integration.mastercard.bin.model.BinResource;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@UtilityClass
public class MastercardBinApiStubHelper {
    private static final String BIN_RESOURCE_JSON = """
            {
              "lowAccountRange": 5221696500000000000,
              "highAccountRange": 5221696600000000000,
              "binNum": "123456",
              "binLength": 6,
              "acceptanceBrand": "MCC",
              "ica": "00000022053",
              "customerName": "DFCC BANK PLC",
              "country": {
                "code": 144,
                "alpha3": "LKA",
                "name": "Sri Lanka"
              },
              "localUse": false,
              "authorizationOnly": false,
              "productCode": "MCW",
              "productDescription": "WORLD",
              "governmentRange": false,
              "nonReloadableIndicator": false,
              "anonymousPrepaidIndicator": "N",
              "cardholderCurrencyIndicator": "D",
              "billingCurrencyDefault": "LKR",
              "programName": null,
              "vertical": null,
              "fundingSource": "CREDIT",
              "consumerType": "CONSUMER",
              "smartDataEnabled": true,
              "affiliate": null,
              "comboCardIndicator": "N",
              "flexCardIndicator": "N",
              "gamblingBlockEnabled": false,
              "paymentAccountType": "P"
            }
            """;
    private static final String EMPTY_BIN_RANGE_RESPONSE_BODY = "[]";

    public static void prepareSuccessResponse(String bin) {
        prepareServiceResponse(bin, makeSingleBinLookupResponseBody(BIN_RESOURCE_JSON));
    }

    public static void prepareNoDataResponse(String bin) {
        prepareServiceResponse(bin, EMPTY_BIN_RANGE_RESPONSE_BODY);
    }

    public static void prepareErrorResponse() {
        WireMockExtension.getWireMockServer()
                .stubFor(post(urlEqualTo("/bin-ranges/account-searches"))
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withHeader("Content-Type", "application/json")
                                .withBody("Internal Server Error"))
                );
    }

    public static void prepareServiceResponse(String bin, String responseBody) {
        WireMockExtension.getWireMockServer()
                .stubFor(post(urlEqualTo("/bin-ranges/account-searches"))
                        .withRequestBody(matchingJsonPath("$.accountRange", equalTo(bin)))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody))
                );
    }

    @SneakyThrows
    public static void prepareCustomServiceResponse(String bin, Consumer<BinResource> apiResponseCustomizer) {
        var objectMapper = new ObjectMapper();
        var binResource = objectMapper.readValue(BIN_RESOURCE_JSON, BinResource.class);
        apiResponseCustomizer.accept(binResource);

        WireMockExtension.getWireMockServer()
                .stubFor(post(urlEqualTo("/bin-ranges/account-searches"))
                        .withRequestBody(matchingJsonPath("$.accountRange", equalTo(bin)))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(makeSingleBinLookupResponseBody(objectMapper.writeValueAsString(binResource))))
                );
    }

    private String makeSingleBinLookupResponseBody(String rawBinResourceJson) {
        return String.format("[%s]", rawBinResourceJson);
    }

}