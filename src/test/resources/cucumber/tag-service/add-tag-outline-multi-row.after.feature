Feature: Payments feature
  Scenario Outline: Tagged scenario outline with multiple rows
    Given a user exists
    When they pay an invoice
    Then the payment succeeds

    @JIRA-TEST-KEY:TEST-1001
    Examples:
      | method | path                                   |
      | GET    | /defendant-accounts/500000009          |
    @JIRA-TEST-KEY:TEST-1002
    Examples:
      | method | path                                   |
      | GET    | /courts?q=magistrates&business_unit=43 |

