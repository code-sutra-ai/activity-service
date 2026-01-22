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
