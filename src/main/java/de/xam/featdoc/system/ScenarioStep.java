package de.xam.featdoc.system;

public record ScenarioStep(Scenario scenario, System source, System target, Message outgoingMessage, String commentOnMessage) {
}
