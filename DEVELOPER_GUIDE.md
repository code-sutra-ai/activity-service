# Developer Guide: Activity Service

## Technologies & Modules Used

- **Spring Boot 4.x**: Core application framework
- **Spring Security**: Secures all endpoints with Amazon Cognito JWT authentication
- **Amazon Cognito**: User authentication and JWT validation
- **Resilience4j**: Circuit breaker and resiliency patterns
- **Micrometer Tracing**: Distributed tracing with OpenTelemetry/Brave, OTLP exporter
- **Prometheus**: Metrics collection and actuator integration
- **Logback**: JSON and colored logging with traceId, spanId, correlationId
- **JUnit 5, Mockito, Serenity BDD**: Unit, integration, and BDD testing
- **Docker**: Containerization for deployment

## Local Development Setup

- Use the `local` Spring profile for development:
  ```zsh
  SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
  ```
- In local mode, the JWT token value `Dummy` is accepted for authentication (no Cognito required).
- Configuration is managed via `application-local.yml`.
- Logs are output in both JSON and colorized formats, including traceId, spanId, and correlationId for observability.
- Tracing is enabled and exported to OpenTelemetry Collector (default: `http://localhost:4318/v1/traces`).
- Prometheus metrics are available at `/actuator/prometheus`.

## Tracing & Observability

- **Micrometer Tracing** is enabled for all requests.
- 100% sampling is configured for local and default profiles.
- Trace context (traceId, spanId, correlationId) is included in all logs.
- To view traces, run an OpenTelemetry Collector locally and use a compatible backend (e.g., Jaeger, Zipkin, Grafana Tempo).
- Prometheus metrics are exposed for monitoring.

## Project Structure

- `src/main/java`: Application source code
- `src/main/resources`: Configuration files (`application.yml`, `application-local.yml`, `logback-spring.xml`)
- `src/test/java`: Unit, integration, and BDD tests
- `Dockerfile`: Container build
- `build.gradle`: Build configuration

## Useful Commands

- Run all tests:
  ```zsh
  ./gradlew test
  ```
- Build Docker image:
  ```zsh
  docker build -t activity-service:latest .
  ```
- Run locally with Prometheus and tracing:
  ```zsh
  SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
  ```

## How to Test Locally

- **Run all unit, integration, and BDD tests using the local profile:**
  ```zsh
  SPRING_PROFILES_ACTIVE=local ./gradlew clean test --info
  ```
  This command runs all tests with the local configuration, using the `Dummy` JWT token for authentication. It provides detailed output for troubleshooting.

- **Run a specific test class:**
  ```zsh
  SPRING_PROFILES_ACTIVE=local ./gradlew test --tests io.code.sutra.activity.controller.HelloWorldControllerBDDTest
  ```
  Useful for isolating and debugging a single test or controller.

- **Set up environment variables for local testing (optional):**
  ```zsh
  export SPRING_PROFILES_ACTIVE=local
  export COGNITO_TEST_JWT=Dummy
  ./gradlew test
  ```
  Ensures the local profile and dummy token are used for all test runs.

- **View test reports:**
  After running tests, open the HTML report for details:
  ```zsh
  open build/reports/tests/test/index.html
  ```

## How to Test Locally with Postman

1. **Start the service in local profile:**
   ```zsh
   SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
   ```
   This will use `application-local.yml` and accept the JWT token value `Dummy` for authentication.

2. **Configure Postman for local testing:**
   - Set the request URL to your local server, e.g.:
     ```
     http://localhost:8080/hello
     ```
   - Add the following header to your request:
     ```
     Authorization: Bearer Dummy
     ```
   - You can use any HTTP method supported by your endpoint (GET, POST, etc.).

3. **Expected behavior:**
   - All secured endpoints will accept the `Dummy` token and respond as if authenticated.
   - Actuator endpoints (e.g., `/actuator/health`, `/actuator/prometheus`) do not require authentication.

4. **Troubleshooting:**
   - If you get a 401 Unauthorized, ensure you are using the `local` profile and the `Dummy` token.
   - Check logs for errors and verify `COGNITO_JWK_URL` is set to a dummy value in `application-local.yml`.
