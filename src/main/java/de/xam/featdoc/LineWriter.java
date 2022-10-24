package de.xam.featdoc;

import de.xam.featdoc.markdown.MarkdownTool;

import java.io.IOException;
import java.io.Writer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface LineWriter {

    static LineWriter wrap(Writer w) {
        return (string, args) -> {
            try {
                if(args==null||args.length==0) {
                    // no replacements
                    w.write(string);
                } else {
                    String[] htmlifiedArgs = new String[args.length];
                    for (int i = 0; i < args.length; i++) {
                        htmlifiedArgs[i] = args[i].replace("|"," ");
                    }
                    w.write(String.format(string, (Object[]) htmlifiedArgs));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /** A default ## section */
    default void writeSection(String sectionTitle, String ...args) {
        writeLine("");
        writeLine("## %s", String.format(sectionTitle, args));
        writeLine("");
    }

    /** A main # section */
    default void writeSection1(String section1Title, String ... args) {
        writeLine("## %s", String.format(section1Title, args));
        writeLine("");
    }

    default void writeToc() {
        writeLine("");
        writeLine("[[_TOC_]]" );
        writeLine("");
    }

    /**
     * If no arguments are given, the string is written as-is.
     * With arguments, the string is used as a pattern and the arguments are the replacements, using String#format
     *
     * @param string
     * @param args
     */
    void write(String string, String... args);

    default void writeLine(String line, String... args) {
        write(line, args);
        write("\n");
    }

    default MarkdownTool.Table table() {
        return new MarkdownTool.Table(this);
    }

}
