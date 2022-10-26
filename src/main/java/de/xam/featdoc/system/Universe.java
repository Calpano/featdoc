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
        // TODO equals?
        return scenarios.stream().flatMap(scenario -> scenario.steps().stream()).filter(scenarioStep -> scenarioStep.outgoingMessage().equals(message));
    }

    public List<Scenario> scenarios() {
        return scenarios;
    }

    public System system(String id, String name, String wikiName) {
        return add(systems, new System(id, name, wikiName,0));
    }

    public System system(String id, String name, String wikiName, int sortOrder) {
        return add(systems, new System(id, name, wikiName,sortOrder));
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
        resultingSteps.stream().flatMap(rs -> Stream.of(rs.source(), rs.target())).distinct().sorted().forEach(system -> sequenceDiagram.participant(system.id, system.label));
        // steps
        resultingSteps.forEach(step -> sequenceDiagram.step(step.source().id,
                step.message().timing() == Timing.Synchronous ? Arrow.SolidWithHead : Arrow.DottedAsync,
                step.target().id,
                MarkdownTool.format(step.message().name() + (step.feature() == null ? "" : " [" + step.feature().label + "]"))));
        return sequenceDiagram;
    }

    public List<StringTree> toTrees(Scenario scenario, Function<ResultStep, String> toMarkdown) {
        Deque<StringTree> stack = new LinkedList<>();
        StringTree root = new StringTree("ROOT Szenario: "+scenario.label());
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

    public List<ResultStep> computeResultingSteps(Scenario scenario) {
        List<ResultStep> resultingSteps = new ArrayList<>();
        for (ScenarioStep step : scenario.steps()) {
            // systems react
            reactOnEventAndMaterializeActions(step, 0, step.source(), step.target(), step.outgoingMessage(), step.commentOnMessage(), resultingSteps::add);
        }
        return resultingSteps;
    }

    /**
     * @param causeFromScenario     initial scenario step
     * @param depth                 in tree from initial scenario step
     * @param source                sending system
     * @param target                receiving system
     * @param triggerMessage        a scenario step or rule action
     * @param triggerMessageComment a comment on the message
     * @param resultConsumer
     */
    private void reactOnEventAndMaterializeActions(ScenarioStep causeFromScenario, int depth, System source, @Nullable System target, Message triggerMessage, @Nullable String triggerMessageComment, Consumer<ResultStep> resultConsumer) {
        if (target != null) {
            resultConsumer.accept(new ResultStep(null, null, triggerMessage, triggerMessageComment, depth, causeFromScenario, source, target));
        }
        forEachRule( (feature,rule) -> {
            if (rule.trigger.isTriggeredBy(triggerMessage)) {
                for (Rule.Action action : rule.actions) {

                    resultConsumer.accept(new ResultStep(feature, rule, action.outgoingMessage(), action.comment(), depth+1, causeFromScenario, triggerMessage.system(), action.outgoingMessage().system()));
                    // recursively react
                    reactOnEventAndMaterializeActions(causeFromScenario, depth + 1, action.outgoingMessage().system(), null, action.message(),action.comment(), resultConsumer);
                }
            }
        } );
    }

    private void forEachRule( BiConsumer<Feature,Rule> feature_rule) {
        for (System system : systems()) {
            for (Feature feature : system.features) {
                for (Rule rule : feature.rules) {
                    feature_rule.accept(feature,rule);
                }
            }
        }
    }
}
