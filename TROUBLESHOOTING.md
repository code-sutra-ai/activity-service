# Troubleshooting: Unit Test Failures in Activity Service

This guide helps you quickly diagnose and resolve common issues with unit and integration tests in this project.

## Common Issues & Fixes

### 1. YAML/Properties Configuration Errors
- **Symptom:** `Failed to load ApplicationContext` or YAML parse errors (e.g., `duplicate key`)
- **Fix:**
  - Check for duplicate keys in `application.yml`, `application-local.yml`, or `application-test.yml`.
  - Use a YAML linter or run:
    ```zsh
    yq e . src/main/resources/application.yml
    yq e . src/main/resources/application-local.yml
    yq e . src/test/resources/application-test.yml
    ```
  - Ensure all configuration blocks (e.g., `management:`) are merged under a single key.

### 2. Missing or Invalid Environment Variables
- **Symptom:** Tests fail with missing Cognito JWT or JWK URL errors.
- **Fix:**
  - For endpoint tests, set a valid Cognito JWT or use the `Dummy` token in local profile:
    ```zsh
    export SPRING_PROFILES_ACTIVE=local
    export COGNITO_TEST_JWT=Dummy
    ./gradlew test
    ```
  - For production-like tests, set real Cognito values:
    ```zsh
    export COGNITO_JWK_URL="<your-jwk-url>"
    export COGNITO_ISSUER="<your-issuer>"
    export COGNITO_TEST_JWT="<your-jwt-token>"
    ./gradlew test
    ```

### 3. Dependency or Build Issues
- **Symptom:** `@MockBean` or `@SpyBean` not found, or test context fails to load.
- **Fix:**
  - Ensure the following dependencies are present in `build.gradle`:
    ```groovy
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.mockito:mockito-core:5.2.0'
    testImplementation 'org.mockito:mockito-junit-jupiter:5.2.0'
    ```
  - Refresh Gradle dependencies:
    ```zsh
    ./gradlew --refresh-dependencies
    ```

### 4. Test Profile/Configuration Not Loaded
- **Symptom:** Tests use wrong config or fail to find test resources.
- **Fix:**
  - Make sure `application-test.yml` exists in `src/test/resources`.
  - Use the correct profile for your test:
    ```zsh
    ./gradlew test -Dspring.profiles.active=test
    ```

## General Troubleshooting Commands

- Run all tests and see detailed output:
  ```zsh
  ./gradlew clean test --info
  ```
- Run a specific test class:
  ```zsh
  ./gradlew test --tests io.code.sutra.activity.controller.HelloWorldControllerBDDTest
  ```
- Check for configuration errors:
  ```zsh
  grep -i error build/reports/tests/test/*.xml
  cat build/reports/problems/problems-report.html
  ```
- Validate YAML syntax:
  ```zsh
  yq e . src/main/resources/application.yml
  yq e . src/main/resources/application-local.yml
  yq e . src/test/resources/application-test.yml
  ```

## When to Ask for Help
- If you see persistent `ApplicationContext` errors after following these steps, check for recent changes to configuration or dependencies.
- If unsure, share the error message and your recent changes with your team or in a pull request for review.

---
