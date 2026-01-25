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

Important rule

- When `Access-Control-Allow-Credentials` is `true`, the server must return an explicit origin in `Access-Control-Allow-Origin` (for example `http://localhost:5173`). Returning `*` is invalid in that case and browsers will reject the response.

Quick verification with curl

1) Preflight (OPTIONS):

```bash
curl -i -X OPTIONS 'http://localhost:8080/api/tasks' \
  -H 'Origin: http://localhost:5173' \
  -H 'Access-Control-Request-Method: GET' -v
```

Expected relevant response headers (examples):

```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
Access-Control-Allow-Headers: Content-Type,Authorization
Access-Control-Max-Age: 3600
```

2) Real request (GET):

```bash
curl -i 'http://localhost:8080/api/tasks' -H 'Origin: http://localhost:5173' -v
```

Expected (relevant) headers on the GET response:

```
HTTP/1.1 200 OK
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Credentials: true
Vary: Origin
```

Browser (React) notes

- If you use cookies or credentials, send requests with credentials enabled from React:

  - fetch: `fetch(url, { credentials: 'include', ... })`
  - axios: `axios.get(url, { withCredentials: true })`

- The browser will block requests if the server returns `Access-Control-Allow-Origin: *` while `Access-Control-Allow-Credentials` is `true`.
- The origin string returned by the server must exactly match the request Origin (including scheme and port).

Quick troubleshooting checklist

- Inspect the Network tab in browser DevTools and look at the `OPTIONS` preflight request:
  - Is the preflight returning 200? Are `Access-Control-Allow-Origin` and `Access-Control-Allow-Credentials` present?
- Confirm the request path is `/api/tasks` (our policy requires the explicit origin for this endpoint).
- Ensure there are no other CORS filters or proxies that override or remove headers.
- If you're using Spring Security, make sure OPTIONS is permitted (or that your CORS filter runs before security filters).

If you still see issues

- Paste the exact request headers (from browser DevTools) and the response headers here or in a PR comment and I will analyze them.

### Explanation: MockMvc tests vs Browser behavior and configuration guidance

- MockMvc (used by many integration tests in this project) simulates HTTP requests inside the Spring test context and applies Spring MVC's CORS configuration. In tests you may see headers produced by Spring's `CorsRegistry` or test-specific CORS filters even when the runtime behavior in a deployed app is different.

- Browsers enforce CORS for JavaScript requests. When your React app calls the API the browser will:
  - Perform an `OPTIONS` preflight for non-simple requests and expect the server to respond with `Access-Control-*` headers.
  - Reject responses that return `Access-Control-Allow-Origin: *` together with `Access-Control-Allow-Credentials: true`.

- Why the tests sometimes set `allowedOriginPatterns("*")` or echo the request origin:
  - In the test environment it's common to permit `allowedOriginPatterns("*")` so MockMvc will echo the `Origin` header back (this simplifies tests that assert the header is present). This is a convenience for tests, not a recommendation for production.

- Production recommendation:
  - Don't rely on `allowedOriginPatterns("*")` when your endpoints require credentials. Instead explicitly list allowed origins (for example, `http://localhost:5173` for local development) either in `application.yml` or environment variables and load them into your CORS config.
  - Centralize CORS handling in one place (prefer Spring MVC `WebMvcConfigurer.addCorsMappings(...)` or a single `CorsFilter`) and avoid duplicating CORS logic across multiple filters — duplicates make header precedence confusing and can break the exact-origin requirement for credentials.
  - If you need to allow multiple environments (local, staging, prod), read allowed origins from a comma-separated config value such as `app.cors.allowed-origins` and validate entries on startup.

- Quick production checklist:
  - Confirm `app.cors.allowed-origins` or equivalent contains only explicit origins when `allowCredentials` is true.
  - Ensure CORS config is registered for the intended path(s) (for example `/api/**`).
  - If using Spring Security, ensure CORS configuration is applied before security filters (or use `CorsConfigurationSource` integrated with security).

## When to Ask for Help
- If you see persistent `ApplicationContext` errors after following these steps, check for recent changes to configuration or dependencies.
- If unsure, share the error message and your recent changes with your team or in a pull request for review.

---
