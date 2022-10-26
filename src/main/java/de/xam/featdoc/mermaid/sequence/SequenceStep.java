package de.xam.featdoc.mermaid.sequence;

/**
 * @param from
 * @param arrow
 * @param lifetimeEvent
 * @param to
 * @param message
 * @param comment       to be rendered in the sourceSystem code of the diagram as comment
 */
public record SequenceStep(String from, Arrow arrow, LifetimeEvent lifetimeEvent, String to, String message,
                           String comment) {

    public enum LifetimeEvent {
        Activate("+"), None(""), Deactivate("-");

        public final String mermaid;

        LifetimeEvent(String mermaid) {
            this.mermaid = mermaid;
        }
    }


    public String mermaid() {
        return from + arrow.mermaid + lifetimeEvent.mermaid + to + ": " + message;
    }
}
