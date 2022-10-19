package de.xam.featdoc.system;

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
    private final List<Step> steps = new ArrayList<>();
    private final Universe universe;
    private Map<Condition, Condition.Variant> variants = new HashMap<>();

    public Scenario(Universe universe, String label) {
        this.universe = universe;
        this.label = label;
    }

    /**
     * Alias for {@link #step(System, System, Event)} to ease refactoring/refinement of scenarios
     */
    public Scenario asyncEvent(System source, System target, Event event) {
        if (!event.isAsynchronous())
            throw new IllegalStateException("asyncEvent must use an asynchronous event, not " + event);
        return step(source, target, event);
    }

    public Scenario asyncEvent(System source, System target, String event) {
        return step(source, target, new Event(target, Timing.Asynchronous, event));
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String localTarget() {
        return MarkdownTool.filename(label());
    }

    public Scenario step(System source, System target, Event event) {
        Step step = new Step(this, source, target, event);
        steps.add(step);
        return this;
    }

    public List<Step> steps() {
        return Collections.unmodifiableList(steps);
    }

    public Scenario syncCall(System source, System target, String eventName) {
        return step(source, target, new Event(target, Timing.Synchronous, eventName));
    }

    /**
     * Alias for {@link #step(System, System, Event)} to ease refactoring/refinement of scenarios
     */
    public Scenario syncCall(System source, System target, Event event) {
        if (!event.isSynchronous())
            throw new IllegalStateException("syncCall must use a synchronous event, not " + event);
        return step(source, target, event);
    }

    /** distinct and sorted */
    public Stream<System> systems() {
        return steps().stream().flatMap(step -> Stream.of(step.source(), step.target())).distinct().sorted();
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
    public @Nullable String wikiFolder() {
        return "Szenario";
    }

}