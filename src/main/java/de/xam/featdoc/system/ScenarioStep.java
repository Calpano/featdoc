package de.xam.featdoc.system;

public record Step (Scenario scenario, System source, System target, Rule.Event event) {
}
