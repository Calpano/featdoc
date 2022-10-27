package de.xam.featdoc.mermaid.sequence;

import javax.annotation.Nullable;

/**
 * @param from
 * @param arrow
 * @param lifetimeEvent
 * @param to
 * @param message
 * @param mermaidSourceCodeComment       to be rendered in the sourceSystem code of the diagram as mermaidSourceCodeComment
 */
public record SequenceStep(String from, Arrow arrow, LifetimeEvent lifetimeEvent, String to, String message,
                           @Nullable String mermaidSourceCodeComment) {

    public enum LifetimeEvent {
        Activate("+"), None(""), Deactivate("-");

        public final String mermaid;

        LifetimeEvent(String mermaid) {
            this.mermaid = mermaid;
        }
    }

    public SequenceStep {
        if (from == null) throw new IllegalArgumentException();
        if (arrow == null) throw new IllegalArgumentException();
        if (lifetimeEvent == null) throw new IllegalArgumentException();
        if (to == null) throw new IllegalArgumentException();
        if (message == null) throw new IllegalArgumentException();
    }

    public String mermaid() {
        return from + arrow.mermaid + lifetimeEvent.mermaid + to + ": " + message;
    }
}
