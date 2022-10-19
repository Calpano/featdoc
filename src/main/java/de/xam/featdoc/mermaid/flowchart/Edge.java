package de.xam.featdoc.mermaid.flowchart;

import de.xam.featdoc.mermaid.sequence.Arrow;
import org.jetbrains.annotations.Nullable;

public record Edge(String source, Arrow arrow, @Nullable String label, String target) {

    public String toMermaidSyntax() {
        return source()+" "+arrow.toMermaidSyntax()+(label==null?"":"|\""+label+"\"|")+" "+target();
    }

    public enum Start {
        Arrow("<"),
        Circle("o"),
        Cross("x"),
        None("");

        Start(String mermaid) {
            this.mermaid = mermaid;
        }
        final String mermaid;
    }

    public enum Line {
        Normal("-"),
        Thick("="),
        Dotted(".");

        Line(String mermaid) {
            this.mermaid = mermaid;
        }
        final String mermaid;
    }

    public enum End {
        Arrow(">"),
        Circle("o"),
        Cross("x"),
        None("-");

        End(String mermaid) {
            this.mermaid = mermaid;
        }
        final String mermaid;
    }

    /**
     *
     * @param start
     * @param line
     * @param length 1 = default minimum
     * @param end
     */
    public record Arrow(Start start, Line line, int length, End end) {

        public static Arrow standard() {
            return new Arrow(Start.None,Line.Normal,1, End.Arrow);
        }

        public String toMermaidSyntax() {
            int len = start.mermaid.length()+end.mermaid.length();
            String s = "";
            for( int i=len; len < length+2; len++) {
                s+= line.mermaid;
            }
            // special case as defined in https://mermaid-js.github.io/mermaid/#/flowchart?id=links-between-nodes
            s = start.mermaid + s + (line==Line.Dotted && end==End.Arrow? "->": end.mermaid);
            return s;
        }

    }

}
