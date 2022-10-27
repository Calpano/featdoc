package de.xam.featdoc.system;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @param scenarioStep   initially causing a chain of system reactions
 * @param depth          depth in tree from initial scenario step. 0 = is defined just like this in the scenario; > 0: how indirect the action is triggered
 * @param cause   produced the message
 * @param effect  consumed the message. Can be null for outgoing messages.
 */
public record ResultStep(ScenarioStep scenarioStep, int depth, Cause cause, @Nullable Effect effect ) {

    public static ResultStep indirect(ScenarioStep scenarioStep, int depth, Cause cause, Effect effect) {
        return new ResultStep(scenarioStep, depth, cause, effect);
    }

    public @Nullable String effectComment() {
        return effect==null?null:effect.comment();
    }

    public @Nullable System effectSystem() {
        return effect==null?null:effect.system();
    }

    public boolean isScenario() {
        return depth == 0;
    }

    public ResultStep {
        if (effect != null && !cause.message().equals(effect.message())) {
            throw new IllegalArgumentException("Cause (" + cause.message() + ") and effect (" + effect.message() + ") don't match");
        }
    }

    public Message message() {
        return cause.message();
    }

    public Stream<Rule> rules() {
        return Stream.of(cause, effect).filter(Objects::nonNull).map(CauseAndEffect::rule).distinct();
    }

    @Override
    public String toString() {
        return String.format("%-10s --> %-10s : Msg=%-40s | depth=%s | Feat=%s",
                cause.system().label,
                effect == null? "--" : effect.system().label,
                cause.message().name()+(effect==null?"":" // "+effect.message().name()),
                depth(),
                feature(cause)+"//"+feature(effect)
        );
    }

    private String feature(CauseAndEffect causeAndEffect) {
        if(causeAndEffect==null)
            return "--";
        return "[" + causeAndEffect.system().label + "]." + causeAndEffect.message() + (causeAndEffect.comment() == null ? "no comment" : causeAndEffect.comment());
    }
}
