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

- **Run all tests with detailed output:**
  ```zsh
  ./gradlew clean test --info
  ```
  *Significance:* Runs all unit and integration tests from a clean state, showing detailed logs for diagnosing failures, dependency issues, and configuration problems.

- **Run a specific test class:**
  ```zsh
  ./gradlew test --tests io.code.sutra.activity.controller.HelloWorldControllerBDDTest
  ```
  *Significance:* Isolates and runs only the specified test class, useful for debugging a single failing test or controller.

- **Check for configuration and test errors:**
  ```zsh
  grep -i error build/reports/tests/test/*.xml
  cat build/reports/problems/problems-report.html
  ```
  *Significance:* Quickly surfaces error messages from test reports and Gradle's problem report, helping pinpoint root causes like bean creation failures, missing dependencies, or misconfigured profiles.

- **Validate YAML syntax:**
  ```zsh
  yq e . src/main/resources/application.yml
  yq e . src/main/resources/application-local.yml
  yq e . src/test/resources/application-test.yml
  ```
  *Significance:* Ensures your configuration files are valid and free of syntax errors, which can prevent Spring Boot from loading the application context.

- **Refresh Gradle dependencies:**
  ```zsh
  ./gradlew --refresh-dependencies
  ```
  *Significance:* Forces Gradle to re-download all dependencies, resolving issues caused by corrupted or outdated local caches.

- **Run tests with a specific profile:**
  ```zsh
  ./gradlew test -Dspring.profiles.active=test
  ```
  *Significance:* Ensures the correct Spring profile and configuration are used during test execution, avoiding environment-specific failures.

## CORS Troubleshooting (React + Browser)

If your React app on `http://localhost:5173` cannot call the backend and you see `Network Error` or CORS errors in the browser, remember:

- CORS is enforced by browsers for JavaScript (fetch, axios) calls — it is not enforced when you directly open an URL in the browser address bar or use curl.
- The server must respond to preflight `OPTIONS` requests and include the proper `Access-Control-*` response headers.

What the backend must send for `/api/tasks` responses

- Access-Control-Allow-Origin: http://localhost:5173
- Access-Control-Allow-Credentials: true

These headers must be present both on the preflight (`OPTIONS`) response and the actual response (GET/POST/etc.).

Quick verification with curl

1) Preflight (OPTIONS):

```bash
curl -i -X OPTIONS 'http://localhost:8080/api/tasks' \
  -H 'Origin: http://localhost:5173' \
  -H 'Access-Control-Request-Method: GET' -v
```

Expect `Access-Control-Allow-Origin: http://localhost:5173` and `Access-Control-Allow-Credentials: true` in the response headers.

2) Real request (GET):

```bash
curl -i 'http://localhost:8080/api/tasks' -H 'Origin: http://localhost:5173' -v
```

Expect the same headers on the response.

Browser (React) notes

- If you use cookies or credentials, send requests with credentials enabled from React:

  - fetch: `fetch(url, { credentials: 'include', ... })`
  - axios: `axios.get(url, { withCredentials: true })`

- The server must NOT return `Access-Control-Allow-Origin: *` when `Access-Control-Allow-Credentials: true` is present — browsers will reject that combination. Instead return the explicit origin string (e.g., `http://localhost:5173`).

If you still see issues

- Check the browser DevTools Network tab. Inspect the `OPTIONS` preflight request — status must be 200 and the appropriate `Access-Control-*` headers must be present.
- Confirm the request path matches `/api/tasks` (our filter applies specifically to that path for the production header requirement).
- Ensure no other filters or CDNs are rewriting or removing CORS headers.

## When to Ask for Help
- If you see persistent `ApplicationContext` errors after following these steps, check for recent changes to configuration or dependencies.
- If unsure, share the error message and your recent changes with your team or in a pull request for review.

---
