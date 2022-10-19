package de.xam.featdoc.system;

import de.xam.featdoc.Util;
import de.xam.featdoc.markdown.MarkdownTool;
import de.xam.featdoc.wiki.IWikiContext;
import de.xam.featdoc.wiki.IWikiLink;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Feature implements IWikiLink {

    final String label;
    final List<Rule> rules = new ArrayList<>();
    private final System system;

    public Feature(System system, String label) {
        this.label = label;
        this.system = system;
    }

    public boolean isProducing(Event event) {
        return producedEvents().anyMatch(event::equals);
    }


    @Override
    public @Nullable String wikiFolder() {
        return system.wikiFolder();
    }




    public String label() {
        return label;
    }

    public Stream<Event> producedEvents() {
        return rules.stream().flatMap(Rule::producedEvents);
    }

    public Feature rule(Event trigger, Event... actions) {
        Util.add(rules, new Rule(trigger, actions));
        return this;
    }

    public Feature rule(Event trigger, String comment, Event... actions) {
        Util.add(rules, new Rule(trigger, actions).comment(comment));
        return this;
    }

    public List<Rule> rules() {
        return Collections.unmodifiableList(rules);
    }

    public System system() {
        return system;
    }

    public String toString() {
        return label;
    }

}