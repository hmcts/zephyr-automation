package uk.hmcts.zephyr.automation.actions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;

import java.util.Optional;


@Getter
@Slf4j
public abstract class AbstractUpdateTicketAction<T extends ZephyrTest>
    extends AbstractTicketAction<T>
    implements UpdateTicketAction {

    protected AbstractUpdateTicketAction(TagService<T> tagService) {
        super(tagService);
    }


    protected void updateJiraIssue(T test) {
        Optional<String> jiraKeyOpt = getTagService().extractJiraKeyFromTag(test);
        if (jiraKeyOpt.isEmpty()) {
            log.warn("No Jira key found for test {}", test);
            return;
        }
        String jiraKey = jiraKeyOpt.get();
        log.info("Updating Jira key {}", jiraKey);

        JiraIssueFieldsWrapper body = buildBody(test, false);

        //Update the issue
        Config.getJira().updateIssue(body, jiraKey);
        //Add any missing links to the issue.
        //This does not remove any existing links, so if links are removed from the test, they will need to be
        //removed manually from the Jira issue.
        addLinksToJiraIssue(jiraKey, test);
    }
}
