# Running smoke BDD tests and common tasks

This project includes Cucumber BDD smoke tests located under `src/test/resources/automation/features` and step definitions under `src/test/java/io/code/sutra/activity/automation/stepdefs`.

Commands:

- Run all unit and test suites (uses JUnit Platform):

```bash
./gradlew test
```

- Run only the BDD smoke tests (scenarios tagged `@smoke`):

```bash
./gradlew smokeTest
```

- Run the Spring Boot application:

```bash
./gradlew bootRun
```

- Build the project (jar):

```bash
./gradlew clean build
```

Notes:
- The Gradle wrapper should be present (`gradle/wrapper/gradle-wrapper.jar`). If missing, either install Gradle locally and run `gradle <task>` or restore the wrapper.
- Tests run with the `test` Spring profile by default via the Gradle test tasks.

## Docker

Build the Docker image (uses multi-stage build):

```bash
# from project root
docker build -t activity-service:latest .
```

If you built the JAR locally (or the Gradle wrapper is missing), pass the jar file as a build arg:

```bash
docker build --build-arg JAR_FILE=build/libs/activity-service-0.0.1-SNAPSHOT.jar -t activity-service:local .
```

Run the image:

```bash
docker run -p 8080:8080 activity-service:latest
```

# Amazon Cognito Authentication

This service uses Amazon Cognito JWT token authentication for all endpoints (except `/actuator/*`).

## Configuration

Set the following environment variables for Cognito integration:

- `COGNITO_JWK_URL`: The Cognito JWKs endpoint, e.g. `https://cognito-idp.<region>.amazonaws.com/<userPoolId>/.well-known/jwks.json`
- `COGNITO_ISSUER`: The Cognito issuer, e.g. `https://cognito-idp.<region>.amazonaws.com/<userPoolId>`

## Testing with Cognito JWT

To run endpoint tests, you must provide a valid Cognito JWT token:

- Set the environment variable `COGNITO_TEST_JWT` to a valid Cognito access or ID token for your user pool.
- Example (zsh):

```zsh
export COGNITO_TEST_JWT="<your-cognito-jwt-token>"
./gradlew test
```

If `COGNITO_TEST_JWT` is not set, endpoint tests will fail with an error.

## Security
- All endpoints require a valid Cognito JWT in the `Authorization: Bearer <token>` header, except `/actuator/health`, `/actuator/info`, and `/actuator/prometheus`.
- The Cognito JWT is validated using the public JWKs from your Cognito user pool.

## API Documentation (Swagger/OpenAPI)

[Swagger/OpenAPI documentation has been removed from this service.]

---
