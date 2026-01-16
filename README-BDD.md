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

