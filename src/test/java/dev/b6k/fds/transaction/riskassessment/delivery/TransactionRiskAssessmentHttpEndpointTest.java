package dev.b6k.fds.transaction.riskassessment.delivery;

import dev.b6k.fds.BaseHttpEndpointTest;
import dev.b6k.fds.MastercardBinApiStubHelper;
import dev.b6k.fds.MastercardBinApiTestProfile;
import dev.b6k.fds.WireMockExtension;
import dev.b6k.fds.integration.mastercard.bin.model.BinResourceCountry;
import dev.b6k.fds.model.*;
import dev.b6k.fds.transaction.TransactionRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
class TransactionRiskAssessmentHttpEndpointTest extends BaseHttpEndpointTest {
    @Inject
    TransactionRepository transactionRepository;

    @BeforeEach
    void setup() {
        WireMockExtension.getWireMockServer().resetAll();
        cleanDatabase();
    }

    // Clean the database before each test to avoid problems when running the tests in continuous testing mode
    // Note on why this has to be done in such way: https://stackoverflow.com/a/72743861
    // Moreover we can't just execute deleteAll() because it would not cascade to the related entities
    @Transactional
    void cleanDatabase() {
        transactionRepository.findAll()
                .stream()
                .forEach(transactionRepository::delete);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-1.00", "100.543", "100000000000.00"})
    void validateAmount(String invalidAmount) {
        // given
        var bin = "123456";
        var request = getRiskAssessmentRequestBuilder(bin)
                .amount(new BigDecimal(invalidAmount))
                .build();

        // when
        var response = callRiskAssessmentService(
                request,
                400,
                ErrorResponse.class
        );

        // then
        assertThat(response.getErrors())
                .hasSize(1)
                .first()
                .extracting(ErrorResponseErrorsInner::getCode)
                .isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void assessRiskForTransactionWithoutRisks() {
        // given
        var bin = "123456";

        MastercardBinApiStubHelper.prepareCustomServiceResponse(bin, response -> {
            var country = new BinResourceCountry();
            country.setAlpha3("POL");
            country.setName("Poland");

            response.setCountry(country);
        });

        // perform the first request now so that the second one won't be the first transaction
        callRiskAssessmentService(
                getRiskAssessmentRequestBuilder(bin).build(),
                200,
                TransactionRiskAssessmentResponse.class
        );

        // when
        var response = callRiskAssessmentService(
                getRiskAssessmentRequestBuilder(bin).build(),
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
        var bin = "123456";
        MastercardBinApiStubHelper.prepareCustomServiceResponse(bin, response -> {
            var country = new BinResourceCountry();
            country.setAlpha3("POL");
            country.setName("Poland");

            response.setCountry(country);
        });

        var request = getRiskAssessmentRequestBuilder(bin)
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
        var bin = "674523";
        MastercardBinApiStubHelper.prepareCustomServiceResponse(bin, response -> {
            var country = new BinResourceCountry();
            country.setAlpha3("POL");
            country.setName("Poland");

            response.setCountry(country);
        });

        var request = TransactionRiskAssessmentRequest.builder()
                .bin(bin)
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
        var bin = "557799";
        MastercardBinApiStubHelper.prepareCustomServiceResponse(bin, response -> {
            var country = new BinResourceCountry();
            country.setAlpha3("RUS");
            country.setName("Russia");

            response.setCountry(country);
        });

        // assess a few transactions first so that the usual amount will be possible to be calculated
        Stream.of("100.00", "200.00", "300.00", "400.00", "500.00")
                .map(amount -> getRiskAssessmentRequestBuilder(bin)
                        .amount(new BigDecimal(amount))
                        .build())
                .forEach(request -> callRiskAssessmentService(request, 200, TransactionRiskAssessmentResponse.class));

        var request = TransactionRiskAssessmentRequest.builder()
                .bin(bin)
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
    void assessRiskForMaximumTransactionAmount() {
        // given
        var bin = "9870934";

        MastercardBinApiStubHelper.prepareCustomServiceResponse(bin, response -> {
            var country = new BinResourceCountry();
            country.setAlpha3("POL");
            country.setName("Poland");

            response.setCountry(country);
        });

        var request = getRiskAssessmentRequestBuilder(bin)
                .amount(new BigDecimal("99999999999.99"))
                .build();

        // when
        var response = callRiskAssessmentService(
                request,
                200,
                TransactionRiskAssessmentResponse.class
        );


        // then
        assertThat(response.getRiskFactors())
                .hasSize(2)
                .extracting(TransactionRiskAssessmentResponseRiskFactorsInner::getFactorName)
                .containsExactlyInAnyOrder(
                        "HIGH_TRANSACTION_AMOUNT",
                        "FIRST_TRANSACTION"
                );
    }

    @Test
    void getErrorResponseWhenBinNotFound() {
        // given
        var bin = "9999742";
        MastercardBinApiStubHelper.prepareNoDataResponse(bin);

        // when
        var response = callRiskAssessmentService(
                getRiskAssessmentRequestBuilder(bin).build(),
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
        var bin = "9999743";
        MastercardBinApiStubHelper.prepareErrorResponse();

        // when
        var response = callRiskAssessmentService(
                getRiskAssessmentRequestBuilder(bin).build(),
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

    private TransactionRiskAssessmentRequest.TransactionRiskAssessmentRequestBuilder<?, ?> getRiskAssessmentRequestBuilder(String bin) {
        return TransactionRiskAssessmentRequest.builder()
                .bin(bin)
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