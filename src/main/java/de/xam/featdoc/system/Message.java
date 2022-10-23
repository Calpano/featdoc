package de.xam.featdoc.system;

public record Message(System system, Timing timing, String label) {

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
    
    public String toString() {
        return system + "-" + label + "(" + (timing == Timing.Synchronous ? "sync" : "async") + ")";
    }
}
