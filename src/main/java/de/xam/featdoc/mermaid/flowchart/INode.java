package de.xam.featdoc.mermaid.flowchart;

import de.xam.featdoc.LineWriter;

import java.util.Optional;

public interface INode {
    String id();

    Optional<String> text();

    void toMermaidSyntax(LineWriter lineWriter);
}
