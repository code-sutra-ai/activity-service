Feature: Smoke tests using DataTable

  @smoke
  Scenario: Validate list of users via DataTable
    Given the following users:
      | name  | email             | active |
      | Alice | alice@example.com | true   |
      | Bob   | bob@example.com   | false  |
    When I validate the users
    Then all users processed
