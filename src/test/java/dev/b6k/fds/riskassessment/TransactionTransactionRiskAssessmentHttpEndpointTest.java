package dev.b6k.fds.riskassessment;

import dev.b6k.fds.MastercardBinApiStubHelper;
import dev.b6k.fds.MastercardBinApiTestProfile;
import dev.b6k.fds.WireMockExtension;
import dev.b6k.fds.integration.mastercard.bin.model.BinResourceCountry;
import dev.b6k.fds.model.TransactionRiskAssessmentRequest;
import dev.b6k.fds.model.TransactionRiskAssessmentRequestLocation;
import dev.b6k.fds.model.TransactionRiskAssessmentResponse;
import dev.b6k.fds.model.TransactionRiskAssessmentResponseRiskFactorsInner;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
@TestProfile(MastercardBinApiTestProfile.class)
// TODO: load custom weights, thresholds and scores for assertions
public class TransactionTransactionRiskAssessmentHttpEndpointTest {
    @BeforeEach
    void setup() {
        WireMockExtension.getWireMockServer().resetAll();
    }

    @Test
    void assessRiskForLowRiskTransaction() {
        // given
        var binNumber = "123456";
        MastercardBinApiStubHelper.prepareCustomServiceResponse(binNumber, response -> {
            var country = new BinResourceCountry();
            country.setAlpha3("POL");
            country.setName("Poland");

            response.setCountry(country);
        });

        var request = TransactionRiskAssessmentRequest.builder()
                .bin(new BigDecimal(binNumber))
                .amount(new BigDecimal("13.74"))
                .currency("PLN")
                .location(TransactionRiskAssessmentRequestLocation.builder()
                        .countryCode("POL")
                        .build())
                .build();

        // when
        var response = callRiskAssessmentService(request, 200, TransactionRiskAssessmentResponse.class);

        // then
        assertEquals(TransactionRiskAssessmentResponse.RiskLevelEnum.LOW, response.getRiskLevel());
        assertEquals(13, response.getRiskScore());
        assertThat(response.getRiskFactors())
                .hasSize(1)
                .first()
                .satisfies(riskFactor -> {
                    assertEquals("FIRST_TRANSACTION", riskFactor.getFactorName());
                    assertEquals("First transaction with this card", riskFactor.getFactorDescription());
                });
    }

    @Test
    void assessRiskForMediumRiskTransaction() {
        // given
        var binNumber = "674523";
        MastercardBinApiStubHelper.prepareCustomServiceResponse(binNumber, response -> {
            var country = new BinResourceCountry();
            country.setAlpha3("POL");
            country.setName("Poland");

            response.setCountry(country);
        });

        var request = TransactionRiskAssessmentRequest.builder()
                .bin(new BigDecimal(binNumber))
                .amount(new BigDecimal("100.00"))
                .currency("PLN")
                .location(TransactionRiskAssessmentRequestLocation.builder()
                        .countryCode("ARG")
                        .build())
                .build();

        // when
        var response = callRiskAssessmentService(request, 200, TransactionRiskAssessmentResponse.class);

        // then
        assertEquals(TransactionRiskAssessmentResponse.RiskLevelEnum.MEDIUM, response.getRiskLevel());
        assertEquals(40, response.getRiskScore());
        assertThat(response.getRiskFactors())
                .hasSize(3)
                .extracting(TransactionRiskAssessmentResponseRiskFactorsInner::getFactorName)
                .containsExactlyInAnyOrder("FOREIGN_CARD", "ROUND_TRANSACTION_AMOUNT", "FIRST_TRANSACTION");
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