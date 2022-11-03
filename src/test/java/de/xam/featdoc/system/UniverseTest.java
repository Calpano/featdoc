package de.xam.featdoc.system;

import de.xam.featdoc.CausalTree;
import de.xam.featdoc.example.RestaurantSystemsAndScenarios;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static de.xam.featdoc.example.RestaurantSystemsAndScenarios.Systems.UNIVERSE;
import static org.slf4j.LoggerFactory.getLogger;

class UniverseTest {

    private static final Logger log = getLogger(UniverseTest.class);

    @BeforeAll
    static void beforeAll() {
        Example2.initOnce();
        // trigger static init
        Example2.scenario1.label();
    }


    /** check rules:  ? --B.message--> A --B.message--> ? */
//    @Test
//    void testBAB() {
//        systemA.feature("rule-under-test").rule(systemB_event, systemB_call);
//        Scenario scenario = universe.scenario("test").step(systemSource, systemB_event);
//    }


    @Test
    void causalTree() {
        Scenario scenario = Example2.scenario1;
        List<CausalTree> trees = Universe.toCausalTrees(scenario);
        trees.forEach(CausalTree::dump);
    }

    @Test
    void causalTreeCoffee() {
        RestaurantSystemsAndScenarios.define();
        Scenario scenario = UNIVERSE.scenariosList().get(0);
        List<CausalTree> trees = Universe.toCausalTrees(scenario);
        trees.forEach(CausalTree::dump);
    }

    @Test
    void generateExample() throws IOException {
        GenerateExampleDocumentation.generateFiles(Example2.universe,"FeatDocExample");
    }

    @Test
    void resultingAction() {
        List<Rule.Action> actionList = new ArrayList<>();
        Example2.universe.forEachResultingAction(Example2.scenario1.steps().get(0).message(), (rule, action) -> {
            actionList.add(action);
        }, true);
        log.info("Res:\n" + actionList.stream().map(action -> action.message().name()).collect(Collectors.joining("\n,")));
        assertActions(actionList, "bCall1", "cCall", "bCall2", "aEventOut", "cCall", "dEventOut", "eEventOut");
    }

    /**
     * check rules:  ? --A.message--> A --A.message--> ?
     */
    @Test
    void testAAA() {
        Example1.systemA.feature("rule-under-test").rule(Example1.systemA_call, Example1.systemA_event);
        Example1.systemSink.feature("sink").rule(Example1.systemA_event, Example1.systemSink_call);
        Scenario scenario = Example1.universe.scenario("test").step(Example1.systemSource, Example1.systemA_call);
    }

    /**
     * check rules:  ? --B.message--> A --A.message--> ?
     */
    @Test
    void testBAA() {
        Example1.systemA.feature("rule-under-test").rule(Example1.systemB_event, Example1.systemA_event);
        Example1.systemSink.feature("sink").rule(Example1.systemA_event, Example1.systemSink_call);
        Scenario scenario = Example1.universe.scenario("test").step(Example1.systemSource, Example1.systemB_event);
    }

    /**
     * Check rules:  B --B.message--> A --C.message--> C
     */
    @Test
    void testBridge() {
        Example1.systemB.feature("bridge")
                .rule(Example1.systemA_event, "c1").action(Example1.systemC_call, "c2").build();
        Example1.universe.validate();
        Scenario scenario = Example1.universe.scenario("test")
                .step(Example1.systemA, Example1.systemA_event, " c3");
        List<ResultStep> resultingSteps = Example1.universe.computeResultingSteps(scenario);
        dump("Bridge", resultingSteps);
    }

    /**
     * check rules:  ? --A.message--> A --B.message--> ?
     */
    @Test
    void testForeignSystem() {
        Scenario scenario = Example1.createForeignSystemScenario();
        List<ResultStep> resultingSteps = Example1.universe.computeResultingSteps(scenario);
        dump("ForeignSystem", resultingSteps);
    }

    private void assertActions(List<Rule.Action> actual, String... expected) {
        List<String> actualList = actual.stream().map(action -> action.message().name()).collect(Collectors.toList());
        List<String> expectedList = Arrays.asList(expected);
        Assertions.assertIterableEquals(expectedList, actualList," Actual: "+actualList);
    }

    private void dump(String s, List<ResultStep> resultingSteps) {
        StringBuilder b = new StringBuilder();
        b.append("== " + s + "\n");
        for (ResultStep rs : resultingSteps) {
            b.append(s + "   " + rs + "\n");
        }
        log.info("\n" + b.toString());
    }

    @Test
    void testSystemCallingCalled() {
        Example2.initOnce();
        log.info("a calls " + Example2.universe.systemsCalling(Example2.a).toList());
        log.info("c calls " + Example2.universe.systemsCalling(Example2.c).toList());
        log.info("d calls " + Example2.universe.systemsCalling(Example2.d).toList());
        log.info("a called from " + Example2.universe.systemsCalledFrom(Example2.a).toList());
        log.info("c called from " + Example2.universe.systemsCalledFrom(Example2.c).toList());
        log.info("d called from " + Example2.universe.systemsCalledFrom(Example2.d).toList());
        log.info("source calls " + Example2.universe.systemsCalling(Example2.source).toList());
        log.info("source called from " + Example2.universe.systemsCalledFrom(Example2.source).toList());
        log.info("source called from " + Example2.universe.systemsCalledFrom(Example2.source).toList());
        log.info("source called from " + Example2.universe.systemsCalledFrom(Example2.source).toList());

        assertThat(Example2.universe.systemsCalledFrom(Example2.source).toList()).containsExactlyElementsIn(List.of(Example2.a, Example2.f));
        assertThat(Example2.universe.systemsCalledFrom(Example2.a).toList()).containsExactlyElementsIn(List.of(Example2.a, Example2.b, Example2.d, Example2.e));
        assertThat(Example2.universe.systemsCalledFrom(Example2.b).toList()).containsExactlyElementsIn(List.of(Example2.c));
        assertThat(Example2.universe.systemsCalledFrom(Example2.c).toList()).containsExactlyElementsIn(List.of());
        assertThat(Example2.universe.systemsCalledFrom(Example2.d).toList()).containsExactlyElementsIn(List.of(Example2.c, Example2.d));
        assertThat(Example2.universe.systemsCalledFrom(Example2.e).toList()).containsExactlyElementsIn(List.of(Example2.e));
    }


}