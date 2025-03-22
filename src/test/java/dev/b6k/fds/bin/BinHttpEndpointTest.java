package dev.b6k.fds.bin;

import dev.b6k.fds.model.ErrorResponse;
import dev.b6k.fds.model.GetBinDetailsResponse;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class BinHttpEndpointTest {

    @Test
    void getBinDetails() {
        // given
        var binNumber = "123456";

        // when
        var response = callGetBinDetailsService(binNumber, 200, GetBinDetailsResponse.class);

        // then
        assertEquals(binNumber, response.getBin());
        assertEquals("Poland", response.getCountry());
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
        return given()
                .pathParam("binNumber", binNumber)

                .when()
                .get("/api/v1/bin/{binNumber}")

                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .as(responseClass);
    }
}