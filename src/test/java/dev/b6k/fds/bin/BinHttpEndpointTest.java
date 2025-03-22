package dev.b6k.fds.bin;

import dev.b6k.fds.model.GetBINDetailsResponse;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class BinHttpEndpointTest {

    @Test
    void testBinEndpoint() {
        // given
        var binNumber = "123456";

        // when
        var response = callGetBinDetailsService(binNumber, GetBINDetailsResponse.class);

        // then
        assertEquals(binNumber, response.getBin());
        assertEquals("Poland", response.getCountry());
    }

    private <T> T callGetBinDetailsService(String binNumber, Class<T> responseClass) {
        return given()
                .pathParam("binNumber", binNumber)

                .when()
                .get("/api/v1/bin/{binNumber}")

                .then()
                .statusCode(200)
                .extract()
                .as(responseClass);
    }
}