# fraud-detection-service
A sample Quarkus microservice.

## Running the application

### Connecting to the real Mastercard API
1. Obtain the api key and a signing certificate - follow the instructions: https://developer.mastercard.com/bin-lookup/documentation/quick-start-guide/ 
2. Copy the `.env.sample` file into `.env` file and provide correct values (or set the proper environment variables)
3. Set the `fds.integration.mastercard.bin.enabled` property to `true`
4. Run `mvn quarkus:dev`
5. Swagger is accessible at http://localhost:8080/swagger-ui/