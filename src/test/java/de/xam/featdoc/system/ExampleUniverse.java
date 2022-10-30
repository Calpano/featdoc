package de.xam.featdoc.system;

/**
 * a has a simple API and triggers
 * <p>
 * - another api call to b
 * - an event
 */
interface ExampleUniverse {
    Universe universe = new Universe();
    System source = universe.system("source", "source", "source");
    System a = universe.system("a", "a", "a");
    System b = universe.system("b", "b", "b");
    System c = universe.system("c", "c", "c");
    System d = universe.system("d", "d", "d");
    System e = universe.system("e", "e", "e");
    Message aCall = a.apiCall("aCall");
    Message aEventOut = a.asyncEventOutgoing("aEventOut");
    Message bCall1 = b.apiCall("bCall1");
    Message bCall2 = b.apiCall("bCall2");
    Message cCall = c.apiCall("cCall");
    Message dEventOut = d.asyncEventOutgoing("dEventOut");
    Message eEventOut = e.asyncEventOutgoing("eEventOut");

    Scenario scenario = universe.scenario("Sceario-1")
            .step(source, aCall, "source-to-a-Comment");

    static void define() {
        a.feature("af")
                .rule(aCall, "aCallComment")
                    .action(bCall1, "af-bCall1Comment")
                    .action(bCall2, "af-bCall2Comment").build()
                .rule(aCall, "aCall2Comment")
                    .action(aEventOut, "a-sendEvent").build();
        b.feature("bf").rule(bCall1, "bCall1Comment")
                .action(cCall, "b-call-c").build();
        d.feature("df").rule(aEventOut, "d-send-aOut")
                .action(cCall, "d-call-c")
                .action(dEventOut, "d-event").build();
        e.feature("ef").rule(aEventOut, "e-send-aOut")
                .action(eEventOut, "e-event").build();
    }
}
