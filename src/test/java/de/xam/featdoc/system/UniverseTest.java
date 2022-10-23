package de.xam.featdoc.system;

import org.junit.jupiter.api.Test;

class UniverseTest {

    @Test
    void testUsage() {
        Universe universe = new Universe();
        System sysa = universe.system("sysa", "sysaName", "sysaWkiname");
        Event aEvent1 = sysa.eventAsync("a-event1");
        Event aEvent2 = sysa.eventAsync("a-event2");
        System sysb = universe.system("sysb", "sysbName", "sysbWkiname");
        Event bEvent1 = sysb.eventAsync("b-event1");
        Event bEvent2 = sysb.eventAsync("b-event2");
        System sysc = universe.system("sysc", "syscName", "syscWkiname");
        Event cEvent1 = sysa.eventAsync("c-aevent1");
        Event cEvent2 = sysa.eventAsync("c-event2");
        Feature aFeature1 = sysa.feature("a-feature1")
                .rule(aEvent1, bEvent1, cEvent1);
        Feature bFeature1 = sysb.feature("b-feature1")
                .rule(bEvent1, cEvent2);

    }

}