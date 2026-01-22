# Functional Overview: Activity Service

## Purpose
The Activity Service provides RESTful APIs for managing and tracking user activities. It is designed for resiliency, observability, and secure operation in cloud-native environments (AWS Fargate).

## Key Features
- **JWT Authentication**: All endpoints (except `/actuator/*`) require a valid Amazon Cognito JWT.
- **Resilience4j**: Circuit breaker patterns for robust service operation.
- **Observability**: JSON and color logs with traceId, spanId, correlationId; distributed tracing; Prometheus metrics.
- **Prometheus Metrics**: Exposed at `/actuator/prometheus` for monitoring.
- **Health Endpoints**: `/actuator/health`, `/actuator/info` are public for readiness/liveness checks.

## Endpoints
- `/hello`: Example endpoint (secured)
- `/actuator/*`: Spring Boot actuator endpoints (health, info, prometheus, etc.)

## Security
- All endpoints except `/actuator/*` require a valid Cognito JWT in the `Authorization: Bearer <token>` header.
- In local profile, the token value `Dummy` is accepted for development/testing.

## Configuration
- All configuration is managed via YAML files (`application.yml`, `application-local.yml`).
- Environment variables can override sensitive values (e.g., Cognito JWK URL, issuer).

## Testing
- Unit, integration, and BDD tests are provided.
- For endpoint tests, set `COGNITO_TEST_JWT` (or use `Dummy` in local profile).
