package de.xam.featdoc.system;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * a has a simple API and triggers
 * <p>
 * - another api call to b
 * - an event
 * <pre>
 *     SOURCE
 *     A (-> aCall; aEventOut -> )
 *     B (-> bCall1, bCall2)
 *     C
 *     D (dEventOut ->)
 *     E (eEventOut ->)
 *     F (-> fCall)
 * </pre>
 */
public class Example2 {
    private static final Logger log = getLogger(Example2.class);

    public static final Universe universe = new Universe();
    public static final System source = universe.system("source", "source", "source");
    public static final System a = universe.system("a", "a", "a");
    public static final System b = universe.system("b", "b", "b");
    public static final System c = universe.system("c", "c", "c");
    public static final System d = universe.system("d", "d", "d");
    public static final System e = universe.system("e", "e", "e");
    public static final System f = universe.system("f", "f", "f");
    public static final Message aCall = a.apiCall("aCall");
    public static final Message aEventOut = a.asyncEventOutgoing("aEventOut");
    public static final Message bCall1 = b.apiCall("bCall1");
    public static final Message bCall2 = b.apiCall("bCall2");
    public static final Message cCall = c.apiCall("cCall");
    public static final Message dEventOut = d.asyncEventOutgoing("dEventOut");
    public static final Message eEventOut = e.asyncEventOutgoing("eEventOut");
    public static final Message fCall = f.apiCall("fCall");

    public static Scenario scenario1;

    static void defineScenarios() {
        scenario1 = universe.scenario("Scenario-1")
                .step(source, aCall, "source-to-a-Comment")
                .step(source, fCall,"source-calls-f-nothing-happens")
        ;
        log.info("ExampleUniverse scenarios defined.");
    }


        /**
         * <pre>
         *  A/af: A.aCall => B.bCall1, B.bCall2
         *  A/af: A.aCall => A.aEventOut ...
         *  B/bf: B.bCall1 => C.cCall
         *  D/df: A.aEventOut => C.cCall, D.dEventOut ...
         *  E/ef: A.aEventOut => eEventOut ...
         * </pre>
         */
    static void defineSystems() {
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
        log.info("ExampleUniverse systems defined.");
    }


    private static boolean initialized = false;

    static synchronized void initOnce() {
        if(!initialized) {
            defineSystems();
            defineScenarios();
            initialized=true;
        }
    }
}
