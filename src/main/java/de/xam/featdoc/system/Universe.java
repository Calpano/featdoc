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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.xam.featdoc.Util.add;

public class Universe {

    /**
     * @param feature
     * @param rule
     * @param rulePart result of applying a rule or scenario step
     * @param depth             0 = is defined just like this in the scenario; > 0: how indirect the action is triggered
     * @param causeFromScenario
     * @param source
     * @param target
     */
    public record ResultStep(Feature feature, Rule rule, Rule.RulePart rulePart, int depth, ScenarioStep causeFromScenario, System source, System target) {

        public boolean isScenario() {
            return depth == 0;
        }
    }

    private final List<Scenario> scenarios = new ArrayList<>();
    private final List<System> systems = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();



    public Condition condition(String label) {
        return add(conditions, new Condition(label));
    }

    public Stream<Feature> featuresProducing(Message message) {
        return systems.stream().flatMap(system -> system.features().stream()).filter(feature -> feature.isProducing(message));
    }

    public void forEachEdge(BiConsumer<System, System> source_target) {
        scenarios().stream().flatMap(scenario -> scenario.steps().stream()).forEach(scenarioStep -> source_target.accept(scenarioStep.source(), scenarioStep.target()));

        systems().stream().flatMap(System::rules).forEach(rule -> rule.actions.forEach(target -> source_target.accept(rule.trigger.incomingMessage().system(), target.outgoingMessage().system())));
    }

    public Scenario scenario(String title) {
        return add(scenarios, new Scenario(this, title));
    }

    public Stream<ScenarioStep> scenarioStepsProducing(Message message) {
        return scenarios.stream().flatMap(scenario -> scenario.steps().stream()).filter(scenarioStep -> scenarioStep.rulePart().message().equals(message));
    }

    public List<Scenario> scenarios() {
        return scenarios;
    }

    public System system(String id, String name, String wikiName) {
        return add(systems, new System(id, name, wikiName));
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
        resultingSteps.stream().flatMap(rs -> Stream.of(rs.source, rs.target)).distinct().sorted().forEach(system -> sequenceDiagram.participant(system.id, system.label));
        // steps
        resultingSteps.forEach(step -> sequenceDiagram.step(step.source.id,
                step.rulePart.message().timing() == Timing.Synchronous ? Arrow.SolidWithHead : Arrow.DottedAsync,
                step.target.id,
                MarkdownTool.format(step.rulePart.message().label() + (step.feature == null ? "" : " [" + step.feature.label + "]"))));
        return sequenceDiagram;
    }

    public List<StringTree> toTrees(Scenario scenario, Function<ResultStep, String> toMarkdown) {
        Deque<StringTree> stack = new LinkedList<>();
        StringTree root = new StringTree("ROOT Szenario: "+scenario.label());
        stack.add(root);
        List<ResultStep> resultingSteps = computeResultingSteps(scenario);
        int depth = 0;
        for (ResultStep rs : resultingSteps) {
            int delta = depth - rs.depth;
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

    public List<ResultStep> computeResultingSteps(Scenario scenario) {
        List<ResultStep> resultingSteps = new ArrayList<>();
        for (ScenarioStep scenarioStep : scenario.steps()) {
            // is anything triggered?
            reactOnEventAndMaterializeActions(scenarioStep.rulePart(), scenarioStep.source(), scenarioStep.target(), scenarioStep, 0, resultingSteps::add);
        }
        return resultingSteps;
    }

    /**
     *
     * @param triggerRulePart a scenario step or rule action
     * @param source sending system
     * @param target receiving system
     * @param causeFromScenario initial scenario step
     * @param depth in tree from initial scenario step
     * @param resultConsumer
     */
    private void reactOnEventAndMaterializeActions(Rule.RulePart triggerRulePart, System source, @Nullable System target, ScenarioStep causeFromScenario, int depth, Consumer<ResultStep> resultConsumer) {
        if (target != null) {
            resultConsumer.accept(new ResultStep(null, null, triggerRulePart, depth, causeFromScenario, source, target));
        }
        for (System system : systems()) {
            for (Feature feature : system.features) {
                for (Rule rule : feature.rules) {
                    if (rule.trigger.isTriggeredBy(triggerRulePart.message())) {
                        for (Rule.Action action : rule.actions) {
                            resultConsumer.accept(new ResultStep(feature, rule, action, depth+1, causeFromScenario, triggerRulePart.message().system(), action.outgoingMessage().system()));
                            // recursively react
                            reactOnEventAndMaterializeActions(action, action.outgoingMessage().system(), null, causeFromScenario, depth + 1, resultConsumer);
                        }
                    }
                }
            }
        }
    }
}
