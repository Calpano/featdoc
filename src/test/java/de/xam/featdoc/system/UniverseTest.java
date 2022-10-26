package de.xam.featdoc.system;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class UniverseTest {

    Universe universe = new Universe();
    System systemSource = universe.system("sourceSystem", "Source", "sysSource");
    System systemA = universe.system("sysa", "A", "sysAAAAAA");
    System systemB = universe.system("sysb", "B", "sysBBBBBB");
    System systemC = universe.system("sysc", "C", "sysCCCCCC");
    System systemSink = universe.system("sink", "Sink", "sys__Sink");

    Message systemA_event = systemA.asyncEventOutgoing("a-event");
    Message systemA_call = systemA.apiCall("a-call");
    Message systemB_event = systemB.asyncEventOutgoing("b-event");
    Message systemB_call = systemB.apiCall("b-call");
    Message systemC_event = systemC.asyncEventOutgoing("c-event");
    Message systemC_call = systemC.apiCall("c-call");
    Message systemSink_call = systemSink.apiCall("sink-call");


    /** check rules:  ? --A.message--> A --A.message--> ? */
    @Test
    void testAAA() {
        systemA.feature("rule-under-test").rule(systemA_call, systemA_call);
        systemSink.feature("sink").rule(systemA_call, systemSink_call);
        Scenario scenario = universe.scenario("test").step(systemSource, systemA_call);
    }
    /** check rules:  ? --B.message--> A --A.message--> ? */
    @Test
    void testBAA() {
        systemA.feature("rule-under-test").rule(systemB_call, systemA_call);
        systemSink.feature("sink").rule(systemA_call, systemSink_call);
        Scenario scenario = universe.scenario("test").step(systemSource, systemB_call);
    }
    /** check rules:  ? --B.message--> A --B.message--> ? */
    @Test
    void testBAB() {
        systemA.feature("rule-under-test").rule(systemB_call, systemB_call);
        systemSink.feature("sink").rule(systemB_call, systemSink_call);
        Scenario scenario = universe.scenario("test").step(systemSource, systemB_call);
    }
    /** check rules:  ? --A.message--> A --B.message--> ? */
    @Test
    void testForeignSystem() {
        systemB.feature("foreignSystem")
                .rule(systemB_call,"c1").action( systemB_event,"c2").build();
        systemSink.feature("sink")
                .rule(systemB_event, "c3").action( systemSink_call,"c4").build();
        universe.validate();
        Scenario scenario = universe.scenario("test")
                .step(systemA, systemB_call, "c0");
        List<ResultStep> resultingSteps = universe.computeResultingSteps(scenario);
        dump("ForeignSystem",resultingSteps);
    }
    /** Check rules:  B --B.message--> A --C.message--> C */
    @Test
    void testBridge() {
        systemB.feature("bridge")
                .rule(systemA_event,"c1").action(systemC_call,"c2").build();
        universe.validate();
        Scenario scenario = universe.scenario("test")
                .step(systemA, systemA_event," c3");
        List<ResultStep> resultingSteps = universe.computeResultingSteps(scenario);
        dump("Bridge",resultingSteps);
    }

    private static final Logger log = getLogger(UniverseTest.class);
    private void dump(String s, List<ResultStep> resultingSteps) {
        StringBuilder b = new StringBuilder();
        b.append("== "+s+"\n");
        for(ResultStep rs : resultingSteps) {
            b.append(s+"   "+rs+"\n");
        }
        log.info("\n"+b.toString());
    }

}