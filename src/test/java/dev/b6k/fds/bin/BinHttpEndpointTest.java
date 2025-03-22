package dev.b6k.fds.bin;

import dev.b6k.fds.MockCertificateTestProfile;
import dev.b6k.fds.WireMockExtension;
import dev.b6k.fds.model.ErrorResponse;
import dev.b6k.fds.model.GetBinDetailsResponse;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
@TestProfile(MockCertificateTestProfile.class)
class BinHttpEndpointTest {
    @Inject
    @CacheName("bin-details-cache")
    Cache binDetailsCache;

    private static final String BIN_RANGE_RESPONSE_BODY = """
            [
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
            ]""";

    @BeforeEach
    void setup() {
        binDetailsCache.invalidateAll().await().indefinitely();
        WireMockExtension.getWireMockServer().resetAll();
    }

    @Test
    void getBinDetails() {
        // given
        WireMockExtension.getWireMockServer()
                .stubFor(post(urlEqualTo("/bin-ranges/account-searches"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(BIN_RANGE_RESPONSE_BODY))
                );
        var binNumber = "123456";

        // when
        var response = callGetBinDetailsService(binNumber, 200, GetBinDetailsResponse.class);

        // then
        assertThat(response.getBin()).isEqualTo(binNumber);
        assertEquals("DFCC BANK PLC", response.getCustomerName());
        assertEquals("LKA", response.getCountry().getCode());
        assertEquals("Sri Lanka", response.getCountry().getName());
        assertEquals("LKR", response.getBillingCurrency());
        assertEquals(GetBinDetailsResponse.FundingSourceEnum.CREDIT, response.getFundingSource());
        assertEquals(GetBinDetailsResponse.ConsumerTypeEnum.CONSUMER, response.getConsumerType());
        assertEquals(false, response.getLocalUse());
    }

    @Test
    void returnErrorResponseWhenGettingNonExistingBinDetails() {
        // given
        WireMockExtension.getWireMockServer()
                .stubFor(post(urlEqualTo("/bin-ranges/account-searches"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("[]"))
                );
        var binNumber = "123456";

        // when
        var response = callGetBinDetailsService(binNumber, 404, ErrorResponse.class);

        // then
        assertThat(response.getErrors())
                .hasSize(1)
                .first()
                .satisfies(error -> {
                    assertEquals("NOT_FOUND", error.getCode());
                    assertThat(error.getMessage()).isNotBlank();
                });
    }

    @Test
    void setCorrectAuthorizationHeaderWhenCallingMastercardApi() {
        // given
        WireMockExtension.getWireMockServer()
                .stubFor(post(urlEqualTo("/bin-ranges/account-searches"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("[]"))
                );
        var binNumber = "123456";

        // when
        callGetBinDetailsService(binNumber, 404, ErrorResponse.class);

        // then
        var requests = WireMockExtension.getWireMockServer()
                .findAll(postRequestedFor(urlEqualTo("/bin-ranges/account-searches")));
        assertThat(requests).hasSize(1);

        var authHeader = requests.getFirst().getHeader("Authorization");
        assertThat(authHeader)
                .isNotNull()
                .startsWith("OAuth")
                .contains("oauth_consumer_key=\"mock-api-key\"")
                .containsPattern("oauth_signature=.*");
    }

    @Test
    void cacheBinDetailsOnSuccessfulResponse() {
        WireMockExtension.getWireMockServer()
                .stubFor(post(urlEqualTo("/bin-ranges/account-searches"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(BIN_RANGE_RESPONSE_BODY))
                );
        var binNumber = "123456";

        // when
        callGetBinDetailsService(binNumber, 200, GetBinDetailsResponse.class);
        callGetBinDetailsService(binNumber, 200, GetBinDetailsResponse.class);

        // then
        WireMockExtension.getWireMockServer().verify(1, postRequestedFor(urlEqualTo("/bin-ranges/account-searches")));
    }

    @Test
    void cacheNonExistingBinDetailsResponse() {
        WireMockExtension.getWireMockServer()
                .stubFor(post(urlEqualTo("/bin-ranges/account-searches"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("[]"))
                );
        var binNumber = "123456";

        // when
        callGetBinDetailsService(binNumber, 404, ErrorResponse.class);
        callGetBinDetailsService(binNumber, 404, ErrorResponse.class);

        // then
        WireMockExtension.getWireMockServer().verify(1, postRequestedFor(urlEqualTo("/bin-ranges/account-searches")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "123456789012"})
    void validateBin(String invalidBinNumber) {
        // given & when
        var response = callGetBinDetailsService(invalidBinNumber, 400, ErrorResponse.class);

        // then
        assertThat(response.getErrors())
                .hasSize(1)
                .first()
                .satisfies(error -> {
                    assertEquals("VALIDATION_ERROR", error.getCode());
                    assertThat(error.getMessage()).isNotBlank();
                });
    }

    private <T> T callGetBinDetailsService(String binNumber, int expectedStatusCode, Class<T> responseClass) {
        return given().pathParam("binNumber", binNumber)

                .when().get("/api/v1/bin/{binNumber}")

                .then().statusCode(expectedStatusCode).extract().as(responseClass);
    }
}