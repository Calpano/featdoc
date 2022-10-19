package de.xam.featdoc.markdown;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MarkdownTool {
    public static String filename(String title) {
        String enc = URLEncoder.encode(title, StandardCharsets.UTF_8);
        // re-decode some simple things which ARE allowed in filenames
        enc = enc.replace("+", "-");
        enc = enc.replace("%2C", ",");
        enc = enc.replace("%28", "(");
        enc = enc.replace("%29", ")");
        return enc;
    }

    public static String format(String in) {
        return in.replace("|", "<br/>");
    }

    public static String fragmentid(String s) {
        String enc = s.toLowerCase();
        enc = enc.replace(" ", "-");
        enc = URLEncoder.encode(enc, StandardCharsets.UTF_8);
        enc = enc.replace("+", "%2B");
        enc = enc.replace("%28", "(");
        enc = enc.replace("%29", ")");
        return enc;
    }

}
