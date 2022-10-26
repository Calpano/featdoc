package de.xam.featdoc.system;

import de.xam.featdoc.I18n;
import de.xam.featdoc.Term;
import de.xam.featdoc.markdown.MarkdownTool;
import de.xam.featdoc.wiki.IWikiLink;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class Scenario implements IWikiLink {
    private final String label;
    private final List<ScenarioStep> scenarioSteps = new ArrayList<>();
    private final Universe universe;

    public Scenario(Universe universe, String label) {
        this.universe = universe;
        this.label = label;
    }


    @Override
    public String label() {
        return label;
    }

    @Override
    public String localTarget() {
        return MarkdownTool.filename(label());
    }

    private static final Logger log = getLogger(Scenario.class);

    /**
     * @param sourceSystem  initial source system
     * @param message to be sent
     * @param comment optional comment on this one particular trigger message (event)
     * @return Scenario for further extension
     */
    public Scenario step(System sourceSystem, Message message, String comment) {
        if(message.isOutgoing() && !message.system().equals(sourceSystem)) {
            log.warn("Calling an outgoing message ({}) from another system ({}) doesn't make sense.",message,sourceSystem);
        }
        ScenarioStep scenarioStep = new ScenarioStep(this, sourceSystem, message, comment);
        scenarioSteps.add(scenarioStep);
        return this;
    }

    public Scenario step(System source, Message message) {
        return step(source, message, null);
    }

    public List<ScenarioStep> steps() {
        return Collections.unmodifiableList(scenarioSteps);
    }

    @Override
    public @Nullable String wikiFolder(I18n i18n) {
        return i18n.resolve(Term.scenario);
    }

}
