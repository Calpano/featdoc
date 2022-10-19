package de.xam.featdoc.mermaid.sequence;

import de.xam.featdoc.markdown.MarkdownTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unsupported features from <a href="https://mermaid-js.github.io/mermaid/#/sequenceDiagram">mermaid sequence diagram</a>
 * <p>
 * Loops, Alt, Else, Opt, parallel, critical, break,
 * <p>
 * block nesting
 * <p>
 * colored background rects
 */
public class SequenceDiagram implements MermaidDiagram{

    public boolean isAutonumber = false;

    private List<SequenceStep> sequenceSteps = new ArrayList<>();

    private List<Participant> participants = new ArrayList<>();

    private List<Note> notes = new ArrayList<>();
    private String title;

    public SequenceDiagram(String title) {
        this.title = title;
    }

    public SequenceDiagram actor(String id) {
        participants.add(Participant.actor(id));
        return this;
    }

    public SequenceDiagram actor(String id, String label) {
        participants.add(Participant.actor(id, label));
        return this;
    }

    public String filename() {
        return MarkdownTool.filename(title);
    }

    public SequenceDiagram note(Note.Position position, String participant, String text) {
        notes.add(Note.actorNote(position, participant, text));
        return this;
    }

    public SequenceDiagram note(String participantStart, String participantEnd, String text) {
        notes.add(Note.actorsNote(text, participantStart, participantEnd));
        return this;
    }

    public List<Note> notes() {
        return Collections.unmodifiableList(notes);
    }

    public SequenceDiagram participant(String id) {
        this.participants.add(Participant.participant(id));
        return this;
    }

    public SequenceDiagram participant(String id, String label) {
        this.participants.add(Participant.participant(id, label));
        return this;
    }

    public SequenceDiagram participants(Participant... participants) {
        this.participants.addAll(List.of(participants));
        return this;
    }

    public List<Participant> participants() {
        return Collections.unmodifiableList(participants);
    }

    public SequenceDiagram step(String fromParticipant, Arrow arrow, SequenceStep.LifetimeEvent lifetimeEvent, String toParticipant, String message, String comment) {
        this.sequenceSteps.add(new SequenceStep(fromParticipant, arrow, lifetimeEvent, toParticipant, message, comment));
        return this;
    }

    public SequenceDiagram step(String fromParticipant, Arrow arrow, String toParticipant, String message) {
        return step(fromParticipant, arrow, SequenceStep.LifetimeEvent.None, toParticipant, message, null);
    }

    public SequenceDiagram step(String fromParticipant, SequenceStep.LifetimeEvent lifetimeEvent, String toParticipant, String message) {
        return step(fromParticipant, Arrow.SolidWithHead, lifetimeEvent, toParticipant, message, null);
    }

    public SequenceDiagram step(String fromParticipant, String toParticipant, String message) {
        return step(fromParticipant, Arrow.SolidWithHead, SequenceStep.LifetimeEvent.None, toParticipant, message, null);
    }

    public SequenceDiagram step(String fromParticipant, Arrow arrow, SequenceStep.LifetimeEvent lifetimeEvent, String toParticipant, String message) {
        return step(fromParticipant, arrow, lifetimeEvent, toParticipant, message, null);
    }

    public List<SequenceStep> steps() {
        return Collections.unmodifiableList(sequenceSteps);
    }

    public String title() {
        return title;
    }

    public SequenceDiagram title(String title) {
        this.title = title;
        return this;
    }
}
