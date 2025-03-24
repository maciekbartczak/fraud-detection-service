package dev.b6k.fds.transaction.riskassessment.delivery;

import dev.b6k.fds.BaseHttpEndpointTest;
import dev.b6k.fds.MastercardBinApiStubHelper;
import dev.b6k.fds.MastercardBinApiTestProfile;
import dev.b6k.fds.WireMockExtension;
import dev.b6k.fds.integration.mastercard.bin.model.BinResourceCountry;
import dev.b6k.fds.model.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(WireMockExtension.class)
@TestProfile(MastercardBinApiTestProfile.class)
// TODO: load custom weights, thresholds and scores for assertions
class TransactionTransactionRiskAssessmentHttpEndpointTest extends BaseHttpEndpointTest {
    @BeforeEach
    void setup() {
        WireMockExtension.getWireMockServer().resetAll();
    }

    @Test
    void assessRiskForTransactionWithoutRisks() {
        // given
        var binNumber = "123456";

        MastercardBinApiStubHelper.prepareCustomServiceResponse(binNumber, response -> {
            var country = new BinResourceCountry();
            country.setAlpha3("POL");
            country.setName("Poland");

            response.setCountry(country);
        });

        // perform the first request now so that the second one won't be the first transaction
        callRiskAssessmentService(
                getRiskAssessmentRequestBuilder(binNumber).build(),
                200,
                TransactionRiskAssessmentResponse.class
        );

        // when
        var response = callRiskAssessmentService(
                getRiskAssessmentRequestBuilder(binNumber).build(),
                200,
                TransactionRiskAssessmentResponse.class
        );

        // then
        assertEquals(TransactionRiskAssessmentResponse.RiskLevelEnum.LOW, response.getRiskLevel());
        assertEquals(0, response.getRiskScore());
        assertThat(response.getRiskFactors()).isEmpty();
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

        var request = getRiskAssessmentRequestBuilder(binNumber)
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

    @Test
    void assessRiskForHighRiskTransaction() {
        // given
        var binNumber = "557799";
        MastercardBinApiStubHelper.prepareCustomServiceResponse(binNumber, response -> {
            var country = new BinResourceCountry();
            country.setAlpha3("RUS");
            country.setName("Russia");

            response.setCountry(country);
        });

        // assess a few transactions first so that the usual amount will be possible to be calculated
        Stream.of("100.00", "200.00", "300.00", "400.00", "500.00")
                .map(amount -> getRiskAssessmentRequestBuilder(binNumber)
                        .amount(new BigDecimal(amount))
                        .build())
                .forEach(request -> callRiskAssessmentService(request, 200, TransactionRiskAssessmentResponse.class));

        var request = TransactionRiskAssessmentRequest.builder()
                .bin(new BigDecimal(binNumber))
                .amount(new BigDecimal("2500.00"))
                .currency("RBL")
                .location(TransactionRiskAssessmentRequestLocation.builder()
                        .countryCode("POL")
                        .build())
                .build();

        // when
        var response = callRiskAssessmentService(request, 200, TransactionRiskAssessmentResponse.class);

        // then
        assertEquals(TransactionRiskAssessmentResponse.RiskLevelEnum.HIGH, response.getRiskLevel());
        assertEquals(100, response.getRiskScore());
        assertThat(response.getRiskFactors())
                .hasSize(5)
                .extracting(TransactionRiskAssessmentResponseRiskFactorsInner::getFactorName)
                .containsExactlyInAnyOrder(
                        "HIGH_RISK_COUNTRY",
                        "HIGH_TRANSACTION_AMOUNT",
                        "FOREIGN_CARD",
                        "UNUSUAL_AMOUNT",
                        "ROUND_TRANSACTION_AMOUNT"
                );
    }

    @Test
    void getErrorResponseWhenBinNotFound() {
        // given
        var binNumber = "9999742";
        MastercardBinApiStubHelper.prepareNoDataResponse(binNumber);

        // when
        var response = callRiskAssessmentService(
                getRiskAssessmentRequestBuilder(binNumber).build(),
                404,
                ErrorResponse.class
        );

        // then
        assertThat(response.getErrors())
                .hasSize(1)
                .first()
                .satisfies(error -> {
                    assertEquals("NOT_FOUND", error.getCode());
                    assertEquals("No data found for the given BIN in the Mastercard API", error.getMessage());
                });
    }

    @Test
    void getErrorResponseWhenApiFails() {
        // given
        var binNumber = "9999743";
        MastercardBinApiStubHelper.prepareErrorResponse();

        // when
        var response = callRiskAssessmentService(
                getRiskAssessmentRequestBuilder(binNumber).build(),
                500,
                ErrorResponse.class
        );

        // then
        assertThat(response.getErrors())
                .hasSize(1)
                .first()
                .satisfies(error -> {
                    assertEquals("EXTERNAL_API_ERROR", error.getCode());
                    assertEquals("Failed to retrieve BIN details from Mastercard API", error.getMessage());
                });
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"invalid-jwt-token"})
    void returnUnauthorizedErrorWhenInvalidAuthorizationHeader(String jwt) {
        given()
                .contentType("application/json")
                .header("Authorization", jwt)

                .when()
                .post("/api/v1/transaction/risk-assessment")

                .then()
                .statusCode(401);
    }

    private TransactionRiskAssessmentRequest.TransactionRiskAssessmentRequestBuilder<?, ?> getRiskAssessmentRequestBuilder(String binNumber) {
        return TransactionRiskAssessmentRequest.builder()
                .bin(new BigDecimal(binNumber))
                .amount(new BigDecimal("13.74"))
                .currency("PLN")
                .location(TransactionRiskAssessmentRequestLocation.builder()
                        .countryCode("POL")
                        .build());
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