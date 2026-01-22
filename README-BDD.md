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

# See also
- [DEVELOPER_GUIDE.md](./DEVELOPER_GUIDE.md) for developer setup, tracing, and technology details
- [FUNCTIONAL_OVERVIEW.md](./FUNCTIONAL_OVERVIEW.md) for functional and security documentation
