package de.xam.featdoc.system;

import de.xam.featdoc.I18n;
import de.xam.featdoc.Term;
import de.xam.featdoc.markdown.MarkdownTool;
import de.xam.featdoc.wiki.IWikiLink;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Scenario implements IWikiLink {
    private final String label;
    private final List<ScenarioStep> scenarioSteps = new ArrayList<>();
    private final Universe universe;
    private Map<Condition, Condition.Variant> variants = new HashMap<>();

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

    /**
     * @param source sending system
     * @param target receiving system
     * @param message must either be defined within 'source' as outgoing OR in 'target' as incoming
     * @param comment optional comment on this one particular trigger message (event)
     * @return Scenario for further extension
     */
    public Scenario step(System source, System target, Message message, String comment) {
        if (message.system().equals(source)) {
            if (message.direction() != Message.Direction.OUTGOING)
                throw new IllegalStateException("Message defined in source System " + source + " must be OUTGOING, is " + message);
        } else if (message.system().equals(target)) {
            if (message.direction() != Message.Direction.INCOMING)
                throw new IllegalStateException("Message defined in target System " + source + " must be INCOMING, is " + message);
        } else {
            throw new IllegalStateException("Message not defined in either source (" + source.label + ") or target (" + target.label + ") system: " + message);
        }

        ScenarioStep scenarioStep = new ScenarioStep(this, source, target, new Rule.Trigger(message, comment));
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


    public Scenario variant(Condition.Variant variant) {
        Condition.Variant prev = variants.put(variant.condition(), variant);
        if (prev != null)
            throw new IllegalStateException("condition '" + variant.condition().label() + "' already set to '" + prev.label() + "'");
        return this;
    }

    @Override
    public @Nullable String wikiFolder(I18n i18n) {
        return i18n.resolve(Term.scenario);
    }

}
