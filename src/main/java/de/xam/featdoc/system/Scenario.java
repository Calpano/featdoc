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
     * @param source sending system
     * @param target receiving system
     * @param message must either be defined within 'source' as outgoing OR in 'target' as incoming
     * @param comment optional comment on this one particular trigger message (event)
     * @return Scenario for further extension
     */
    public Scenario step(System source, System target, Message message, String comment) {
        if (message.system().equals(source) && !message.equals(target)) {
            if (message.direction() != Message.Direction.OUTGOING)
                log.warn("Message defined in source System " + source + " must be OUTGOING, is " + message);
        } else if (message.system().equals(target) && !message.system().equals(source)) {
            if (message.direction() != Message.Direction.INCOMING)
                log.warn("Message defined in target System " + source + " must be INCOMING, is " + message);
        } else {
            log.warn("Message not defined in either source (" + source.label + ") or target (" + target.label + ") system: " + message);
        }

        ScenarioStep scenarioStep = new ScenarioStep(this, source, target, message, comment);
        scenarioSteps.add(scenarioStep);
        return this;
    }


    public Scenario step(System source, System target, Message message) {
        return step(source, target, message, null);
    }


    public List<ScenarioStep> steps() {
        return Collections.unmodifiableList(scenarioSteps);
    }

    /**
     * Direct scenario systems, not indirectly called systems.
     * Distinct and sorted.
     */
    public Stream<System> systems() {
        return steps().stream().flatMap(scenarioStep -> Stream.of(scenarioStep.source(), scenarioStep.target())).distinct().sorted();
    }



    @Override
    public @Nullable String wikiFolder(I18n i18n) {
        return i18n.resolve(Term.scenario);
    }

}
