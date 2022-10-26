package de.xam.featdoc.system;

import de.xam.featdoc.I18n;
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

    public String label() {
        return label;
    }

    public Stream<Message> producedEvents() {
        return rules.stream().flatMap(Rule::producedEvents);
    }

    public Feature rule(Message trigger, Message action1, Message... moreActions) {
        Rule.RuleWithTriggerBuilder builder = rule().feature(this).trigger(trigger);
        builder.action(action1);
        if(moreActions!=null) {
            Stream.of(moreActions).forEach(action -> builder.action(action));
        }
        return builder.build();
    }

    public Rule.RuleWithTriggerBuilder rule(Message trigger, String comment) {
        return rule().trigger(trigger, comment);
    }

    public Rule.RuleWithTriggerBuilder rule(Message trigger) {
        return rule(trigger,null);
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

    @Override
    public @Nullable String wikiFolder(I18n i18n) {
        return system.wikiFolder(i18n);
    }

}
