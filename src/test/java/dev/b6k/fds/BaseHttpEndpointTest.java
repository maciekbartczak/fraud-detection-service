package dev.b6k.fds;

import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.junit.jupiter.api.BeforeAll;

public abstract class BaseHttpEndpointTest {
    private static class JwtFilter implements Filter {
        @Override
        public Response filter(FilterableRequestSpecification requestSpec,
                               FilterableResponseSpecification responseSpec,
                               FilterContext ctx) {
            var token = JwtGenerator.generateToken();

            requestSpec.header("Authorization", "Bearer " + token);

            return ctx.next(requestSpec, responseSpec);
        }
    }

    @BeforeAll
    public static void setUp() {
        RestAssured.filters(new JwtFilter());
    }
}