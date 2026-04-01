Feature: Payments feature
  Scenario Outline: Existing example tag
    Given a user exists
    When they pay an invoice
    Then the payment succeeds

    @example-tag @JIRA-KEY:TEST-0002
    Examples:
      | method | path                                   |
      | GET    | /defendant-accounts/500000009          |

