Feature: Payments feature
  Scenario Outline: Tagged scenario outline with multiple rows
    Given a user exists
    When they pay an invoice
    Then the payment succeeds

    Examples:
      | method | path                                   |
      | GET    | /defendant-accounts/500000009          |
      | GET    | /courts?q=magistrates&business_unit=43 |

