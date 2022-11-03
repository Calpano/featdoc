package de.xam.featdoc.system;

public class Example1 {

    public static final Universe universe = new Universe();
    public static final System systemSource = universe.system("sourceSystem", "Source", "sysSource");
    public static final System systemA = universe.system("sysa", "A", "sysAAAAAA");
    public static final Message systemA_event = systemA.asyncEventOutgoing("a-event");
    public static final Message systemA_call = systemA.apiCall("a-call");
    public static final System systemB = universe.system("sysb", "B", "sysBBBBBB");
    public static final Message systemB_event = systemB.asyncEventOutgoing("b-event");
    public static final Message systemB_call = systemB.apiCall("b-call");
    public static final System systemC = universe.system("sysc", "C", "sysCCCCCC");
    public static final Message systemC_event = systemC.asyncEventOutgoing("c-event");
    public static final Message systemC_call = systemC.apiCall("c-call");
    public static final System systemSink = universe.system("sink", "Sink", "sys__Sink");
    public static final Message systemSink_call = systemSink.apiCall("sink-call");

    public static Scenario createForeignSystemScenario() {
        systemB.feature("foreignSystem")
                .rule(systemB_call, "c1").action(systemB_event, "c2").build();
        systemSink.feature("sink")
                .rule(systemB_event, "c3").action(systemSink_call, "c4").build();
        universe.validate();
        Scenario scenario = universe.scenario("test")
                .step(systemA, systemB_call, "c0");
        return scenario;
    }

}
