package de.xam.featdoc.system;

import de.xam.featdoc.I18n;
import de.xam.featdoc.Term;
import de.xam.featdoc.wiki.IWikiLink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static de.xam.featdoc.Util.add;

/**
 * Systems are compared by sortOrder and label.
 */
public class System implements IWikiLink, Comparable<System>, SystemApi {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof System system)) return false;
        return sortOrder == system.sortOrder && Objects.equals(label, system.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, sortOrder);
    }

    public final String wikiName;
    final String id;
    final String label;

    public int sortOrder() {
        return sortOrder;
    }

    final int sortOrder;
    final List<Feature> featureList = new ArrayList<>();
    private final List<Message> messageList = new ArrayList<>();

    /***
     *
     * @param mermaidDiagramId to be used in generated Mermaid sequence diagrams
     * @param name pretty name with Umlauts and all
     * @param wikiName suitable for a wiki page name / link target
     * @param sortOrder first sort criterion; name is used as second
     */
    public System(String mermaidDiagramId, String name, String wikiName, int sortOrder) {
        this.id = mermaidDiagramId;
        this.label = name;
        this.wikiName = wikiName;
        this.sortOrder = sortOrder;
    }


    @Override
    public Message step(Message.Direction direction, Timing timing, String name) {
        return add(messageList, new Message(this, direction, timing,name));
    }


    @Override
    public int compareTo(@NotNull System o) {
        return comparator().compare(this, o);
    }

    public static Comparator<System> comparator() {
        return Comparator.comparing(System::sortOrder).thenComparing(System::label);
    }


    public List<Message> events() {
        return Collections.unmodifiableList(messageList);
    }

    public Feature feature( String name) {
        Feature feature = new Feature(this, name);
        featureList.add(feature);
        return feature;
    }

    public List<Feature> features() {
        return Collections.unmodifiableList(featureList);
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


    public Stream<Message> producedEvents() {
        return featureList.stream().flatMap(feature -> feature.producedEvents()).distinct();
    }

    public String localTarget() {
        return wikiName;
    }

}
