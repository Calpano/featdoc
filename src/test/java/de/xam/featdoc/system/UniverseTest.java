package de.xam.featdoc.system;

import de.xam.featdoc.CausalTree;
import de.xam.featdoc.example.RestaurantSystemsAndScenarios;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.xam.featdoc.example.RestaurantSystemsAndScenarios.Systems.UNIVERSE;
import static org.slf4j.LoggerFactory.getLogger;

class UniverseTest {

    private static final Logger log = getLogger(UniverseTest.class);

    static {
        ExampleUniverse.define();
    }

    Universe universe = new Universe();
    System systemSource = universe.system("sourceSystem", "Source", "sysSource");
    System systemA = universe.system("sysa", "A", "sysAAAAAA");
    Message systemA_event = systemA.asyncEventOutgoing("a-event");
    Message systemA_call = systemA.apiCall("a-call");
    System systemB = universe.system("sysb", "B", "sysBBBBBB");
    Message systemB_event = systemB.asyncEventOutgoing("b-event");
    Message systemB_call = systemB.apiCall("b-call");
    System systemC = universe.system("sysc", "C", "sysCCCCCC");
    Message systemC_event = systemC.asyncEventOutgoing("c-event");
    Message systemC_call = systemC.apiCall("c-call");
    System systemSink = universe.system("sink", "Sink", "sys__Sink");
    Message systemSink_call = systemSink.apiCall("sink-call");
    /** check rules:  ? --B.message--> A --B.message--> ? */
//    @Test
//    void testBAB() {
//        systemA.feature("rule-under-test").rule(systemB_event, systemB_call);
//        Scenario scenario = universe.scenario("test").step(systemSource, systemB_event);
//    }

    public Scenario createForeignSystemScenario() {
        systemB.feature("foreignSystem")
                .rule(systemB_call, "c1").action(systemB_event, "c2").build();
        systemSink.feature("sink")
                .rule(systemB_event, "c3").action(systemSink_call, "c4").build();
        universe.validate();
        Scenario scenario = universe.scenario("test")
                .step(systemA, systemB_call, "c0");
        return scenario;
    }

    @Test
    void causalTree() {
        Scenario scenario = ExampleUniverse.scenario;
        List<CausalTree> trees = Universe.toCausalTrees(scenario);
        trees.forEach(CausalTree::dump);
    }

    @Test
    void causalTreeCoffee() {
        RestaurantSystemsAndScenarios.define();
        Scenario scenario = UNIVERSE.scenarios().get(0);
        List<CausalTree> trees = Universe.toCausalTrees(scenario);
        trees.forEach(CausalTree::dump);
    }

    @Test
    void generateExample() throws IOException {
        GenerateExampleDocumentation.generateFiles(ExampleUniverse.universe);
    }

    @Test
    void resultingAction() {
        List<Rule.Action> actionList = new ArrayList<>();
        ExampleUniverse.universe.forEachResultingAction(ExampleUniverse.scenario.steps().get(0).message(), (rule, action) -> {
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
        systemA.feature("rule-under-test").rule(systemA_call, systemA_event);
        systemSink.feature("sink").rule(systemA_event, systemSink_call);
        Scenario scenario = universe.scenario("test").step(systemSource, systemA_call);
    }

    /**
     * check rules:  ? --B.message--> A --A.message--> ?
     */
    @Test
    void testBAA() {
        systemA.feature("rule-under-test").rule(systemB_event, systemA_event);
        systemSink.feature("sink").rule(systemA_event, systemSink_call);
        Scenario scenario = universe.scenario("test").step(systemSource, systemB_event);
    }

    /**
     * Check rules:  B --B.message--> A --C.message--> C
     */
    @Test
    void testBridge() {
        systemB.feature("bridge")
                .rule(systemA_event, "c1").action(systemC_call, "c2").build();
        universe.validate();
        Scenario scenario = universe.scenario("test")
                .step(systemA, systemA_event, " c3");
        List<ResultStep> resultingSteps = universe.computeResultingSteps(scenario);
        dump("Bridge", resultingSteps);
    }

    /**
     * check rules:  ? --A.message--> A --B.message--> ?
     */
    @Test
    void testForeignSystem() {
        Scenario scenario = createForeignSystemScenario();
        List<ResultStep> resultingSteps = universe.computeResultingSteps(scenario);
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

}