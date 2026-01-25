# Feature: Users API BDD using DataTable

Feature: Users API
  As a client of the Activity Service
  I want to create, list and delete users

  Background:
    Given the API is available at "/api"

  @smoke
  Scenario: Create multiple users using DataTable
    Given the following users exist:
      | name   |
      | alice  |
      | bob    |
    When I POST "/api/users/batch" with the above table
    Then the response status should be 201
    And the response should contain a JSON array with size 2

  Scenario: Create a user and then delete it
    Given the following users exist:
      | name   |
      | charlie|
    When I POST "/api/users" with body:
      """
      { "name": "charlie" }
      """
    Then the response status should be 201
    When I DELETE "/api/users/1"
    Then the response status should be 200

