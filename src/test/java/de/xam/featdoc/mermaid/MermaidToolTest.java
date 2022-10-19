package de.xam.featdoc.mermaid;

import de.xam.featdoc.LineWriter;
import de.xam.featdoc.mermaid.flowchart.FlowchartDiagram;
import de.xam.featdoc.mermaid.flowchart.Node;
import de.xam.featdoc.mermaid.sequence.SequenceDiagram;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import static de.xam.featdoc.mermaid.MermaidDsl.flowchart;
import static de.xam.featdoc.mermaid.MermaidDsl.sequence;
import static de.xam.featdoc.mermaid.sequence.Arrow.DottedWithHead;
import static de.xam.featdoc.mermaid.sequence.Arrow.SolidWithHead;
import static de.xam.featdoc.mermaid.sequence.Note.Position.RightOf;
import static de.xam.featdoc.mermaid.sequence.SequenceStep.LifetimeEvent.Activate;
import static de.xam.featdoc.mermaid.sequence.SequenceStep.LifetimeEvent.Deactivate;

class MermaidToolTest {


    /**
     * <pre>
     *     flowchart TB
     *     c1-->a2
     *     subgraph one
     *     a1-->a2
     *     end
     *     subgraph two
     *     b1-->b2
     *     end
     *     subgraph three
     *     c1-->c2
     *     end
     *     one --> two
     *     three --> two
     *     two --> c2
     * </pre>
     */
    @Test
    void testFlowchart() throws IOException {
        FlowchartDiagram flowchartDiagram = flowchart("a,b,c");
        Node a1 = flowchartDiagram.node("a1");
        Node a2 = flowchartDiagram.node("a2");
        Node b1 = flowchartDiagram.node("b1");
        Node b2 = flowchartDiagram.node("b2");
        Node c1 = flowchartDiagram.node("c1");
        Node c2 = flowchartDiagram.node("c2");
        flowchartDiagram
                .edge(c1, a2)
                .subgraph("one")
                .edge(a1, a2)
                .end()
                .subgraph("one")
                .edge(a1, a2)
                .end()
                .subgraph("two")
                .edge(b1, b2)
                .end()
                .subgraph("three")
                .edge(c1, c2)
                .end()
                .edge("one", "two")
                .edge("three", "two")
                .edge("two", "c2")
        ;

        Writer w = new PrintWriter(System.out);
        MermaidTool.generateMermaidSyntax(flowchartDiagram, LineWriter.wrap(w));
        w.flush();
        System.out.flush();


    }

    @Test
    void testSequenceDiagram1() throws IOException {
        SequenceDiagram sequenceDiagram = sequence("Alice calls John").participant("Alice").participant("John")//
                .note(RightOf, "Alice", "Alice calls John.")//
                .step("Alice", SolidWithHead, Activate, "John", "Hello John, how are you?")//
                .step("Alice", SolidWithHead, Activate, "John", "John, can you hear me?")//
                .step("John", DottedWithHead, Deactivate, "Alice", "Hi Alice, I can hear you!")//
                .step("John", DottedWithHead, Deactivate, "Alice", "I feel great!")//
                .step("Alice", SolidWithHead, Activate, "John", "Did you want to go to the game tonight?")//
                .step("John", DottedWithHead, Deactivate, "Alice", "Yeah! See you there.");
        try (Writer w = new PrintWriter(System.out)) {
            MermaidTool.generateMermaidSyntax(sequenceDiagram, LineWriter.wrap(w));
            w.flush();
        }
    }

}