package de.xam.featdoc.system;

import javax.annotation.Nullable;

public record ScenarioStep(Scenario scenario, System sourceSystem, Message message, String commentOnMessage) implements Cause {
    @Nullable
    @Override
    public String comment() {
        return commentOnMessage();
    }

    @Nullable
    @Override
    public Rule rule() {
        return null;
    }

    @Override
    public System system() {
        return sourceSystem;
    }
}
