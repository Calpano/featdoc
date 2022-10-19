package de.xam.featdoc;

import java.io.IOException;
import java.io.Writer;

public interface LineWriter {

    static LineWriter wrap(Writer w) {
        return (string, args) -> {

            String[] htmlifiedArgs = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                htmlifiedArgs[i] = args[i].replace("|"," ");
            }

            try {
                w.write(String.format(string, htmlifiedArgs));
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
        writeLine("[[_TOC_]]" );
    }

    /**
     * Using String#format
     *
     * @param string
     * @param args
     */
    void write(String string, String... args);

    default void writeLine(String line, String... args) {
        write(line, args);
        write("\n");
    }

}
