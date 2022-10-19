package de.xam.featdoc.mermaid.sequence;

import java.util.List;
import java.util.stream.Collectors;

public class Note {

    public enum Position {
        RightOf("right of"), LeftOf("left of)"), Over("over");

        private final String mermaid;

        Position(String mermaid) {
            this.mermaid = mermaid;
        }

    }

    private final String text;
    private final Position position;
    private final List<String> participants;

    private Note(Position position, List<String> participants, String text) {
        this.text = text;
        this.position = position;
        this.participants = participants;
    }

    public static Note actorNote(Position position, String participant, String text) {
        return new Note(position, List.of(participant), text);
    }

    public static Note actorsNote(String text, String... participants) {
        return new Note(Position.Over, List.of(participants), text);
    }

    public String mermaid() {
        return "note " + position.mermaid + " " + participants.stream().collect(Collectors.joining(",")) + ": " + text;
    }
}
