package de.xam.featdoc.system;

import org.junit.jupiter.api.Test;

class UniverseTest {

    @Test
    void testUsage() {
        Universe universe = new Universe();
        System sysa = universe.system("sysa", "sysaName", "sysaWkiname");
        System sysb = universe.system("sysb", "sysbName", "sysbWkiname");
        System sysc = universe.system("sysc", "syscName", "syscWkiname");
        Feature af1 = sysa.feature("a-f1");
        Event aEvent1 = sysa.eventAsync("a-event1");
        Event aEvent2 = sysa.eventAsync("a-event2");

    }

}