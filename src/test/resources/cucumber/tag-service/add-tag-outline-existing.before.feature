Feature: Payments feature
  Scenario Outline: Existing example tag
    Given a user exists
    When they pay an invoice
    Then the payment succeeds

    @example-tag
    Examples:
      | method | path                                   |
      | GET    | /defendant-accounts/500000009          |

