package de.xam.featdoc.mermaid;

import de.xam.featdoc.LineWriter;
import de.xam.featdoc.mermaid.flowchart.FlowchartDiagram;
import de.xam.featdoc.mermaid.sequence.MermaidDiagram;
import de.xam.featdoc.mermaid.sequence.Note;
import de.xam.featdoc.mermaid.sequence.Participant;
import de.xam.featdoc.mermaid.sequence.SequenceDiagram;
import de.xam.featdoc.mermaid.sequence.SequenceStep;

public class MermaidTool {
    private static final String INDENT = "    ";

    private MermaidTool() {
    }

    public static void generateMermaidSyntax(MermaidDiagram mermaidDiagram, LineWriter lineWriter) {
        if (mermaidDiagram instanceof  SequenceDiagram sd) {
            generateMermaidSyntax(sd, lineWriter);
        } else if (mermaidDiagram instanceof FlowchartDiagram fd) {
            generateMermaidSyntax(fd, lineWriter);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static void generateMermaidSyntax(SequenceDiagram sequenceDiagram, LineWriter lineWriter) {
        lineWriter.writeLine("sequenceDiagram\n");
        for (Participant participant : sequenceDiagram.participants()) {
            lineWriter.writeLine(INDENT + participant.mermaid());
        }
        for (Note note : sequenceDiagram.notes()) {
            lineWriter.writeLine(INDENT + note.mermaid());
        }
        for (SequenceStep sequenceStep : sequenceDiagram.steps()) {
            lineWriter.writeLine(INDENT + sequenceStep.mermaid());
        }
    }


    public static void generateMermaidSyntax(FlowchartDiagram flowchartDiagram, LineWriter lineWriter) {
        // Azure wiki does not understand "flowchart"
        lineWriter.writeLine("graph %s", flowchartDiagram.orientation().name());
        flowchartDiagram.nodeMap().forEach((id, node) -> node.toMermaidSyntax(lineWriter));
        flowchartDiagram.edges().forEach(edge -> {
            lineWriter.writeLine("%s%s --> %s", INDENT, edge.source(), edge.target());
        });
    }
}
