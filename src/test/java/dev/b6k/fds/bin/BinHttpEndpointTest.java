package dev.b6k.fds.bin;

import dev.b6k.fds.MastercardBinApiStubHelper;
import dev.b6k.fds.MastercardBinApiTestProfile;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
@TestProfile(MastercardBinApiTestProfile.class)
class BinHttpEndpointTest {
    @Inject
    @CacheName("bin-details-cache")
    Cache binDetailsCache;

    @BeforeEach
    void setup() {
        binDetailsCache.invalidateAll().await().indefinitely();
        WireMockExtension.getWireMockServer().resetAll();
    }

    @Test
    void getBinDetails() {
        // given
        var bin = "123456";
        MastercardBinApiStubHelper.prepareSuccessResponse(bin);

        // when
        var response = callGetBinDetailsService(bin, 200, GetBinDetailsResponse.class);

        // then
        assertThat(response.getBin()).isEqualTo(bin);
        assertEquals("DFCC BANK PLC", response.getIssuerName());
        assertEquals("LKA", response.getIssuerCountry().getCode());
        assertEquals("Sri Lanka", response.getIssuerCountry().getName());
        assertEquals("LKR", response.getBillingCurrency());
        assertEquals(GetBinDetailsResponse.FundingSourceEnum.CREDIT, response.getFundingSource());
        assertEquals(GetBinDetailsResponse.ConsumerTypeEnum.CONSUMER, response.getConsumerType());
        assertEquals(false, response.getLocalUse());
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

    @Test
    void returnErrorResponseWhenGettingNonExistingBinDetails() {
        // given
        var binNumber = "654321";
        MastercardBinApiStubHelper.prepareNoDataResponse(binNumber);

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
        var binNumber = "654321";
        MastercardBinApiStubHelper.prepareSuccessResponse(binNumber);

        // when
        callGetBinDetailsService(binNumber, 200, GetBinDetailsResponse.class);

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

    @Nested
    class BinDetailsCache {
        @Test
        void cacheBinDetailsOnSuccessfulResponse() {
            // given
            var binNumber = "123456";
            MastercardBinApiStubHelper.prepareSuccessResponse(binNumber);

            // when
            callGetBinDetailsService(binNumber, 200, GetBinDetailsResponse.class);
            callGetBinDetailsService(binNumber, 200, GetBinDetailsResponse.class);

            // then
            WireMockExtension.getWireMockServer().verify(1, postRequestedFor(urlEqualTo("/bin-ranges/account-searches")));
        }

        @Test
        void cacheNonExistingBinDetailsResponse() {
            // given
            var binNumber = "55555567";
            MastercardBinApiStubHelper.prepareNoDataResponse(binNumber);

            // when
            callGetBinDetailsService(binNumber, 404, ErrorResponse.class);
            callGetBinDetailsService(binNumber, 404, ErrorResponse.class);

            // then
            WireMockExtension.getWireMockServer().verify(1, postRequestedFor(urlEqualTo("/bin-ranges/account-searches")));
        }

        @Test
        void doNotCacheBinDetailsOnErrorResponse() {
            // given
            WireMockExtension.getWireMockServer()
                    .stubFor(post(urlEqualTo("/bin-ranges/account-searches"))
                            .willReturn(aResponse()
                                    .withStatus(500))
                    );
            var binNumber = "123456";

            // when
            callGetBinDetailsService(binNumber, 500, ErrorResponse.class);
            callGetBinDetailsService(binNumber, 500, ErrorResponse.class);

            // then
            WireMockExtension.getWireMockServer().verify(2, postRequestedFor(urlEqualTo("/bin-ranges/account-searches")));
        }
    }

    private <T> T callGetBinDetailsService(String bin, int expectedStatusCode, Class<T> responseClass) {
        return given()
                .pathParam("bin", bin)

                .when()
                .get("/api/v1/bin/{bin}")

                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .as(responseClass);
    }
}