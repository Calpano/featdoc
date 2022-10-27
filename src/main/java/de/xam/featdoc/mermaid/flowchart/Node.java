package de.xam.featdoc.mermaid.flowchart;

import de.xam.featdoc.LineWriter;

import javax.annotation.Nullable;
import java.util.Optional;

public record Node(String id, Shape shape, @Nullable String label) implements INode {

    enum Shape {
        Box("[", "]"), Rounded("(", ")"), Pill("([", "])"), Subroutine("[[", "]]"), Cylinder("[(", ")]"), Circle("((", "))"), Asym(">", "]"), Rhombus("{", "}"), Hexagon("{{", "}}"), Parallelogram("[/", "/]"), ParallelogramAlt("[\\", "\\]"), Trapezoid("[/", "\\]"), TrapezoidAlt("[\\", "/]"), DoubleCircle("(((", ")))");

        private final String mermaid[];

        Shape(String... mermaid) {
            this.mermaid = mermaid;
        }

        String mermaidEnd() {
            return mermaid[1];
        }

        String mermaidStart() {
            return mermaid[0];
        }
    }

    @Override
    public Optional<String> text() {
        return Optional.ofNullable(label);
    }

    public void toMermaidSyntax(LineWriter lineWriter) {
        if (text().isPresent()) {
            lineWriter.writeLine(id() + shape().mermaidStart() + "\"" + text().get() + "\"" + shape().mermaidEnd());
        }
    }

}
