# fraud-detection-service
A sample Quarkus microservice.

## Running the application

The application can be integrated with an external API for BIN lookup.
Since this integration might require an API key and a signing certificate, the application can also be run in a mock mode, where the BIN lookup is simulated.

The mode can be set by changing the `fds.integration.bin.provider` property in the `application.properties` file.
The following values are supported:
- `mock` - the application will use a mock implementation of the BIN lookup service
- `mastercard` - the application will use the real Mastercard API for BIN lookup
 
### Connecting to the Mastercard API
1. Obtain the api key and a signing certificate - follow the instructions: https://developer.mastercard.com/bin-lookup/documentation/quick-start-guide/ 
2. Copy the `.env.sample` file into `.env` file and provide correct values (or set the proper environment variables)
3. Set the `fds.integration.bin.provider` property to `mastercard`

### Running the application
1. Run `mvn quarkus:dev`
2. Swagger is accessible at http://localhost:8080/swagger-ui/