Feature: Payments feature
  Scenario Outline: Tagged scenario outline
    Given a user exists
    When they pay an invoice
    Then the payment succeeds

    @JIRA-KEY:TEST-0001
    Examples:
      | method | path                                   |
      | GET    | /defendant-accounts/500000009          |
    Examples:
      | method | path                                   |
      | GET    | /courts?q=magistrates&business_unit=43 |

