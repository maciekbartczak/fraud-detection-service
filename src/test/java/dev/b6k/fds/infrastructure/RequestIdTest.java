package dev.b6k.fds.infrastructure;

import dev.b6k.fds.BaseHttpEndpointTest;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest

class RequestIdTest extends BaseHttpEndpointTest {

    @Test
    void generateRequestIdWhenHeaderIsNotProvided() {
        given()

                .when()
                .get("/api/v1/bin/123456")

                .then()
                .statusCode(200)
                .header("X-Request-ID", not(blankString()));
    }

    @Test
    void generateRequestIdWhenProvidedHeaderIsEmpty() {
        given()
                .header("X-Request-ID", "")

                .when()
                .get("/api/v1/bin/123456")

                .then()
                .statusCode(200)
                .header("X-Request-ID", not(blankString()));
    }

    @Test
    void useProviderRequestId() {
        // given
        var providedRequestId = "test-request-id";

        // when & then
        given()
                .header("X-Request-ID", providedRequestId)

                .when()
                .get("/api/v1/bin/123456")

                .then()
                .statusCode(200)
                .header("X-Request-ID", equalTo(providedRequestId));
    }
}