# Running smoke BDD tests and common tasks

This project includes Cucumber BDD smoke tests located under `src/test/resources/automation/features` and step definitions under `src/test/java/io/code/sutra/activity/automation/stepdefs`.

## Commands

- Run all unit and test suites (uses JUnit Platform):
  ```zsh
  ./gradlew test
  ```
- Run only the BDD smoke tests (scenarios tagged `@smoke`):
  ```zsh
  ./gradlew smokeTest
  ```
- Run the Spring Boot application:
  ```zsh
  ./gradlew bootRun
  ```
- Build the project (jar):
  ```zsh
  ./gradlew clean build
  ```

## Docker

Build the Docker image (uses multi-stage build):
  ```zsh
  docker build -t activity-service:latest .
  ```
If you built the JAR locally (or the Gradle wrapper is missing), pass the jar file as a build arg:
  ```zsh
  docker build --build-arg JAR_FILE=build/libs/activity-service-0.0.1-SNAPSHOT.jar -t activity-service:local .
  ```
Run the image:
  ```zsh
  docker run -p 8080:8080 activity-service:latest
  ```

---

# Features -> Business mapping

A detailed mapping of BDD feature files to business requirements is available in `FEATURES_BUSINESS_REQUIREMENTS.md`. This file lists each feature, the business requirement it verifies, example DataTables, and where to find generated reports.

See: FEATURES_BUSINESS_REQUIREMENTS.md

---

# Sample data JSON

The application ships with a `DataInitializer` that pre-populates a few users and tasks when the application starts in a local/test profile. Below are example JSON payloads you can use to create data via the HTTP API (useful for manual testing or to seed environments).

- Create a User (POST /api/users)

```json
{
  "name": "sam"
}
```

Example curl:

```bash
curl -X POST http://localhost:8080/api/users \
 -H 'Content-Type: application/json' \
 -d '{"name":"mukesh"}'
```

- Create a Task (POST /api/tasks)

```json
{
  "id": 11,
  "title": "Fix bike",
  "status": "pending",
  "assignee": "mukesh",
  "service": "bike-service"
}
```

Example curl:

```bash
curl -X POST http://localhost:8080/api/tasks \
 -H 'Content-Type: application/json' \
 -d '{"id":11,"title":"Fix bike","status":"pending","assignee":"mukesh","service":"bike-service"}'
```

Notes:
- If you rely on the included `DataInitializer`, you will already find sample users (e.g. `mukesh`, `elon`, `jack`, `diana`) and tasks created at startup. The JSON examples above are for calling the HTTP endpoints directly when the app is running.
- `Task` instances in this project accept explicit `id` values (the starter data uses fixed ids). Avoid re-using an id already persisted unless you intend to update the existing row.

---

# See also
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) for developer setup, tracing, and technology details
- [FUNCTIONAL_OVERVIEW.md](./FUNCTIONAL_OVERVIEW.md) for functional and security documentation
