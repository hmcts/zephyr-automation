package uk.hmcts.zephyr.automation.actions;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.Config;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.jira.JiraConfig;
import uk.hmcts.zephyr.automation.jira.models.JiraIssue;
import uk.hmcts.zephyr.automation.jira.models.JiraIssueFieldsWrapper;

import java.util.Optional;


@Getter
@Slf4j
public abstract class AbstractCreateTicketAction<T extends ZephyrTest>
    extends AbstractTicketAction<T>
    implements CreateTicketAction {

    protected AbstractCreateTicketAction(TagService<T> tagService) {
        super(tagService);
    }

    public Optional<JiraIssue> createJiraIssue(T test) {
        try {
            Optional<String> name = getTagService().extractJiraKeyFromTag(test);
            if (name.isPresent()) {
                log.info("Test in {} has JIRA key: {}", test.getNameAndLocation(), name.get());
                return Optional.empty();
            }
            if (getTagService().hasTag(test, JiraConfig.JIRA_IGNORE)) {
                log.info("Test in {} is marked to ignore JIRA creation", test.getNameAndLocation());
                return Optional.empty();
            }
            log.info("Test in {} does not have JIRA key tag", test.getNameAndLocation());

            JiraIssueFieldsWrapper body = buildBody(test, true);

            //Create the issue
            JiraIssue jiraIssue = Config.getJira().createIssue(body);
            addLinksToJiraIssue(jiraIssue.getKey(), test);
            getTagService().addTag(test, JiraConfig.JIRA_KEY_TAG_PREFIX + jiraIssue.getKey());
            return Optional.of(jiraIssue);
        } catch (Exception e) {
            log.error("Error creating JIRA issue for test in {}", test.getNameAndLocation(), e);
            return Optional.empty();
        }
    }
}
