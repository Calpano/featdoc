package de.xam.featdoc.markdown;

import de.xam.featdoc.LineWriter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MarkdownTool {

    public static class Table {

        final LineWriter lineWriter;
        private int cols = -1;

        public Table(LineWriter lineWriter) {
            this.lineWriter = lineWriter;
        }

        public Table headerSeparator() {
            lineWriter.writeLine("|" + IntStream.range(0, cols).mapToObj(i -> "---").collect(Collectors.joining("|")) + "|");
            return this;
        }

        public Table row(String... args) {
            if(cols > 0 && cols!=args.length)
                throw new IllegalArgumentException("Table rows must have some length");
            lineWriter.writeLine("|" +
                Stream.of(args)
                    .map(MarkdownTool::escapePipes)
                    .collect(Collectors.joining("|")) + "|"
            );
            this.cols = args.length;
            return this;
        }


    }

    public static String escapePipes(String s) {
        if(s==null)
            return null;
        return s.replace("|", "\\|");
    }

    private static final String ALLOWED_CHARS_IN_AZURE_WIKI_FILENAMES = ",()öäüÖÄÜ";

    public static String filename(String title) {
        String enc = URLEncoder.encode(title, StandardCharsets.UTF_8);
        // re-decode some simple things which ARE allowed in filenames
        enc = enc.replace("+", "-");
        enc = urlReDecode(ALLOWED_CHARS_IN_AZURE_WIKI_FILENAMES, enc);
        return enc;
    }

    private static String urlReDecode(String charactesWhichShouldNotHaveBeenEncoded, String enc) {
        Map<String,Character> redecodeMap = new HashMap<>();
        charactesWhichShouldNotHaveBeenEncoded.chars().forEach(c -> {
            redecodeMap.put(URLEncoder.encode( Character.toString(c), StandardCharsets.UTF_8) , Character.valueOf((char) c));
        });
        String result = enc;
        for( Map.Entry<String, Character> entry : redecodeMap.entrySet() ) {
            result = result.replace(entry.getKey(), ""+entry.getValue());
        }
        return result;
    }

    public static String format(String in) {
        return in.replace("|", "<br/>");
    }

    public static String fragmentId(String s) {
        String enc = s.toLowerCase();
        enc = enc.replace(" ", "-");
        enc = URLEncoder.encode(enc, StandardCharsets.UTF_8);
        enc = enc.replace("+", "%2B");
        enc = enc.replace("%28", "(");
        enc = enc.replace("%29", ")");
        return enc;
    }

}
