package dev.b6k.fds.riskassessment;

import dev.b6k.fds.MastercardBinApiStubHelper;
import dev.b6k.fds.MastercardBinApiTestProfile;
import dev.b6k.fds.WireMockExtension;
import dev.b6k.fds.model.RiskAssessmentRequest;
import dev.b6k.fds.model.RiskAssessmentRequestLocation;
import dev.b6k.fds.model.RiskAssessmentResponse;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
@TestProfile(MastercardBinApiTestProfile.class)
public class RiskAssessmentHttpEndpointTest {

    @Test
    void assessSingleTransactionRisk() {
        // given
        var binNumber = "123456";
        MastercardBinApiStubHelper.prepareSuccessResponse(binNumber);

        var request = RiskAssessmentRequest.builder()
                .bin(new BigDecimal(binNumber))
                .amount(new BigDecimal("2000.00"))
                .currency("PLN")
                .location(RiskAssessmentRequestLocation.builder()
                        .countryCode("POL")
                        .build())
                .build();

        // when
        var response = callRiskAssessmentService(request, 200, RiskAssessmentResponse.class);

        // then
        assertThat(response).isNotNull();
    }

    private <T> T callRiskAssessmentService(RiskAssessmentRequest request, int expectedStatusCode, Class<T> responseClass) {
        return given()
                .body(request)
                .contentType("application/json")

                .when()
                .post("/api/v1/transaction/risk-assessment")

                .then()
                .statusCode(expectedStatusCode)
                .extract()
                .as(responseClass);
    }
}
