package uk.hmcts.zephyr.automation.zephyr.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class ZephyrExecutionSearchResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void given_legacySearchResponseJson_when_deserialising_then_mapsSearchObjectListAndExecution() throws Exception {
        String json = """
            {
              "searchObjectList": [
                {
                  "warningMessage": null,
                  "originMessage": null,
                  "execution": {
                    "id": "a1d2fbf8-a645-4b71-9cc9-f353e67305fd",
                    "issueId": 899098,
                    "versionId": -1,
                    "projectId": 10340,
                    "cycleId": "67430369-dbc1-4581-881b-34d731d6a59b",
                    "orderId": 1,
                    "createdBy": "712020:414e06d6-3719-484b-8d21-0bc93fa19663",
                    "createdByAccountId": "712020:414e06d6-3719-484b-8d21-0bc93fa19663",
                    "status": {
                      "name": "UNEXECUTED",
                      "id": -1,
                      "description": "The test has not yet been executed.",
                      "color": "#A0A0A0",
                      "type": 0
                    },
                    "cycleName": "Automated Cycle",
                    "defects": [],
                    "stepDefects": [],
                    "creationDate": 1790705026000,
                    "executedByZapi": true,
                    "zfjIndexType": "execution",
                    "issueTypeId": 10005,
                    "projectType": "classic"
                  },
                  "issueKey": "POT-5",
                  "issueLabel": "some_label",
                  "component": "",
                  "issueSummary": "create report instance single business unit",
                  "viewIssuePermission": true,
                  "executionWorkflowEnabled": true
                }
              ],
              "summaryList": null,
              "totalCount": 1,
              "currentOffset": 1,
              "maxAllowed": 50,
              "executionStatus": {
                "-1": {
                  "name": "UNEXECUTED",
                  "id": -1,
                  "description": "The test has not yet been executed.",
                  "color": "#A0A0A0",
                  "type": 0
                }
              },
              "stepExecutionStatus": null
            }
            """;

        ZephyrExecutionSearchResponse response = objectMapper.readValue(json, ZephyrExecutionSearchResponse.class);

        assertEquals(1, response.getSearchObjectList().size());
        assertEquals(1, response.getExecutions().size());
        assertEquals("POT-5", response.getExecutions().getFirst().getIssueKey());
        assertEquals("a1d2fbf8-a645-4b71-9cc9-f353e67305fd", response.getExecutions().getFirst().getId());
        assertEquals("67430369-dbc1-4581-881b-34d731d6a59b", response.getExecutions().getFirst().getCycleId());
        assertEquals(-1, response.getExecutionStatus().get("-1").getId());
        assertTrue(response.getSearchObjectList().getFirst().getViewIssuePermission());
    }
}
