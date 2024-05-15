Feature: Order Updates

    An order is updated in one way or another

    Scenario: Updated Purchase Order
    Given a new order with id O-1234 is received
    And an invoice with id I-12341 is generated for that order
    And the invoice is being reviewed
    When an update is received for order with id O-1234
    Then the updated order is persisted
    And a new invoice with id I-12342 is generated for that order
    And 