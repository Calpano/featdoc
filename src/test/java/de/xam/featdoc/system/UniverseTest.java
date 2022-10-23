package de.xam.featdoc.system;

import org.junit.jupiter.api.Test;

class UniverseTest {

    @Test
    void testUsage() {
        Universe universe = new Universe();
        System sysa = universe.system("sysa", "sysaName", "sysaWkiname");
        Message aMessage1 = sysa.eventAsync("a-event1");
        Message aMessage2 = sysa.eventAsync("a-event2");
        System sysb = universe.system("sysb", "sysbName", "sysbWkiname");
        Message bMessage1 = sysb.eventAsync("b-event1");
        Message bMessage2 = sysb.eventAsync("b-event2");
        System sysc = universe.system("sysc", "syscName", "syscWkiname");
        Message cMessage1 = sysa.eventAsync("c-aevent1");
        Message cMessage2 = sysa.eventAsync("c-event2");
        Feature aFeature1 = sysa.feature("a-feature1")
                .rule(aMessage1, bMessage1, cMessage1);
        Feature bFeature1 = sysb.feature("b-feature1")
                .rule(bMessage1, cMessage2);

    }

}