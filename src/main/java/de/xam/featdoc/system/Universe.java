package de.xam.featdoc.system;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import de.xam.featdoc.Util;
import de.xam.featdoc.markdown.StringTree;
import de.xam.featdoc.mermaid.sequence.Arrow;
import de.xam.featdoc.mermaid.sequence.SequenceDiagram;

import javax.annotation.Nullable;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.xam.featdoc.Util.add;

public class Universe {

    private final List<Scenario> scenarios = new ArrayList<>();
    private final List<System> systems = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();

    public List<ResultStep> computeResultingSteps(Scenario scenario) {
        List<ResultStep> resultingSteps = new ArrayList<>();
        for (ScenarioStep step : scenario.steps()) {
            reactOn(step, 0, step,  resultingSteps::add);
        }
        return resultingSteps;
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
                .flatMap(rs -> Stream.of(rs.cause().system(), rs.effectSystem()))
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .forEach(system -> sequenceDiagram.participant(system.id, system.label));
        // steps
        resultingSteps.forEach(step -> sequenceDiagram.step(
                step.cause().system().id,
                step.message().timing() == Timing.Synchronous ? Arrow.SolidWithHead : Arrow.DottedAsync,
                (step.effect()==null? step.message().system() : step.effect().system()).id,
                combinedMessage(step.cause(), step.effect())
        ));
        return sequenceDiagram;
    }

    private String combinedMessage(Cause cause, @Nullable Effect effect) {
        final Stream<String> lines;
        if(effect==null) {
            lines = Stream.of(
                    cause.comment() == null ? null : ("'" + cause.comment() + "'"),
                    cause.message().name(),
                    cause.rule() == null ? null : "[" + cause.rule().feature().label + "]"
            );
        } else {
            lines = Stream.of(
                    cause.comment() == null ? null : ("'" + cause.comment() + "'"),
                    cause.message().name(),
                    Util.combineStrings(
                            cause.rule() == null ? null : ("[" + cause.rule().feature().label + "]"),
                            effect.rule() == null ? null : ("[" + effect.rule().feature().label + "]")),
                    effect.comment() == null ? null : ("'" + effect.comment() + "'")
            );
        }
        return lines.filter(Objects::nonNull).collect(Collectors.joining("<br/>"));
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


    /**
     * Each {@link ResultStep} combines the information from a outgoing action/scenario with an incoming trigger.
     * @param input
     * @param resultConsumer
     */
    private void reactOn(ScenarioStep scenarioStep, int depth, Cause cause, Consumer<ResultStep> resultConsumer) {
        AtomicBoolean isAnyRuleTriggered = new AtomicBoolean(false);
        rules().forEach(rule -> {
            if(rule.trigger().isTriggeredBy(cause.message())) {
                isAnyRuleTriggered.set(true);
                ResultStep inputAndTriggerStep = ResultStep.indirect(
                        scenarioStep,
                        depth,
                        cause,
                        new RuleEffect(rule,rule.trigger())
                );
                resultConsumer.accept(inputAndTriggerStep);
                for(Rule.Action action : rule.actions()) {
                    RuleEffect ruleEffect = new RuleEffect(rule, action);
                    reactOn(scenarioStep, depth+1, ruleEffect, resultConsumer);
                }
            }
        });
        if(!isAnyRuleTriggered.get()) {
            ResultStep outgoingEventToSelf = ResultStep.indirect(
                    scenarioStep,
                    depth,
                    cause,
                    null
            );
            resultConsumer.accept(outgoingEventToSelf);
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
