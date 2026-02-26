package uk.hmcts.zephyr.automation.support;

import uk.hmcts.zephyr.automation.cucumber.models.CucumberFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CucumberDataUtil {

    public static CucumberFeature featureWithElements(CucumberFeature.Element... elements) {
        return featureWithElements("Feature-name", elements);
    }

    public static CucumberFeature featureWithElements(String name, CucumberFeature.Element... elements) {
        CucumberFeature feature = new CucumberFeature();
        feature.setName(name);
        List<CucumberFeature.Element> list = new ArrayList<>();
        Collections.addAll(list, elements);
        feature.setElements(list);
        return feature;
    }

    public static CucumberFeature.Element scenario(String name) {
        return element("scenario", name);
    }

    public static CucumberFeature.Element background() {
        return element("Background", "background");
    }

    public static CucumberFeature.Element element(String type) {
        return element(type, type + "-name");

    }

    public static  CucumberFeature.Tag tag(String name) {
        return new CucumberFeature.Tag(name, "Tag", new CucumberFeature.Location(1, 1));
    }

    public static  CucumberFeature.Element.Step stepAtLine(int line, String status) {
        CucumberFeature.Element.Step step = new CucumberFeature.Element.Step();
        step.setLine(line);
        CucumberFeature.Result result = new CucumberFeature.Result();
        result.setStatus(status);
        step.setResult(result);
        return step;
    }

    public static CucumberFeature.Element element(String type, String name) {
        CucumberFeature.Element element = new CucumberFeature.Element();
        element.setType(type);
        element.setName(name);
        return element;
    }

    public static CucumberFeature.Element.Step step(String status) {
        CucumberFeature.Element.Step step = new CucumberFeature.Element.Step();
        step.setResult(result(status));
        return step;
    }

    public static CucumberFeature.Result result(String status) {
        CucumberFeature.Result result = new CucumberFeature.Result();
        result.setStatus(status);
        return result;
    }
}
