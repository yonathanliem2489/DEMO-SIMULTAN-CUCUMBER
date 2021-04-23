Feature: try to test mongo
  Scenario: client makes call test integration mongo
    When the client calls to test mongo
    Then the client mongo receives status code of 200
    And the client mongo receives server