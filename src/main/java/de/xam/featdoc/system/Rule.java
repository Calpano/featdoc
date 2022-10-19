package de.xam.featdoc.system;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Rule {

    final Event[] actions;
    final Event trigger;
    @Nullable
    private String comment;

    public Rule(Event trigger, Event... actions) {
        this.trigger = trigger;
        this.actions = actions;
    }

    public List<Event> actions() {
        return Collections.unmodifiableList(Arrays.asList(actions));
    }

    public Rule comment(String comment) {
        this.comment = comment;
        return this;
    }

    public String comment() {
        return comment;
    }

    public Stream<Event> producedEvents() {
        return Stream.of(actions);
    }

    public String toString() {
        return trigger + "=>{" + Arrays.asList(actions) + "}";
    }

    public Event trigger() {
        return trigger;
    }
}
