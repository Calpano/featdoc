package de.xam.featdoc.example;

import de.xam.featdoc.I18n;
import de.xam.featdoc.markdown.IMarkdownCustomizer;
import de.xam.featdoc.markdown.MermaidBlockSyle;
import de.xam.featdoc.wiki.IWikiContext;

import java.io.File;
import java.util.Optional;

public class CafeWiki {

    public static IMarkdownCustomizer customMarkdown() {
        return new IMarkdownCustomizer() {
            @Override
            public MermaidBlockSyle mermaidBlockSyle() {
                return MermaidBlockSyle.Microsoft;
            }

            @Override
            public Optional<String> preamble() {
                return Optional.of("""
                        This page was auto-generated from a technical model.
                        """);
            }
        };
    }

    public static IWikiContext wikiContext() {
        // IMPROVE paths from config/env-var
        return new IWikiContext() {

            I18n i18n = new I18n(I18n.Language.en);

            @Override
            public I18n i18n() {
                return i18n;
            }

            @Override
            public IMarkdownCustomizer markdownCustomizer() {
                return customMarkdown();
            }

            // @devs: this is a first version, such developer-specific config belongs to env vars, sure
            @Override
            public File rootDir() {
                return new File("C:\\_data_\\_p_\\_git\\GitHub\\Coffee-Shop.wiki");
            }

            @Override
            public String rootPath() {
                return "/FeatDoc";
            }

        };
    }

}
