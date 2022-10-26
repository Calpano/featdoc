package de.xam.featdoc.system;

public record ScenarioStep(Scenario scenario, System sourceSystem, Message message, String commentOnMessage) {
}
