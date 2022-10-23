package de.xam.featdoc.system;

import de.xam.featdoc.I18n;
import de.xam.featdoc.Term;
import de.xam.featdoc.wiki.IWikiLink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class System implements IWikiLink, Comparable<System> {

    public final String wikiName;
    final String id;
    final String label;
    final List<Feature> features = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();

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

    public Message apiCall(String apiCallName) {
        Message message = new Message(this, Timing.Synchronous, apiCallName);
        messages.add(message);
        return message;
    }

    @Override
    public int compareTo(@NotNull System o) {
        return this.label.compareTo(o.label);
    }

    public Message eventAsync(String eventName) {
        Message message = new Message(this, Timing.Asynchronous, eventName);
        messages.add(message);
        return message;
    }

    public List<Message> events() {
        return Collections.unmodifiableList(messages);
    }

    public Feature feature( String name) {
        Feature feature = new Feature(this, name);
        features.add(feature);
        return feature;
    }

    public List<Feature> features() {
        return Collections.unmodifiableList(features);
    }

    public boolean isProducing(Message message) {
        return producedEvents().filter(e -> e.equals(message)).findAny().isPresent();
    }

    @Override
    public String label() {
        return label;
    }

    public Stream<Rule> rules() {
        return features().stream().flatMap( f -> f.rules().stream());
    }

    @Override
    public @Nullable String wikiFolder(I18n i18n) {
        return i18n.resolve(Term.system);
    }

    public String toString() {
        return "[" + label + "]";
    }

    public Message uiAction(String label) {
        Message message = new Message(this, Timing.Synchronous, label);
        messages.add(message);
        return message;
    }

    public Stream<Message> producedEvents() {
        return features.stream().flatMap(feature -> feature.producedEvents()).distinct();
    }

    public String localTarget() {
        return wikiName;
    }

}
