# Feature: Hello endpoint smoke test
# This feature verifies the simple hello endpoint using a smoke BDD scenario.
# It's intentionally small and tagged with @smoke so it runs in the smokeTest task.

Feature: Hello endpoint
  As a consumer of the Activity Service
  I want to call the hello endpoint
  So that I receive a friendly greeting

  @smoke
  Scenario: Hello returns greeting
    Given the hello endpoint is available
    When I call GET "/hello"
    Then the response status should be 200
    And the response body should contain "Hello"

