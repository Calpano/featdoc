package de.xam.featdoc.system;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Rule {

    public interface RulePart {
        String comment();

        Message message();
    }

    public interface RuleBuilder {

        RuleBuilder feature(Feature feature);

        RuleWithTriggerBuilder trigger(Message triggerMessage, String triggerComment);

        default RuleWithTriggerBuilder trigger(Message triggerMessage) {
            return trigger(triggerMessage, null);
        }
    }

    public interface RuleWithTriggerBuilder {


        RuleWithTriggerBuilder action(Message action, String comment);

        default RuleWithTriggerBuilder action(Message action) {
            return action(action, null);
        }

        RuleWithTriggerBuilder actions(Message... actions);

        Feature build();
    }

    public record Trigger(Message message, @Nullable String comment) implements RulePart {
        public Trigger {
            if (message == null)
                throw new IllegalArgumentException();
        }

        public boolean isTriggeredBy(Message message) {
            return message().name().equals(message.name()) && message().system().equals(message.system());
        }

    }

    public record Action(Message message, @Nullable String comment) implements RulePart {
        public Action {
            if (message == null)
                throw new IllegalArgumentException();
        }

    }

    static class InternalRuleBuilder implements RuleBuilder, RuleWithTriggerBuilder {

        private final List<Action> actions = new ArrayList<>();
        private Feature feature;
        private Trigger trigger;

        @Override
        public RuleWithTriggerBuilder action(Message message, String comment) {
            actions.add(new Action(message, comment));
            return this;
        }

        @Override
        public RuleWithTriggerBuilder actions(Message... actions) {
            for (Message message : actions) {
                action(message, null);
            }
            return this;
        }

        @Override
        public Feature build() {
            feature.rules.add(new Rule(feature, this.trigger, actions));
            feature.rulesUnderConstruction.remove(this);
            return feature;
        }

        @Override
        public RuleBuilder feature(Feature feature) {
            this.feature = feature;
            return this;
        }

        @Override
        public RuleWithTriggerBuilder trigger(Message triggerMessage, String triggerComment) {
            if(triggerMessage==null)
                throw new IllegalArgumentException("Trigger is null");
            this.trigger = new Trigger(triggerMessage, triggerComment);
            feature.rulesUnderConstruction.add(this);
            return this;
        }

        public RuleWithTriggerBuilder trigger(Message triggerMessage) {
            return trigger(triggerMessage, null);
        }
    }

    private final Feature feature;
    private final List<Action> actions;
    private final Trigger trigger;


    /**
     * @param feature
     * @param trigger
     * @param actions may be empty/null if this system is just listening
     */
    public Rule(Feature feature, Trigger trigger, @Nullable List<Action> actions) {
        if (trigger == null)
            throw new IllegalArgumentException("null-trigger");
        if (trigger.message().isIncoming() && !trigger.message().system().equals(feature.system())) {
            throw new IllegalArgumentException(String.format("Cannot consume an incoming message '%s' (defined in system '%s') in rule of system '%s' -- see feature '%s'",
                    trigger.message.name(),
                    trigger.message.system().label,
                    feature.system().label,
                    feature.label));
        }
        this.actions = actions == null ? new ArrayList<>() : actions;
        for (Action action : actions) {
            if (action.message().isOutgoing() && !action.message().system().equals(feature.system())) {
                throw new IllegalArgumentException(String.format("Cannot produce an outgoing message '%s' (defined in system '%s') in rule of system '%s' -- see feature '%s'",
                        action.message().name(),
                        action.message().system().label, feature.system().label, feature.label));
            }
        }
        this.feature = feature;
        this.trigger = trigger;
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
        return actions.stream().map(Action::message);
    }

    public String toString() {
        return trigger + "=>{" + Arrays.asList(actions) + "}";
    }

    public Trigger trigger() {
        return trigger;
    }
}
