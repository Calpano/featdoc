package de.xam.featdoc.system;

public record ScenarioStep(Scenario scenario, System source, Message outgoingMessage, String commentOnMessage) {
}
