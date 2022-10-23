package de.xam.featdoc.system;

import de.xam.featdoc.I18n;
import de.xam.featdoc.Util;
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

    public boolean isProducing(Message message) {
        return producedEvents().anyMatch(message::equals);
    }


    @Override
    public @Nullable String wikiFolder(I18n i18n) {
        return system.wikiFolder(i18n);
    }




    public String label() {
        return label;
    }

    public Stream<Message> producedEvents() {
        return rules.stream().flatMap(Rule::producedEvents);
    }

    public Feature rule(Message trigger, Message... actions) {
        return rule(trigger,null,actions);
    }

    public Feature rule(Message trigger, String triggerComment, Message... actions) {
        if (actions == null || actions.length == 0)
            throw new IllegalArgumentException("No actions given");
        Util.add(rules, rule().trigger(trigger,triggerComment).actions(actions).build());
        return this;
    }

    public Rule.RuleBuilder rule() {
        return Rule.builder(this);
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
