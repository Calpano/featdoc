package de.xam.featdoc.mermaid;

import de.xam.featdoc.mermaid.flowchart.FlowchartDiagram;
import de.xam.featdoc.mermaid.sequence.SequenceDiagram;

public class MermaidDsl {
    public static SequenceDiagram sequence(String title) {
        return new SequenceDiagram(title);
    }
    public static FlowchartDiagram flowchart(String title) {
        return new FlowchartDiagram(title, FlowchartDiagram.Orientation.TD);
    }
}
