package de.xam.featdoc.system;

import com.google.common.base.Joiner;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import de.xam.featdoc.CausalTree;
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

    class Chain {
        private final ScenarioStep scenarioStep;
        private final Consumer<ResultStep> resultConsumer;

        public Chain(ScenarioStep scenarioStep, Consumer<ResultStep> resultConsumer) {
            this.scenarioStep = scenarioStep;
            this.resultConsumer = resultConsumer;
        }

        public void react() {
            reactOn(0, scenarioStep);
        }

        private void chain(int depth, Cause cause, Effect effect) {
            resultConsumer.accept(ResultStep.indirect(scenarioStep, depth, cause, effect));
        }

        private void reactOn(int depth, Cause cause) {
            AtomicBoolean isAnyRuleTriggered = new AtomicBoolean(false);
            rules().forEach(rule -> {
                if (rule.trigger().isTriggeredBy(cause.message())) {
                    isAnyRuleTriggered.set(true);
                    chain(depth, cause, new RuleEffect(rule, rule.trigger()));
                    for (Rule.Action action : rule.actions()) {
                        RuleEffect ruleEffect = new RuleEffect(rule, action);
                        reactOn(depth + 1, ruleEffect);
                    }
                }
            });
            if (!isAnyRuleTriggered.get()) {
                switch (cause.message().direction()) {
                    case OUTGOING -> chain(depth, cause, null);
                    case INCOMING -> chain(depth, cause, TerminalEffect.of(cause));
                }
            }
        }
    }

    private final List<Scenario> scenarios = new ArrayList<>();
    private final List<System> systems = new ArrayList<>();
    private final List<Condition> conditions = new ArrayList<>();

    public static String commentInMermaidLineLabel(@Nullable String comment) {
        return comment == null ? null : ("(" + comment + ")");
    }

    /**
     * Skip nulls
     */
    public static String join(String joiner, String... parts) {
        return Joiner.on(joiner).skipNulls().join(parts);
    }

    public static CausalTree toCausalTree(ScenarioStep scenarioStep) {
        CausalTree causalTree = CausalTree.create(scenarioStep);
        scenarioStep.scenario().universe().toCausalTree(scenarioStep, causalTree);
        return causalTree;
    }

    public static List<CausalTree> toCausalTrees(Scenario scenario) {
        return scenario.steps().stream().map(Universe::toCausalTree).collect(Collectors.toList());
    }

    public List<ResultStep> computeResultingSteps(Scenario scenario) {
        List<ResultStep> resultingSteps = new ArrayList<>();
        for (ScenarioStep step : scenario.steps()) {
            Chain chain = new Chain(step, resultingSteps::add);
            chain.react();
        }
        return resultingSteps;
    }

    public Stream<Feature> features() {
        return systems().stream().flatMap(system -> system.featureList.stream());
    }

    public Stream<Feature> featuresProducing(Message message) {
        return systems.stream().flatMap(system -> system.features().stream()).filter(feature -> feature.isProducing(message));
    }

    public void forEachEdge(BiConsumer<System, System> source_target) {
        scenarios().stream().flatMap(scenario -> scenario.steps().stream()).forEach(scenarioStep -> source_target.accept(scenarioStep.sourceSystem(), scenarioStep.message().system()));

        systems().stream().flatMap(System::rules).forEach(rule -> rule.actions().forEach(target -> source_target.accept(rule.trigger().message().system(), target.message().system())));
    }

    public void forEachResultingAction(Message message, BiConsumer<Rule, Rule.Action> rule_action, boolean transitive) {
        rules().filter(rule -> rule.trigger().isTriggeredBy(message))
                .forEach(rule -> {
                    rule.actions().forEach(action -> {
                        rule_action.accept(rule, action);
                        if (transitive) {
                            //RECURSE
                            forEachResultingAction(action.message(), rule_action, transitive);
                        }
                    });
                });
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
                // from
                step.cause().system().id,
                // arrow
                step.message().timing() == Timing.Synchronous ? Arrow.SolidWithHead : Arrow.DottedAsync,
                // to
                (step.effect() == null ? step.message().system() : step.effect().system()).id,
                // message on the line
                combinedMessageOnSeqenceDiagram(step.cause(), step.effect())
        ));
        return sequenceDiagram;
    }

    public List<StringTree> toTrees(Scenario scenario, Function<ResultStep, String> toMarkdown) {
        Deque<StringTree> stack = new LinkedList<>();
        StringTree root = new StringTree("ROOT Scenario: " + scenario.label());
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

    /**
     * @throws IllegalStateException if anything is invalid
     */
    public void validate() {
        features().forEach(feature -> {
            if (feature.hasUnfinishedRules()) {
                throw new IllegalStateException(String.format("System '%s' in feature '%s' has an unfinished rule. Likely you forgot to call 'build()'.", feature.system().label, feature.label));
            }
        });
    }

    private String combinedMessageOnSeqenceDiagram(Cause cause, @Nullable Effect effect) {
        final String[] lines;
        if (effect == null) {
            lines = new String[]{
                    commentInMermaidLineLabel(cause.comment())
                    , cause.message().name()
//                    ,cause.rule() == null ? null : "[" + cause.rule().feature().label + "]"
            };
        } else {
            lines = new String[]{
                    commentInMermaidLineLabel(cause.comment())
                    , cause.message().name()
//                    ,Util.combineStrings(
//                            cause.rule() == null ? null : ("[" + cause.rule().feature().label + "]"),
//                            effect.rule() == null ? null : ("[" + effect.rule().feature().label + "]"))
                    , commentInMermaidLineLabel(effect.comment())
            };
        }
        return join("<br/>", lines);
    }

    private Stream<Rule> rules() {
        return systems().stream().flatMap(System::rules);
    }

    /**
     * One cause triggers
     * <p>
     * n systems > m features > k rules > l actions
     * <pre>
     * cause =>
     *   system.feature: action
     * </pre>
     *
     * @param cause
     * @param causalTree
     */
    private void toCausalTree(Cause cause, CausalTree causalTree) {
        forEachResultingAction(cause.message(), (rule, action) -> {
            CausalTree causalChild = causalTree.addEffect(new RuleEffect(rule, action));
            toCausalTree(new RuleCause(rule,action), causalChild);
        },false);
    }

}
