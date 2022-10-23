package de.xam.featdoc.markdown;

import de.xam.featdoc.LineWriter;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringTreeTest {

    @Test
    void test() {
        StringTree tree = new StringTree("root");
        tree.addChild("-> [Foo] Aufruf").addChild("-> [Bar] Folgeaufruf").addChild("-> [Baz] weitere Folge");
        StringTree b = tree.addChild("-> [B] brancht auf");
        b.addChild("-> [C] Archiv");
        b.addChild("-> [D] passiert auch noch");
        StringWriter sw = new StringWriter();
        tree.toMarkdownList(LineWriter.wrap(sw));
        String expect ="* root\n" +
                "    * -> [Foo] Aufruf\n" +
                "        * -> [Bar] Folgeaufruf\n" +
                "            * -> [Baz] weitere Folge\n" +
                "    * -> [B] brancht auf\n" +
                "        * -> [C] Archiv\n" +
                "        * -> [D] passiert auch noch\n";
        assertEquals(expect, sw.getBuffer().toString());
    }

}