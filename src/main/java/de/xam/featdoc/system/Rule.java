package de.xam.featdoc.system;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Rule {

    public interface Event {
        String comment();

        Message message();
    }

    interface RuleBuilder {

        RuleBuilder feature(Feature feature);

        RuleWithTriggerBuilder trigger(Message triggerMessage, String triggerComment);
    }

    interface RuleWithTriggerBuilder {


        RuleWithTriggerBuilder actions(Message... actions);

        Rule build();
    }

    public record Trigger(Message incomingMessage, @Nullable String comment) implements Event {
        @Override
        public Message message() {
            return incomingMessage();
        }
    }

    public record Action(Message outgoingMessage, @Nullable String comment) implements Event {
        @Override
        public Message message() {
            return outgoingMessage();
        }
    }

    static class InternalRuleBuilder implements RuleBuilder, RuleWithTriggerBuilder {

        private final List<Action> actions = new ArrayList<>();
        private Feature feature;
        private Trigger trigger;

        @Override
        public RuleWithTriggerBuilder actions(Message... actions) {
            for (Message message : actions) {
                action(message, null);
            }
            return this;
        }

        @Override
        public Rule build() {
            return new Rule(feature, this.trigger, actions);
        }

        @Override
        public RuleBuilder feature(Feature feature) {
            this.feature = feature;
            return this;
        }

        public RuleWithTriggerBuilder trigger(Message triggerMessage) {
            return trigger(triggerMessage, null);
        }

        @Override
        public RuleWithTriggerBuilder trigger(Message triggerMessage, String triggerComment) {
            assert triggerMessage != null;
            this.trigger = new Trigger(triggerMessage, triggerComment);
            return this;
        }

        private RuleWithTriggerBuilder action(Message message, String comment) {
            actions.add(new Action(message, comment));
            return this;
        }
    }

    final Feature feature;
    final List<Action> actions;
    final Trigger trigger;


    public Rule(Feature feature, Trigger trigger, List<Action> actions) {
        assert feature != null;
        assert trigger != null;
        assert actions != null && !actions.isEmpty();
        this.feature = feature;
        this.trigger = trigger;
        this.actions = actions;
    }

    public static RuleBuilder builder(Feature feature) {
        return new InternalRuleBuilder().feature(feature);
    }

    public List<Action> actions() {
        return Collections.unmodifiableList(actions);
    }

    public Feature feature() {
        return feature;
    }

    public Stream<Message> producedEvents() {
        return actions.stream().map(Action::outgoingMessage);
    }

    public String toString() {
        return trigger + "=>{" + Arrays.asList(actions) + "}";
    }

    public Trigger trigger() {
        return trigger;
    }
}
