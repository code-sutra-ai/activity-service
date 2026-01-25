# Feature: Tasks controller BDD using DataTable
# This feature demonstrates adding multiple tasks via a DataTable payload and assigning tasks.

Feature: Tasks API
  In order to manage tasks
  As a client application
  I want to create tasks and assign them

  Background:
    Given the API is available at "/api"

  @smoke
  Scenario Outline: Create multiple tasks via DataTable
    Given the following tasks exist:
      | title           | description          | assignee |
      | <title1>        | <desc1>              | <assignee1> |
      | <title2>        | <desc2>              | <assignee2> |
    When I POST "/api/tasks/batch" with the above table
    Then the response status should be 201
    And the response should contain a JSON array with size 2

    Examples:
      | title1    | desc1         | assignee1 | title2    | desc2         | assignee2 |
      | Task One  | First task    | alice     | Task Two  | Second task   | bob       |

  Scenario: Assign a task via PATCH
    Given a task exists with title "Task One" and assignee "alice"
    When I PATCH "/api/tasks/1/assign" with JSON:
      | { "assignee": "carol" } |
    Then the response status should be 200
    And the response JSON should have "assignee" equal to "carol"

