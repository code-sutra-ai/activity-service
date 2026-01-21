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

## JWT Token Generation (for testing/demo)

To generate a JWT token for API access, use the following endpoint:

```
POST /auth/token
Content-Type: application/json

{
  "username": "your-username"
}
```

The response will be:
```
{
  "token": "<JWT_TOKEN>"
}
```

Use this token in the `Authorization` header for all API requests:
```
Authorization: Bearer <JWT_TOKEN>
```

## Prometheus Metrics

Prometheus metrics are available at:

```
GET /actuator/prometheus
```
