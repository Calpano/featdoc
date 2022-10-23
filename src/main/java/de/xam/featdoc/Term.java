package de.xam.featdoc;

import java.util.EnumMap;

public enum Term {
    asynchronous("asynchronous","asynchron"),
    callFrom("Call from","Aufruf von"),
    callsFrom("Incoming Calls from","Aufrufe von" ),
    callsTo("Outgoing Calls to", "Aufrufe zu" ),
    feature("Feature", "Feature"),
    features("Features","Features"),
    footer("Generated on %s by **featdoc**", "Erzeugt am %s von **featdoc**"),
    if_("IF","WENN"),
    incomingApiCalls("Incoming Synchronous API Calls","Eingehende API-Aufrufe" ),
    incomingAsyncEvents("Incoming Asynchronous Events", "Eingehende Events"),
    overviewCallsFromSystems("Overview Of Calls From Other Systems", "Übersicht Aufrufe von Systemen"),
    rule("Rule","Regel"),
    scenario("Scenario", "Szenario"),
    scenarioSteps("Scenario Steps","Szenario-Schritte" ),
    scenarioTree("Scenario Tree","Szenario Baum" ),
    scenarios("Scenarios", "Szenarien"),
    sequenceDiagram("Sequence Diagram","Sequenz-Diagramm" ),
    synchronous("synchronous","synchron"),
    system("System","System" ),
    systemLandscape("System Landscape","Systemlandschaft" ),
    systems("Systems", "Systeme"),
    systemsOverview("Overview of Systems calling each other","Übersicht Aufrufe von Systemen" ),
    then_("THEN","DANN"),
    legend("Legend","Legende" );

    private EnumMap<I18n.Language, String> map = new EnumMap<>(I18n.Language.class);

    Term(String en, String de) {
        map.put(I18n.Language.en, en);
        map.put(I18n.Language.de, de);
    }

    public String in(I18n.Language language) {
        return map.get(language);
    }
}
