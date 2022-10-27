package de.xam.featdoc.system;

import javax.annotation.Nullable;

public record RuleEffect(Rule rule, Rule.RulePart rulePart) implements Effect, Cause {

    @Nullable
    @Override
    public String comment() {
        return rulePart.comment();
    }

    @Nullable
    @Override
    public Message message() {
        return rulePart.message();
    }

    @Override
    public System system() {
        return rule.feature().system();
    }
}
