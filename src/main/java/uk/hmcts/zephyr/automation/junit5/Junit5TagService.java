package uk.hmcts.zephyr.automation.junit5;

import lombok.extern.slf4j.Slf4j;
import uk.hmcts.zephyr.automation.TagService;
import uk.hmcts.zephyr.automation.TestTag;
import uk.hmcts.zephyr.automation.junit5.JavaTagger.AnnotationDescriptor;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraComponent;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraDefect;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraEpic;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraIgnore;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraTestKey;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraLabel;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraLink;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraNfr;
import uk.hmcts.zephyr.automation.junit5.annotations.JiraStory;
import uk.hmcts.zephyr.automation.junit5.model.Junit5ZephyrReport;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class Junit5TagService implements TagService<Junit5ZephyrReport.Test> {

    private static final Map<TestTag.Type, AnnotationDescriptor> ANNOTATIONS = Map.of(
        TestTag.Type.JIRA_KEY, JavaTagger.descriptor(JiraTestKey.class, true),
        TestTag.Type.JIRA_COMPONENT, JavaTagger.descriptor(JiraComponent.class, true),
        TestTag.Type.JIRA_LABEL, JavaTagger.descriptor(JiraLabel.class, true),
        TestTag.Type.JIRA_EPIC, JavaTagger.descriptor(JiraEpic.class, true),
        TestTag.Type.JIRA_NFR, JavaTagger.descriptor(JiraNfr.class, true),
        TestTag.Type.JIRA_LINK, JavaTagger.descriptor(JiraLink.class, true),
        TestTag.Type.JIRA_STORY, JavaTagger.descriptor(JiraStory.class, true),
        TestTag.Type.JIRA_DEFECT, JavaTagger.descriptor(JiraDefect.class, true),
        TestTag.Type.JIRA_IGNORE, JavaTagger.descriptor(JiraIgnore.class, false)
    );

    private final JavaTagger javaTagger;

    public Junit5TagService() {
        this(new JavaTagger());
    }

    Junit5TagService(JavaTagger javaTagger) {
        this.javaTagger = javaTagger;
    }

    @Override
    public List<TestTag> extractTagListFromType(Junit5ZephyrReport.Test test, TestTag.Type tagType) {
        JiraAnnotationMetadata metadata = test.getMetadata();
        if (TestTag.Type.JIRA_IGNORE.equals(tagType)) {
            return List.of(new TestTag(TestTag.Type.JIRA_IGNORE, String.valueOf(metadata.isJiraIgnore())));
        }

        Set<String> values = getTagValues(test, tagType);
        return values.stream()
            .map(value -> new TestTag(tagType, value))
            .toList();
    }

    private Set<String> getTagValues(Junit5ZephyrReport.Test test, TestTag.Type tagType) {
        JiraAnnotationMetadata metadata = test.getMetadata();
        return switch (tagType) {
            case JIRA_KEY -> metadata.getJiraKey();
            case JIRA_COMPONENT -> metadata.getJiraComponents();
            case JIRA_LABEL -> metadata.getJiraLabels();
            case JIRA_EPIC -> metadata.getJiraEpics();
            case JIRA_NFR -> metadata.getJiraNfrs();
            case JIRA_LINK -> metadata.getJiraLinks();
            case JIRA_STORY -> metadata.getJiraStories();
            case JIRA_DEFECT -> metadata.getJiraDefects();
            default -> throw new UnsupportedOperationException("Unknown tag type: " + tagType);
        };
    }

    @Override
    public boolean hasTag(Junit5ZephyrReport.Test test, TestTag.Type tagType) {
        if (TestTag.Type.JIRA_IGNORE.equals(tagType)) {
            JiraAnnotationMetadata metadata = test.getMetadata();
            return metadata.isJiraIgnore();
        } else {
            return TagService.super.hasTag(test, tagType);
        }
    }

    @Override
    public void addTag(Junit5ZephyrReport.Test test, TestTag testTag) {
        AnnotationDescriptor descriptor = Optional.ofNullable(ANNOTATIONS.get(testTag.type()))
            .orElseThrow(() -> new UnsupportedOperationException("Unsupported tag type: " + testTag.type()));
        String className = Objects.requireNonNull(test.getClassName(), "className");
        log.info("Adding {} annotation to {}#{}", descriptor.simpleName(), className, test.getMethodName());
        javaTagger.addAnnotation(className, test.getMethodName(), descriptor, testTag);

        if (TestTag.Type.JIRA_IGNORE.equals(testTag.type())) {
            test.getMetadata().setJiraIgnore(true);
            return;
        }
        Set<String> values = getTagValues(test, testTag.type());
        if (TestTag.Type.JIRA_KEY.equals(testTag.type())) {
            values.clear();
        }
        values.add(testTag.value());

    }
}
