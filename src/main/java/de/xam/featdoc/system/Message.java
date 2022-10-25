package de.xam.featdoc.system;

public record Message(System system, Direction direction, Timing timing, String name) {

    /** Templates for messages, non-restricting */
    public enum Kind {
        API_CALL(Direction.INCOMING, Timing.Synchronous),
        WEB_HOOK(Direction.OUTGOING, Timing.Synchronous),
        EVENT_OUTGOING(Direction.OUTGOING, Timing.Asynchronous),
        EVENT_INCOMING(Direction.INCOMING, Timing.Asynchronous),
        UI_INPUT(Direction.INCOMING, Timing.Synchronous),
        UI_OUTPUT(Direction.OUTGOING, Timing.Synchronous);

        private final Direction direction;
        private final Timing timing;

        public Timing timing() {
            return timing;
        }

        public Direction direction() {
            return direction;
        }

        Kind(Direction direction, Timing timing) {
            this.direction = direction;
            this.timing = timing;
        }
    }

    public enum Direction {
        INCOMING, OUTGOING
    }

    public static Message create(System system, Kind kind, String label) {
        return new Message(system, kind.direction, kind.timing, label);
    }

    public boolean isAsynchronous() {
        return timing() == Timing.Asynchronous;
    }

    public boolean isSynchronous() {
        return timing() == Timing.Synchronous;
    }

    public String toString() {
        return system + "-" + name + "(" + (timing() == Timing.Synchronous ? "sync" : "async") + ")";
    }
}
