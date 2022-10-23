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
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.xam.featdoc.Util.add;

public class Universe {

    /**
     * @param feature
     * @param rule
     * @param action
     * @param depth             0 = is defined just like this in the scenario; > 0: how indirect the action is triggered
     * @param causeFromScenario
     * @param source
     * @param target
     */
    public record ResultStep(Feature feature, Rule rule, Event action, int depth, Step causeFromScenario, System source,
                             System target) {

        public boolean isScenario() {
            return depth == 0;
        }
    }

    private List<Scenario> scenarios = new ArrayList<>();
    private List<System> systems = new ArrayList<>();
    private List<Condition> conditions = new ArrayList<>();

    public List<ResultStep> computeResultingSteps(Scenario scenario) {
        List<ResultStep> resultingSteps = new ArrayList<>();
        for (Step step : scenario.steps()) {
            // is anything triggered?
            reactOnEventAndMaterializeActions(step.event(), step.source(), step.target(), step, 0, resultingSteps::add);
        }
        return resultingSteps;
    }

    public Condition condition(String label) {
        return add(conditions, new Condition(label));
    }

    public Stream<Feature> featuresProducing(Event event) {
        return systems.stream().flatMap(system -> system.features().stream()).filter(feature -> feature.isProducing(event));
    }

    public void forEachEdge(BiConsumer<System, System> source_target) {
        scenarios().stream().flatMap(scenario -> scenario.steps().stream()).forEach(step -> source_target.accept(step.source(), step.target()));

        systems().stream().flatMap(System::rules).forEach(rule -> {
            Stream.of(rule.actions).forEach(target -> {
                source_target.accept(rule.trigger.system(), target.system());
            });
        });
    }

    public Scenario scenario(String title) {
        return add(scenarios, new Scenario(this, title));
    }

    public Stream<Step> scenarioStepsProducing(Event event) {
        return scenarios.stream().flatMap(scenario -> scenario.steps().stream()).filter(step -> step.event().equals(event));
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

    public Stream<System> systemsProducing(Event event) {
        return systems.stream().filter(system -> system.isProducing(event));
    }

    public SequenceDiagram toSequence(Scenario scenario) {
        List<ResultStep> resultingSteps = computeResultingSteps(scenario);
        SequenceDiagram sequenceDiagram = new SequenceDiagram(scenario.label());
        // participants
        resultingSteps.stream().flatMap(rs -> Stream.of(rs.source, rs.target)).distinct().sorted().forEach(system -> sequenceDiagram.participant(system.id, system.label));
        // steps
        resultingSteps.forEach(step -> sequenceDiagram.step(step.source.id, step.action.timing() == Timing.Synchronous ? Arrow.SolidWithHead : Arrow.DottedAsync, step.target.id, MarkdownTool.format(step.action.label() + (step.feature == null ? "" : " [" + step.feature.label + "]"))));
        return sequenceDiagram;
    }

    public List<StringTree> toTrees(Scenario scenario, Function<ResultStep, String> toMarkdown) {
        Stack<StringTree> stack = new Stack<>();
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

    private void reactOnEventAndMaterializeActions(Event trigger, System source, @Nullable System target, Step causeFromScenario, int depth, Consumer<ResultStep> resultConsumer) {
        if (target != null) {
            resultConsumer.accept(new ResultStep(null, null, trigger, depth, causeFromScenario, source, target));
        }
        for (System system : systems()) {
            for (Feature feature : system.features) {
                for (Rule rule : feature.rules) {
                    if (rule.trigger.equals(trigger)) {
                        for (Event action : rule.actions) {
                            resultConsumer.accept(new ResultStep(feature, rule, action, depth+1, causeFromScenario, trigger.system(), action.system()));
                            // recursively react
                            reactOnEventAndMaterializeActions(action, action.system(), null, causeFromScenario, depth + 1, resultConsumer);
                        }
                    }
                }
            }
        }
    }
}
