# Automation BDD

This folder contains Cucumber feature files and step definitions used for smoke BDD tests.

Files:
- src/test/resources/automation/hello.feature - Simple smoke scenario for /hello endpoint.
- src/test/resources/automation/tasks.feature - Tasks API BDD using DataTable and examples.
- src/test/java/io/code/sutra/activity/automation/CucumberSpringConfig.java - Enables Spring context for Cucumber tests.
- src/test/java/io/code/sutra/activity/automation/StepsHttpClient.java - Step definitions using Spring's TestRestTemplate.

How to run (locally):
1. Make sure the `smokeTest` Gradle task is configured (it is in root `build.gradle`).
2. Run the smoke tests and aggregate Serenity reports:

```bash
./gradlew clean smokeTest copySmokeResultsForSerenity aggregate integrateSmokeHtmlWithSerenity
```

3. Open the Serenity report:

```bash
open target/site/serenity/index.html
```

Notes:
- Feature files are tagged `@smoke` so the `smokeTest` task picks them up.
- `StepsHttpClient` uses `TestRestTemplate` which runs against the random port Spring test server provided by `CucumberSpringConfig`.
- The DataTable steps map table rows to JSON objects and POST to `/api/tasks`.

