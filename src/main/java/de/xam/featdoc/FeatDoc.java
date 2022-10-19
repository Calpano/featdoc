package de.xam.featdoc;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import de.xam.featdoc.markdown.IMarkdownCustomizer;
import de.xam.featdoc.mermaid.MermaidTool;
import de.xam.featdoc.mermaid.flowchart.FlowchartDiagram;
import de.xam.featdoc.mermaid.sequence.MermaidDiagram;
import de.xam.featdoc.mermaid.sequence.SequenceDiagram;
import de.xam.featdoc.mermaid.sequence.SequenceStep;
import de.xam.featdoc.system.Event;
import de.xam.featdoc.system.Feature;
import de.xam.featdoc.system.Rule;
import de.xam.featdoc.system.Scenario;
import de.xam.featdoc.system.System;
import de.xam.featdoc.system.Universe;
import de.xam.featdoc.wiki.IWikiContext;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class FeatDoc {

    static class MarkdownWriter {
        final IWikiContext wikiContext;

        public MarkdownWriter(IWikiContext wikiContext) {
            this.wikiContext = wikiContext;
        }

        public void write(File f, Consumer<LineWriter> lineConsumerC) throws IOException {
            f.getParentFile().mkdirs();
            try (Writer w = new FileWriter(f)) {
                wikiContext.markdownCustomizer().preamble().ifPresent(preamble -> {
                    try {
                        w.write(preamble);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                lineConsumerC.accept(LineWriter.wrap(w));
                w.write("\n");
                w.write(String.format("""
                        Erzeugt am %s von **featdoc**
                        """, Instant.now()));
                w.flush();
            }
        }
    }

    private static final Logger log = getLogger(FeatDoc.class);

    private FeatDoc() {
    }

    private static void debugPage(Universe universe, IWikiContext wikiContext, LineWriter lineWriter) {
        lineWriter.writeToc();

        // unused events?
        Set<Event> allEvents = universe.systems().stream().flatMap(system -> system.events().stream()).collect(Collectors.toSet());
        Set<Event> usedInRules = new HashSet<>();
        universe.systems().stream().flatMap(System::rules).forEach(rule -> {
            usedInRules.add(rule.trigger());
            usedInRules.addAll(rule.actions());
        });
        lineWriter.writeLine("# Events, die nicht in Regeln verwendet werden");
        allEvents.removeAll(usedInRules);
        allEvents.forEach(event -> lineWriter.writeLine("* %s", wikiContext.wikiLink(event)));

        // rules, which never trigger?
        Set<Rule> allRules = universe.systems().stream().flatMap(System::rules).collect(Collectors.toSet());
        Set<Rule> usedInScenarios = new HashSet<>();
        universe.scenarios().stream().flatMap(scenario -> scenario.steps().stream()).forEach(step -> {
            // TODO ...
        });
        lineWriter.writeLine("# Regeln, die von keinem Szenario verwendet werden");
        allRules.removeAll(usedInScenarios);
        allEvents.forEach(event -> lineWriter.writeLine("* %s", wikiContext.wikiLink(event)));
    }

    private static void eventsToMarkdown(Universe universe, System system, IWikiContext wikiContext, Predicate<Event> eventPredicate, LineWriter lineWriter) {
        system.events().stream().filter(eventPredicate).sorted((a, b) -> a.label().compareTo(b.label())).forEach(event -> {
            lineWriter.writeLine("* **%s** [%s]%n", event.label(), timing(event));
            universe.featuresProducing(event).forEach(producingFeature -> {
                lineWriter.writeLine("    * <= Aufruf von %s, Feature %s/%s%n", //
                        wikiContext.wikiLink(producingFeature.system()), wikiContext.wikiLink(producingFeature.system()), producingFeature.label());
            });
            universe.scenarioStepsProducing(event).forEach(producingScenarioStep -> {
                lineWriter.writeLine("    * <= Aufruf von %s, Szenario %s%n", wikiContext.wikiLink(producingScenarioStep.source()), wikiContext.wikiLink(producingScenarioStep.scenario()));
            });
        });
    }

    public static void generateMarkdownFiles(Universe universe, IWikiContext wikiContext) throws IOException {
        MarkdownWriter markdownWriter = new MarkdownWriter(wikiContext);
        for (Scenario scenario : universe.scenarios()) {
            markdownWriter.write(wikiContext.markdownFile(scenario), lineWriter -> scenarioPage(universe, scenario, wikiContext, lineWriter));
        }
        // markdown files for each system, with features, rules, events
        for (System system : universe.systems()) {
            markdownWriter.write(wikiContext.markdownFile(system), lineWriter -> systemPage(universe, system, wikiContext, lineWriter));
        }
        // markdown index file
        markdownWriter.write(new File(wikiContext.rootDir(), wikiContext.rootPath() + ".md"), lineWriter -> indexPage(universe, wikiContext, lineWriter));
        // markdown debug file
        markdownWriter.write(new File(wikiContext.rootDir(), wikiContext.rootPath() + "/DebugInfo.md"), lineWriter -> debugPage(universe, wikiContext, lineWriter));
    }

    private static void indexPage(Universe universe, IWikiContext wikiContext, LineWriter lineWriter) {
        lineWriter.writeToc();
        lineWriter.writeSection1("Szenarien");
        for (Scenario scenario : universe.scenarios()) {
            lineWriter.writeLine("* %s", wikiContext.wikiLink(scenario));
        }
        lineWriter.writeSection1("Systeme");
        for (System system : universe.systems()) {
            lineWriter.writeLine("* %s", wikiContext.wikiLink(system));
        }

        // system dependency map
        TreeMultimap<System, System> multiMap = TreeMultimap.create();
        universe.forEachEdge(multiMap::put);
        FlowchartDiagram flowchartDiagram = toMermaidFlowchart("Übersicht Aufrufe von Systemen", multiMap, system -> system.wikiName, System::label, false);
        mermaidDiagramBlock(flowchartDiagram, wikiContext.markdownCustomizer(), lineWriter);
    }

    public static void mermaidDiagramBlock(MermaidDiagram mermaidDiagram, IMarkdownCustomizer markdownCustomizer, LineWriter lineWriter) {
        lineWriter.writeLine(switch (markdownCustomizer.mermaidBlockSyle()) {
            case Default -> "```mermaid";
            case Microsoft -> ":::mermaid";
        });
        MermaidTool.generateMermaidSyntax(mermaidDiagram, lineWriter);
        lineWriter.writeLine(switch (markdownCustomizer.mermaidBlockSyle()) {
            case Default -> "```";
            case Microsoft -> ":::";
        });
    }

    public static void scenarioPage(Universe universe, Scenario scenario, IWikiContext wikiContext, LineWriter lineWriter) {
        SequenceDiagram sequenceDiagram = universe.toSequence(scenario);
        lineWriter.writeSection1("Szenario: %s", sequenceDiagram.title());
        lineWriter.writeToc();

        lineWriter.writeSection("Sequenz-Diagramm");
        mermaidDiagramBlock(sequenceDiagram, wikiContext.markdownCustomizer(), lineWriter);

        lineWriter.writeSection("Szenario-Schritte");
        List<Universe.ResultStep> resultingSteps = universe.computeResultingSteps(scenario);
        for (Universe.ResultStep rs : resultingSteps) {
            if (rs.isScenario()) {
                // initial cause from scenario
                lineWriter.writeLine("* %s --%s--> %s: **%s**", wikiContext.wikiLink(rs.source()), timing(rs.action()), wikiContext.wikiLink(rs.target()), rs.action().label());
            } else {
                // resulting cascade
                lineWriter.writeLine("    * %s --%s--> %s: **%s** (%s/%s)", wikiContext.wikiLink(rs.source()), timing(rs.action()), wikiContext.wikiLink(rs.target()), rs.action().label(), wikiContext.wikiLink(rs.feature().system()), rs.feature().label());
            }
        }

        lineWriter.writeSection("Sequenz-Schritte");
        lineWriter.writeLine("Die gleichen Daten wie im Diagramm, hier aber ggf. besser lesbar");
        for (SequenceStep sequenceStep : sequenceDiagram.steps()) {
            lineWriter.writeLine("1. **%s** %s **%s**: %s", sequenceStep.from().toUpperCase(), sequenceStep.arrow().mermaid(), sequenceStep.to().toUpperCase(), sequenceStep.message());
        }
    }

    public static void systemPage(Universe universe, System system, IWikiContext wikiContext, LineWriter lineWriter) {
        lineWriter.writeLine("# System: %s", system.label());
        lineWriter.writeToc();
        lineWriter.writeLine("## Features");
        for (Feature feature : system.features()) {
            lineWriter.writeLine("""
                                                
                    ### Feature: %s""", feature.label(), feature.localTarget());
            for (Rule rule : feature.rules()) {
                lineWriter.writeLine("""

                        * Regel: WENN **%s** in %s [%s]""", rule.trigger().label(), rule.trigger().system().label(), timing(rule.trigger()));
                for (Event action : rule.actions()) {
                    lineWriter.writeLine("    * => DANN **%s** in %s [%s]", //
                            action.label(), //
                            wikiContext.wikiLink(action.system()), //
                            timing(action)    //
                    );
                }
                if (rule.comment() != null) {
                    lineWriter.writeLine("    * NOTE: *%s*", rule.comment());
                }
            }
        }
        lineWriter.writeSection("Eingehende API-Aufrufe");
        eventsToMarkdown(universe, system, wikiContext, Event::isSynchronous, lineWriter);

        lineWriter.writeSection("Eingehende Events");
        eventsToMarkdown(universe, system, wikiContext, Event::isAsynchronous, lineWriter);

        lineWriter.writeSection("Systemlandschaft");
        lineWriter.writeLine("* Aufrufe von: %s", universe.systemsCalling(system).map(wikiContext::wikiLink).collect(Collectors.joining(", ")));
        lineWriter.writeLine("* Aufrufe zu: %s", universe.systemsCalledFrom(system).map(wikiContext::wikiLink).collect(Collectors.joining(", ")));

        // system dependency map
        TreeMultimap<System, System> multiMap = TreeMultimap.create();
        universe.systemsCalling(system).forEach(source -> multiMap.put(source, system));
        universe.systemsCalledFrom(system).forEach(target -> multiMap.put(system, target));
        FlowchartDiagram flowchartDiagram = toMermaidFlowchart("Übersicht Aufrufe von Systemen", multiMap, s -> s.wikiName, System::label, false);
        mermaidDiagramBlock(flowchartDiagram, wikiContext.markdownCustomizer(), lineWriter);
    }

    private static String timing(Event event) {
        return event.isSynchronous() ? "synchron" : "asynchron";
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
