FEATURES AND BUSINESS REQUIREMENTS

This document maps the repository's automated BDD feature files to business requirements. It's intended for product owners, QA and developers to understand what each scenario verifies in business language and where the BDD artifacts live.

Location of feature files
- src/test/resources/automation/hello.feature
- src/test/resources/automation/tasks.feature
- src/test/resources/automation/users.feature

How to run the smoke BDD suite
- The Gradle `smokeTest` task runs JUnit tests tagged with `@smoke` (the Cucumber-based smoke scenarios are tagged). To run and generate Serenity reports:

```bash
./gradlew clean smokeTest copySmokeResultsForSerenity aggregate integrateSmokeHtmlWithSerenity
open target/site/serenity/index.html
```

High-level mapping: feature -> business requirement

1) Hello endpoint - smoke / Sanity
- Feature file: `hello.feature`
- Business requirement: The public greeting endpoint must be reachable and respond with a friendly greeting for health-check and smoke validation.
- Purpose: Quick smoke verification that the application is up and responding.
- Acceptance criteria (automated): GET /hello returns HTTP 200 and body contains "Hello".

2) Tasks API - create, assign, batch operations
- Feature file: `tasks.feature`
- Business requirement(s):
  - Create tasks with required fields (title, description, service) so downstream services can process them.
  - Support batch creation of multiple tasks (DataTable) to accelerate onboarding/seed workflows.
  - Support assigning a task to a user (assignee) through a PATCH/assign endpoint.
- Purpose: Verify core task lifecycle operations work via the REST API.
- Acceptance criteria (automated):
  - Posting tasks (single or batch) returns success (201) and created records are returned.
  - Assigning a task returns the updated task with the requested assignee.

DataTable example (Tasks)

| title     | description   | assignee |
| Task One  | First task    | alice    |
| Task Two  | Second task   | bob      |

This table is used by `tasks.feature` to create multiple tasks in one scenario.

3) Users API - create, list, delete (DataTable-driven)
- Feature file: `users.feature`
- Business requirement(s):
  - Allow creation of users with unique names.
  - Allow listing and deletion of users.
  - Support batch or DataTable-driven creation of multiple users for onboarding.
- Purpose: Verify the user management endpoints used to assign tasks and manage principals.
- Acceptance criteria (automated):
  - POST /api/users returns 201 with created user id and name.
  - DELETE /api/users/{id} returns 200 for existing users.

DataTable example (Users)

| name  |
| alice |
| bob   |

This table is used by `users.feature` to create multiple users.

Sample JSON payloads (reuse from README-BDD)

- Create a User (POST /api/users)

```json
{ "name": "sam" }
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

Where reports are generated
- Gradle/JUnit html for the smoke task: `build/reports/smokeTest/index.html`
- Serenity aggregated site: `target/site/serenity/index.html`
- Individual JUnit XMLs used by Serenity are copied to `build/serenity-input/test-results` and `target/site/serenity/junit` during the build pipeline.

Notes and recommendations
- The BDD scenarios use DataTable where bulk creation is required — this improves readability and keeps scenarios declarative (business-focused).
- If you add UI/browser tests, re-enable appropriate Serenity transitive dependencies (we excluded heavy test-only transitive libs earlier).
- Update this mapping as features are added or business requirements evolve.

Contact
- If you need additional business scenarios documented or want the feature-to-requirement mapping placed in a different format (CSV/Confluence), I can generate that as well.

