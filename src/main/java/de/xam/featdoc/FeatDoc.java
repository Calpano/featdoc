package de.xam.featdoc;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import de.xam.featdoc.markdown.IMarkdownCustomizer;
import de.xam.featdoc.markdown.StringTree;
import de.xam.featdoc.mermaid.MermaidTool;
import de.xam.featdoc.mermaid.flowchart.FlowchartDiagram;
import de.xam.featdoc.mermaid.sequence.MermaidDiagram;
import de.xam.featdoc.mermaid.sequence.SequenceDiagram;
import de.xam.featdoc.system.Message;
import de.xam.featdoc.system.Feature;
import de.xam.featdoc.system.Rule;
import de.xam.featdoc.system.Scenario;
import de.xam.featdoc.system.System;
import de.xam.featdoc.system.Universe;
import de.xam.featdoc.wiki.IWikiContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FeatDoc {

    record MarkdownWriter(IWikiContext wikiContext) {

        public void write(File f, Consumer<LineWriter> lineConsumerC, String footer) throws IOException {
                f.getParentFile().mkdirs();
                try (Writer w = new FileWriter(f)) {
                    LineWriter lineWriter = LineWriter.wrap(w);
                    wikiContext.markdownCustomizer().preamble().ifPresent(lineWriter::write);
                    lineConsumerC.accept(lineWriter);
                    lineWriter.writeLine(footer);
                    w.flush();
                }
            }
        }

    private FeatDoc() {
    }

    private static void debugPage(Universe universe, IWikiContext wikiContext, LineWriter lineWriter) {
        lineWriter.writeToc();

        // unused events?
        Set<Message> allMessages = universe.systems().stream().flatMap(system -> system.events().stream()).collect(Collectors.toSet());
        Set<Message> usedInRules = new HashSet<>();
        universe.systems().stream().flatMap(System::rules).forEach(rule -> {
            usedInRules.add(rule.trigger());
            usedInRules.addAll(rule.actions());
        });
        lineWriter.writeLine("# Events, not used in any rules");
        allMessages.removeAll(usedInRules);
        allMessages.forEach(event -> lineWriter.writeLine("* %s", wikiContext.wikiLink(event)));

        // rules, which never trigger?
        Set<Rule> allRules = universe.systems().stream().flatMap(System::rules).collect(Collectors.toSet());
        Set<Rule> usedInScenarios = new HashSet<>();
        universe.scenarios().stream().flatMap(scenario -> scenario.steps().stream()).forEach(step -> {
            // TODO ...
        });
        lineWriter.writeLine("# Rules, not used in any scenario");
        allRules.removeAll(usedInScenarios);
        allMessages.forEach(event -> lineWriter.writeLine("* %s", wikiContext.wikiLink(event)));
    }

    private static void eventsToMarkdown(Universe universe, System system, IWikiContext wikiContext, Predicate<Message> eventPredicate, LineWriter lineWriter) {
        system.events().stream().filter(eventPredicate).sorted(Comparator.comparing(Message::label)).forEach(event -> {
            lineWriter.writeLine("* **%s** [%s]%n", event.label(), timing(event,wikiContext));
            universe.featuresProducing(event).forEach(producingFeature -> lineWriter.writeLine(
                    "    * %s %s %s, %s %s/%s%n",
                    ARROW_RIGHT_LEFT_SOLID,
                    wikiContext.i18n(Term.callFrom),
                    wikiContext.wikiLink(producingFeature.system()),
                    wikiContext.i18n(Term.feature),
                    wikiContext.wikiLink(producingFeature.system()),
                    producingFeature.label()));
            universe.scenarioStepsProducing(event).forEach(producingScenarioStep -> lineWriter.writeLine(
                    "    * %s %s %s, %s %s%n",
                    ARROW_RIGHT_LEFT_SOLID,
                    wikiContext.i18n(Term.callFrom),
                    wikiContext.wikiLink(producingScenarioStep.source()),
                    wikiContext.i18n(Term.scenario),
                    wikiContext.wikiLink(producingScenarioStep.scenario())));
        });
    }

    private static String footer(I18n i18n) {
        String msg = String.format("""
                
                %s
                """,i18n.resolve(Term.footer) );
        if(!msg.contains("%s"))
            throw new IllegalArgumentException();
        return String.format(msg,  Instant.now());
    }

    public static void generateMarkdownFiles(Universe universe, IWikiContext wikiContext) throws IOException {
        MarkdownWriter markdownWriter = new MarkdownWriter(wikiContext);
        for (Scenario scenario : universe.scenarios()) {
            markdownWriter.write(wikiContext.markdownFile(scenario), lineWriter -> scenarioPage(universe, scenario, wikiContext, lineWriter), footer(wikiContext.i18n()) );
        }
        // markdown files for each system, with features, rules, events
        for (System system : universe.systems()) {
            markdownWriter.write(wikiContext.markdownFile(system), lineWriter -> systemPage(universe, system, wikiContext, lineWriter), footer(wikiContext.i18n()));
        }
        // markdown index file
        markdownWriter.write(new File(wikiContext.rootDir(),
                (wikiContext.rootPath().isEmpty()? "Index" : wikiContext.rootPath()) + ".md"), lineWriter -> indexPage(universe, wikiContext, lineWriter), footer(wikiContext.i18n()));
        // markdown debug file
        markdownWriter.write(new File(wikiContext.rootDir(), wikiContext.rootPath() + "/DebugInfo.md"), lineWriter -> debugPage(universe, wikiContext, lineWriter), footer(wikiContext.i18n()));
    }

    private static void indexPage(Universe universe, IWikiContext wikiContext, LineWriter lineWriter) {
        lineWriter.writeToc();
        lineWriter.writeSection1(wikiContext.i18n(Term.scenarios) );
        for (Scenario scenario : universe.scenarios()) {
            lineWriter.writeLine("* %s", wikiContext.wikiLink(scenario));
        }
        lineWriter.writeSection1(wikiContext.i18n(Term.systems));
        for (System system : universe.systems()) {
            lineWriter.writeLine("* %s", wikiContext.wikiLink(system));
        }

        // system dependency map
        TreeMultimap<System, System> multiMap = TreeMultimap.create();
        universe.forEachEdge(multiMap::put);
        FlowchartDiagram flowchartDiagram = toMermaidFlowchart(
                wikiContext.i18n(Term.overviewCallsFromSystems)
                , multiMap, system -> system.wikiName, System::label, false);
        mermaidDiagramBlock(flowchartDiagram, wikiContext.markdownCustomizer(), lineWriter);
    }

    public static void mermaidDiagramBlock(MermaidDiagram mermaidDiagram, IMarkdownCustomizer markdownCustomizer, LineWriter lineWriter) {
        lineWriter.writeLine("");
        lineWriter.writeLine(switch (markdownCustomizer.mermaidBlockSyle()) {
            case Default -> "```mermaid";
            case Microsoft -> ":::mermaid";
        });
        MermaidTool.generateMermaidSyntax(mermaidDiagram, lineWriter);
        lineWriter.writeLine(switch (markdownCustomizer.mermaidBlockSyle()) {
            case Default -> "```";
            case Microsoft -> ":::";
        });
        lineWriter.writeLine("");
    }

    public static void scenarioPage(Universe universe, Scenario scenario, IWikiContext wikiContext, LineWriter lineWriter) {
        SequenceDiagram sequenceDiagram = universe.toSequence(scenario);
        lineWriter.writeSection1( "%s: %s",wikiContext.i18n(Term.scenario), sequenceDiagram.title());
        lineWriter.writeToc();

        lineWriter.writeSection( wikiContext.i18n(Term.sequenceDiagram));
        mermaidDiagramBlock(sequenceDiagram, wikiContext.markdownCustomizer(), lineWriter);

        lineWriter.writeSection(wikiContext.i18n(Term.scenarioSteps));
        List<Universe.ResultStep> resultingSteps = universe.computeResultingSteps(scenario);
        for (Universe.ResultStep rs : resultingSteps) {
            if (rs.isScenario()) {
                // initial cause from scenario
                lineWriter.writeLine("* %s --%s--> %s: **%s**",
                        wikiContext.wikiLink(rs.source()),
                        timing(rs.action(),wikiContext),
                        wikiContext.wikiLink(rs.target()),
                        rs.action().label());
            } else {
                // resulting cascade
                lineWriter.writeLine("    * %s --%s--> %s: **%s** (%s/%s)",
                        wikiContext.wikiLink(rs.source()),
                        timing(rs.action(),wikiContext),
                        wikiContext.wikiLink(rs.target()),
                        rs.action().label(),
                        wikiContext.wikiLink(rs.feature().system()),
                        rs.feature().label());
            }
        }

        lineWriter.writeSection(wikiContext.i18n(Term.scenarioTree));
        lineWriter.write("**%s**: ",wikiContext.i18n(Term.legend));
        lineWriter.write("(%s) %s",ARROW_LEFT_RIGHT_SOLID, wikiContext.i18n(Term.synchronous));
        lineWriter.write(" | ");
        lineWriter.write("(%s) %s",ARROW_LEFT_RIGHT_DASHED, wikiContext.i18n(Term.asynchronous));
        lineWriter.writeLine("");
        lineWriter.writeLine("");
        List<StringTree> trees = universe.toTrees(scenario, (rs) -> String.format("%s %s %s: **%s** (%s)", wikiContext.wikiLink(rs.source()),
                rs.action().isAsynchronous() ? ARROW_LEFT_RIGHT_DASHED : ARROW_LEFT_RIGHT_SOLID,
                wikiContext.wikiLink(rs.target()),
                rs.action().label(),
                rs.feature() == null ? wikiContext.i18n(Term.scenario) : wikiContext.wikiLink(rs.feature().system()) + "/" + rs.feature().label()
        ));
        StringTree.toMarkdownList(trees,lineWriter);
    }

    static final String ARROW_RIGHT_LEFT_SOLID = "\u2B05";
    static final String ARROW_LEFT_RIGHT_SOLID = "\u2B95";

    static final String ARROW_RIGHT_LEFT_DASHED = "\u21E6";
    static final String ARROW_LEFT_RIGHT_DASHED = "\u21E8";


    static final String ARROW_LEFT_RIGHT_TINYWAVY = "\u27FF";
    static final String ARROW_LEFT_RIGHT_WAVY = "\u219D";
    static final String ARROW_LEFT_RIGHT_RULE = "\u21A6";




    public static void systemPage(Universe universe, System system, IWikiContext wikiContext, LineWriter lineWriter) {
        lineWriter.writeLine("# %s: %s", wikiContext.i18n(Term.system),  system.label());
        lineWriter.writeToc();
        lineWriter.writeLine("## %s", wikiContext.i18n(Term.features));
        for (Feature feature : system.features()) {
            lineWriter.writeLine("""
                                                
                    ### %s: %s""", wikiContext.i18n(Term.feature), feature.label(), feature.localTarget());
            for (Rule rule : feature.rules()) {
                lineWriter.writeLine("""

                        * %s: %s **%s** in %s [%s]""",
                        wikiContext.i18n(Term.rule),
                        wikiContext.i18n(Term.if_),
                        rule.trigger().label(),
                        rule.trigger().system().label(),
                        timing(rule.trigger(),wikiContext));
                for (Message action : rule.actions()) {
                    lineWriter.writeLine("    * %s %s **%s** in %s [%s]",
                            ARROW_LEFT_RIGHT_RULE,
                            wikiContext.i18n(Term.then_),
                            action.label(),
                            wikiContext.wikiLink(action.system()),
                            timing(action,wikiContext)
                    );
                }
                if (rule.comment() != null) {
                    lineWriter.writeLine("    * NOTE: *%s*", rule.comment());
                }
            }
        }
        lineWriter.writeSection(wikiContext.i18n(Term.incomingApiCalls));
        eventsToMarkdown(universe, system, wikiContext, Message::isSynchronous, lineWriter);

        lineWriter.writeSection(wikiContext.i18n(Term.incomingAsyncEvents));
        eventsToMarkdown(universe, system, wikiContext, Message::isAsynchronous, lineWriter);

        lineWriter.writeSection(wikiContext.i18n(Term.systemLandscape));
        lineWriter.writeLine("* %s: %s",
                wikiContext.i18n(Term.callsFrom),
                universe.systemsCalling(system).map(wikiContext::wikiLink).collect(Collectors.joining(", ")));
        lineWriter.writeLine("* %s: %s",
                wikiContext.i18n(Term.callsTo),
                universe.systemsCalledFrom(system).map(wikiContext::wikiLink).collect(Collectors.joining(", ")));

        // system dependency map
        TreeMultimap<System, System> multiMap = TreeMultimap.create();
        universe.systemsCalling(system).forEach(source -> multiMap.put(source, system));
        universe.systemsCalledFrom(system).forEach(target -> multiMap.put(system, target));
        FlowchartDiagram flowchartDiagram = toMermaidFlowchart(wikiContext.i18n(Term.systemsOverview), multiMap, s -> s.wikiName, System::label, false);
        mermaidDiagramBlock(flowchartDiagram, wikiContext.markdownCustomizer(), lineWriter);
    }

    private static String timing(Message message, IWikiContext wikiContext) {
        return message.isSynchronous() ? wikiContext.i18n(Term.synchronous) : wikiContext.i18n(Term.asynchronous);
    }

    public static <T> FlowchartDiagram toMermaidFlowchart(String title, Multimap<T, T> multimap, Function<T, String> idFun, Function<T, String> labelFun, boolean allowSelfLinks) {
        FlowchartDiagram flowchartDiagram = new FlowchartDiagram(title, FlowchartDiagram.Orientation.TD);
        multimap.keySet().forEach(node -> flowchartDiagram.node(idFun.apply(node), labelFun.apply(node)));
        multimap.forEach((s, t) -> {
            if (allowSelfLinks || s != t) {
                flowchartDiagram.edge(idFun.apply(s), idFun.apply(t));
            }
        });
        return flowchartDiagram;
    }

}
