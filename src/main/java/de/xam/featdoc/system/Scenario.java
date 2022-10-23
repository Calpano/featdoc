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

    /**
     * Alias for {@link #step(System, System, Message)} to ease refactoring/refinement of scenarios
     */
    public Scenario asyncEvent(System source, System target, Message message) {
        if (!message.isAsynchronous())
            throw new IllegalStateException("asyncEvent must use an asynchronous event, not " + message);
        return step(source, target, message);
    }

    public Scenario asyncEvent(System source, System target, String event) {
        return step(source, target, new Message(target, Timing.Asynchronous, event));
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String localTarget() {
        return MarkdownTool.filename(label());
    }

    public Scenario step(System source, System target, Message message) {
        ScenarioStep scenarioStep = new ScenarioStep(this, source, target, new Rule.Trigger(  message,null));
        scenarioSteps.add(scenarioStep);
        return this;
    }

    public List<ScenarioStep> steps() {
        return Collections.unmodifiableList(scenarioSteps);
    }

    public Scenario syncCall(System source, System target, String callMessage) {
        return step(source, target, new Message(target, Timing.Synchronous, callMessage));
    }

    /**
     * Alias for {@link #step(System, System, Message)} to ease refactoring/refinement of scenarios
     */
    public Scenario syncCall(System source, System target, Message message) {
        if (!message.isSynchronous())
            throw new IllegalStateException("syncCall must use a synchronous event, not " + message);
        return step(source, target, message);
    }

    /** distinct and sorted */
    public Stream<System> systems() {
        return steps().stream().flatMap(scenarioStep -> Stream.of(scenarioStep.source(), scenarioStep.target())).distinct().sorted();
    }

    public Scenario variant(Condition.Variant variant) {
        Condition.Variant prev = variants.put(variant.condition(), variant);
        if (prev != null)
            throw new IllegalStateException("condition '" + variant.condition().label() +
                    "' already set to '" + prev.label() +
                    "'");
        return this;
    }

    @Override
    public @Nullable String wikiFolder(I18n i18n) {
        return i18n.resolve(Term.scenario);
    }

}
