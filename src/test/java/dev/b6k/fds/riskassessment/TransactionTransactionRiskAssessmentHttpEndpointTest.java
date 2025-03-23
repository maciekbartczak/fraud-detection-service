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
import java.util.stream.Stream;

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