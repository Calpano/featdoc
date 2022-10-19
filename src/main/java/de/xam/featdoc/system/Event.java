package de.xam.featdoc.system;

import de.xam.featdoc.wiki.IWikiLink;
import org.jetbrains.annotations.Nullable;

public record Event(System system, Timing timing, String label) implements IWikiLink {

    public boolean isAsynchronous() {
        return timing == Timing.Asynchronous;
    }

    public boolean isSynchronous() {
        return timing == Timing.Synchronous;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public @Nullable String wikiFolder() {
        return "Event";
    }

    public String toString() {
        return system + "-" + label + "(" + (timing == Timing.Synchronous ? "sync" : "async") + ")";
    }
}
