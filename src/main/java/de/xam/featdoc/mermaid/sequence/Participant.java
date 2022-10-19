package de.xam.featdoc.mermaid.sequence;

import java.util.ArrayList;
import java.util.List;

public class Participant {

    public final boolean isActor;
    public final String id;
    public final String label;

    private final List<Link> link = new ArrayList<>();

    private Participant(boolean isActor, String id, String label) {
        this.isActor = isActor;
        this.id = id;
        this.label = label;
    }

    public static Participant participant(String id) {
        return participant(id,null );
    }
    public static Participant actor(String id) {
        return actor(id,null );
    }

    public String id() {
        return id;
    }

    public String mermaid() {
        return (isActor?"actor":"participant")+" "+id+(label==null?"":" as "+label);
    }

    public static Participant participant( String id, String label ) {
        return new Participant(false, id, label);
    }
    public static Participant actor( String id, String label ) {
        return new Participant(true, id, label);
    }

}
