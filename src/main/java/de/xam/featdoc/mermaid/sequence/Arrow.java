package de.xam.featdoc.mermaid.sequence;

public enum Arrow {

    /**
     * Solid line without arrow
     */
    Solid("->"),
    /**
     * Dotted line without arrow
     */
    Dotted("-->"),
    /**
     * Solid line with arrowhead
     */
    SolidWithHead("->>"),
    /**
     * Dotted line with arrowhead
     */
    DottedWithHead("-->"),
    /**
     * Solid line with a cross at the end
     */
    SolidWithCross("-x"),
    /**
     * Dotted line with a cross at the end.
     */
    DottedWithCross("--x"),
    /**
     * Solid line with an open arrow at the end (async)
     */
    SolidAsync("-)"),
    /**
     * Dotted line with a open arrow at the end (async)
     */
    DottedAsync("--)");

    public final String mermaid;

    Arrow(String mermaid) {
        this.mermaid = mermaid;
    }

    public String mermaid() {
        return mermaid;
    }

}
