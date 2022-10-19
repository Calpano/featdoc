package de.xam.featdoc.system;

import de.xam.featdoc.wiki.IWikiLink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class System implements IWikiLink, Comparable<System> {

    public static final String WIKI_FOLDER = "System";
    public final String wikiName;
    final String id;
    final String label;
    final List<Feature> features = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();

    /***
     *
     * @param id to be used in generated Mermaid sequence diagrams
     * @param label ..
     * @param wikiName suitable for a wiki page name / link target
     */
    public System(String id, String label, String wikiName) {
        this.id = id;
        this.label = label;
        this.wikiName = wikiName;
    }

    public Event apiCall(String apiCallName) {
        Event event = new Event(this, Timing.Synchronous, apiCallName);
        events.add(event);
        return event;
    }

    @Override
    public int compareTo(@NotNull System o) {
        return this.label.compareTo(o.label);
    }

    public Event eventAsync(String eventName) {
        Event event = new Event(this, Timing.Asynchronous, eventName);
        events.add(event);
        return event;
    }

    public List<Event> events() {
        return Collections.unmodifiableList(events);
    }

    public Feature feature( String name) {
        Feature feature = new Feature(this, name);
        features.add(feature);
        return feature;
    }

    public List<Feature> features() {
        return Collections.unmodifiableList(features);
    }

    public boolean isProducing(Event event) {
        return producedEvents().filter(e -> e.equals(event)).findAny().isPresent();
    }

    @Override
    public String label() {
        return label;
    }

    public Stream<Rule> rules() {
        return features().stream().flatMap( f -> f.rules().stream());
    }

    @Override
    public @Nullable String wikiFolder() {
        return WIKI_FOLDER;
    }

    public String toString() {
        return "[" + label + "]";
    }

    public Event uiAction(String label) {
        Event event = new Event(this, Timing.Synchronous, label);
        events.add(event);
        return event;
    }

    public Stream<Event> producedEvents() {
        return features.stream().flatMap(feature -> feature.producedEvents()).distinct();
    }

    public String localTarget() {
        return wikiName;
    }

}
