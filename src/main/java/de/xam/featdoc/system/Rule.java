package de.xam.featdoc.system;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Rule {

    final Feature feature;
    final Message[] actions;
    final Message trigger;
    @Nullable
    private String comment;

    public Rule(Feature feature, Message trigger, Message... actions) {
        this.feature = feature;
        this.trigger = trigger;
        this.actions = actions;
    }

    public List<Message> actions() {
        return Collections.unmodifiableList(Arrays.asList(actions));
    }

    public Rule comment(String comment) {
        this.comment = comment;
        return this;
    }

    public String comment() {
        return comment;
    }

    public Feature feature() {
        return feature;
    }

    public Stream<Message> producedEvents() {
        return Stream.of(actions);
    }

    public String toString() {
        return trigger + "=>{" + Arrays.asList(actions) + "}";
    }

    public Message trigger() {
        return trigger;
    }
}
