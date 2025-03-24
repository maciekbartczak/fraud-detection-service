# fraud-detection-service

A sample Quarkus microservice.

## Running the application

The application can be integrated with an external API for BIN lookup.
Since this integration might require an API key and a signing certificate, the application can also be run in a mock
mode, where the BIN lookup is simulated.

The mode can be set by changing the `fds.integration.bin.provider` property in the `application.properties` file.
The following values are supported:

- `mock` - the application will use a mock implementation of the BIN lookup service
- `mastercard` - the application will use the real Mastercard API for BIN lookup

### Connecting to the Mastercard API

1. Obtain the api key and a signing certificate - follow the
   instructions: https://developer.mastercard.com/bin-lookup/documentation/quick-start-guide/
2. Copy the `.env.sample` file into `.env` file and provide correct values (or set the proper environment variables)
3. Set the `fds.integration.bin.provider` property to `mastercard`

The following BINs can be used to test the Mastercard API:

- 99875393
- 29214303
- 47980829
- 66757806
- 855153163
- 1157111
- 966741047
- 225108
- 932860
- 777305770

### Running the application locally

#### Prerequisites
- Java 21
- Maven 3.9.0 or later

The application can be run in two ways:

- Run `mvn generate-sources` to generate the OpenAPI client and run the application using IntellJ
- Run `mvn quarkus:dev`

Swagger UI is available at `http://localhost:8080/swagger-ui`

## Trade-offs

In order to keep the application simple and reduce the scope of the task the following trade-offs were made:

1. The BIN details domain object represents only a subset of the data returned by the Mastercard API. Additionally, it
   is assumed that the Mastercard API always returns all the required data.
2. The risk evaluation is based only on few simple factors, however the code enables easy extension of the evaluation
   logic.
3. Only a subset of the data stored on the database is used for the risk evaluation.