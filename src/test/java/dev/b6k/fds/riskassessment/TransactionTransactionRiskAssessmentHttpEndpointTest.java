package dev.b6k.fds.riskassessment;

import dev.b6k.fds.MastercardBinApiStubHelper;
import dev.b6k.fds.MastercardBinApiTestProfile;
import dev.b6k.fds.WireMockExtension;
import dev.b6k.fds.model.TransactionRiskAssessmentRequest;
import dev.b6k.fds.model.TransactionRiskAssessmentRequestLocation;
import dev.b6k.fds.model.TransactionRiskAssessmentResponse;
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
public class TransactionTransactionRiskAssessmentHttpEndpointTest {

    @Test
    void assessSingleTransactionRisk() {
        // given
        var binNumber = "123456";
        MastercardBinApiStubHelper.prepareSuccessResponse(binNumber);

        var request = TransactionRiskAssessmentRequest.builder()
                .bin(new BigDecimal(binNumber))
                .amount(new BigDecimal("2000.00"))
                .currency("PLN")
                .location(TransactionRiskAssessmentRequestLocation.builder()
                        .countryCode("POL")
                        .build())
                .build();

        // when
        var response = callRiskAssessmentService(request, 200, TransactionRiskAssessmentResponse.class);

        // then
        assertThat(response).isNotNull();
    }

    private <T> T callRiskAssessmentService(TransactionRiskAssessmentRequest request, int expectedStatusCode, Class<T> responseClass) {
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
