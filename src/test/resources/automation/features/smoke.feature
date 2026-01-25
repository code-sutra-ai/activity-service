Feature: Smoke tests using DataTable

  @smoke
  Scenario: Validate list of users via DataTable
    Given the following users:
      | name  | email             | active |
      | Alice | alice@jugaads.co.iz | true   |
      | Bob   | bob@jugaads.co.iz   | false  |
    When I validate the users
    Then all users processed
