package de.xam.featdoc.system;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import de.xam.featdoc.markdown.MarkdownTool;
import de.xam.featdoc.markdown.StringTree;
import de.xam.featdoc.mermaid.sequence.Arrow;
import de.xam.featdoc.mermaid.sequence.SequenceDiagram;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.xam.featdoc.Util.add;

public class Universe {

    private final List<Scenario> scenarios = new ArrayList<>();
    private final List<System> systems = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();

    public List<ResultStep> computeResultingSteps(Scenario scenario) {
        List<ResultStep> resultingSteps = new ArrayList<>();
        for (ScenarioStep step : scenario.steps()) {
            reactOn(step, resultingSteps::add);
        }
        return resultingSteps;
    }

    public Condition condition(String label) {
        return add(conditions, new Condition(label));
    }

    public Stream<Feature> featuresProducing(Message message) {
        return systems.stream().flatMap(system -> system.features().stream()).filter(feature -> feature.isProducing(message));
    }

    public void forEachEdge(BiConsumer<System, System> source_target) {
        scenarios().stream().flatMap(scenario -> scenario.steps().stream()).forEach(scenarioStep -> source_target.accept(scenarioStep.sourceSystem(), scenarioStep.message().system()));

        systems().stream().flatMap(System::rules).forEach(rule -> rule.actions().forEach(target -> source_target.accept(rule.trigger().message().system(), target.message().system())));
    }

    public Scenario scenario(String title) {
        return add(scenarios, new Scenario(this, title));
    }

    public Stream<ScenarioStep> scenarioStepsProducing(Message message) {
        // TODO equals?
        return scenarios.stream().flatMap(scenario -> scenario.steps().stream()).filter(scenarioStep -> scenarioStep.message().equals(message));
    }

    public List<Scenario> scenarios() {
        return scenarios;
    }

    public System system(String id, String name, String wikiName) {
        return add(systems, new System(id, name, wikiName, 0));
    }

    public System system(String id, String name, String wikiName, int sortOrder) {
        return add(systems, new System(id, name, wikiName, sortOrder));
    }

    public List<System> systems() {
        return Collections.unmodifiableList(systems);
    }

    public Stream<System> systemsCalledFrom(System system) {
        SetMultimap<System, System> systemSystemMap = TreeMultimap.create();
        forEachEdge(systemSystemMap::put);
        Collection<System> targets = systemSystemMap.get(system);
        return targets.stream().sorted();
    }

    /**
     * all systems calling 'system', looking in all rules and scenarios
     */
    public Stream<System> systemsCalling(System system) {
        SetMultimap<System, System> systemSystemMap = TreeMultimap.create();
        forEachEdge(systemSystemMap::put);
        return systemSystemMap.entries().stream().filter(e -> e.getValue().equals(system)).map(Map.Entry::getKey).sorted();
    }

    public Stream<System> systemsProducing(Message message) {
        return systems.stream().filter(system -> system.isProducing(message));
    }

    public SequenceDiagram toSequence(Scenario scenario) {
        List<ResultStep> resultingSteps = computeResultingSteps(scenario);
        SequenceDiagram sequenceDiagram = new SequenceDiagram(scenario.label());
        // participants
        resultingSteps.stream()
                .flatMap(rs -> Stream.of(rs.sourceSystem(), rs.targetSystem()))
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .forEach(system -> sequenceDiagram.participant(system.id, system.label));
        // steps
        resultingSteps.forEach(step -> sequenceDiagram.step(
                step.sourceSystem().id,
                step.message().timing() == Timing.Synchronous ? Arrow.SolidWithHead : Arrow.DottedAsync,
                // FIXME why can targetSytem be null in a resultingStep?
                step.targetSystem()==null?null : step.targetSystem().id,
                MarkdownTool.format(step.message().name() + (step.feature() == null ? "" : " [" + step.feature().label + "]"))));
        return sequenceDiagram;
    }

    public List<StringTree> toTrees(Scenario scenario, Function<ResultStep, String> toMarkdown) {
        Deque<StringTree> stack = new LinkedList<>();
        StringTree root = new StringTree("ROOT Szenario: " + scenario.label());
        stack.add(root);
        List<ResultStep> resultingSteps = computeResultingSteps(scenario);
        int depth = 0;
        for (ResultStep rs : resultingSteps) {
            int delta = depth - rs.depth();
            if (delta > 0) {
                for (int i = 0; i < delta; i++) {
                    stack.pop();
                    depth--;
                }
            }
            StringTree child = stack.peek().addChild(toMarkdown.apply(rs));
            stack.push(child);
            depth++;
        }
        List<StringTree> trees = new ArrayList<>();
        root.getChildNodesIterator().forEachRemaining(trees::add);
        return trees;
    }

    private Stream<Rule> rules() {
        return systems().stream().flatMap(System::rules);
    }

    private void reactOn(ScenarioStep step, Consumer<ResultStep> resultConsumer) {
        ResultStep initialStep = ResultStep.direct(step, 0, step.sourceSystem(), step.message(), step.commentOnMessage(),
                step.message().isIncoming() ? step.message().system() : null);
        reactOn(initialStep, resultConsumer);
    }

    /**
     * Each {@link ResultStep} combines the information from a outgoing action/scenario with an incoming trigger.
     * @param input
     * @param resultConsumer
     */
    private void reactOn(ResultStep input, Consumer<ResultStep> resultConsumer) {
        AtomicBoolean isAnyRuleTriggered = new AtomicBoolean(false);
        rules().forEach(rule -> {
            if(rule.trigger().isTriggeredBy(input.message())) {
                isAnyRuleTriggered.set(true);
                ResultStep inputAndTriggerStep = ResultStep.indirect(
                        input.scenarioStep(),
                        input.depth(),
                        input.sourceSystem(),
                        input.message(),
                        combineComments(input.messageComment(),rule.trigger().comment()),
                        rule.feature().system(),
                        rule);
                resultConsumer.accept(inputAndTriggerStep);
                for(Rule.Action action : rule.actions()) {
                    ResultStep outputStep = ResultStep.indirect(
                            input.scenarioStep(),
                            input.depth()+1,
                            rule.feature().system(),
                            action.message(),
                            action.comment(),
                            // if we CALL another system, we know the targetSystem
                            // if we emit an event, we don't know who listens
                            action.message().isIncoming() ? action.message().system() : null,
                            rule
                    );
                    reactOn(outputStep, resultConsumer);
                }
            }
        });
        if(!isAnyRuleTriggered.get()) {
            resultConsumer.accept(input);
        }
    }

    private static @Nullable String combineComments(@Nullable  String scenarioOrActionComment, @Nullable  String ruleTriggerComment) {
        if (scenarioOrActionComment == null) {
            if (ruleTriggerComment == null) return null;
            else return String.format("Trigger: %s", ruleTriggerComment);
        } else if (ruleTriggerComment == null) {
            return null;
        } else {
            return String.format("%s / Trigger: %s", scenarioOrActionComment, ruleTriggerComment);
        }
    }

    public Stream<Feature> features() {
        return systems().stream().flatMap(system -> system.featureList.stream());
    }

    /**
     * @throws  IllegalStateException if anything is invalid
     */
    public void validate() {
        features().forEach(feature -> {
            if(feature.hasUnfinishedRules()) {
                throw new IllegalStateException(String.format("System '%s' in feature '%s' has an unfinished rule. Likely you forgot to call 'build()'.", feature.system().label, feature.label));
            }
        });
    }

}
