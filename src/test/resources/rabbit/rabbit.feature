Feature: try to test rabbit
  Scenario: client makes call test integration rabbit
    When the client calls to test rabbit
    Then the client rabbit receives status code of 200
    And the client rabbit receives server