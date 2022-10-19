package de.xam.featdoc.mermaid.flowchart;

import de.xam.featdoc.LineWriter;
import de.xam.featdoc.mermaid.sequence.MermaidDiagram;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.xam.featdoc.Util.add;

public class FlowchartDiagram implements MermaidDiagram {

    public enum Orientation {
        TD,
        /**
         * same as TD
         */
        TB, RL, LR, BT
    }

    public class Subgraph implements INode {
        private final String id;

        public Subgraph(String id) {
            this.id = id;
        }

        private List<Edge> edges = new ArrayList<>();

        public Subgraph edge(Node source, Node target) {
            edges.add(new Edge(source.id(), Edge.Arrow.standard(), null, target.id()));
            return this;
        }

        public FlowchartDiagram end() {
            return FlowchartDiagram.this;
        }

        public String id() {
            return id;
        }

        @Override
        public Optional<String> text() {
            return Optional.empty();
        }

        @Override
        public void toMermaidSyntax(LineWriter lineWriter) {
            lineWriter.writeLine("subgraph %s",id);
            for(Edge edge: edges)
                lineWriter.writeLine("%s%s", INDENT, edge.toMermaidSyntax() );
            lineWriter.writeLine("end");
        }
    }

    private static final String INDENT = "    ";

    private final String title;
    private final Orientation orientation;

    public FlowchartDiagram(String title,Orientation orientation) {
        this.title = title;
        this.orientation=orientation;
    }
    private Map<String, INode> nodeMap = new HashMap<>();
    private List<Edge> edges = new ArrayList<>();

    public FlowchartDiagram edge(Node source, Node target) {
        return edge(source.id(), target.id());
    }

    public FlowchartDiagram edge(String sourceId, String targetId) {
        add(edges, new Edge(sourceId, Edge.Arrow.standard(), null, targetId));
        return this;
    }

    public List<Edge> edges() {
        return edges;
    }

    public Node node(String id) {
        return node(id, null);
    }

    public Node node(String id, String label) {
        Node node = new Node(id, Node.Shape.Box, label);
        nodeMap.put(id, node);
        return node;
    }

    public Map<String,INode> nodeMap() {
        return nodeMap;
    }

    public Orientation orientation() {
        return orientation;
    }

    public Subgraph subgraph(String id) {
        Subgraph subgraph = new Subgraph(id);
        nodeMap.put(id,subgraph);
        return subgraph;
    }

    public String title() {
        return title;
    }
}
