package de.xam.featdoc.wiki;

import de.xam.featdoc.I18n;
import de.xam.featdoc.Term;
import de.xam.featdoc.markdown.IMarkdownCustomizer;

import java.io.File;

public interface IWikiContext {


    IMarkdownCustomizer markdownCustomizer();

    I18n i18n_de = new I18n(I18n.Language.de);

    default I18n i18n() {
        return i18n_de;
    }

    default String i18n(Term term) {
        return i18n().resolve(term);
    }

    default File markdownFile(IWikiFile wikiFile) {
        return new File(rootDir(), path(wikiFile) + ".md");
    }

    default String path(IWikiFile wikiFile) {
        return (rootPath() == null ? "" : rootPath() + "/") + wikiFile.wikiLink();
    }

    File rootDir();

    default String rootPath() {
        return "";
    }

    default String wikiLink(IWikiLink wikiLink) {
        return "[" + wikiLink.label() + "](" + path(wikiLink) + ")";
    }

}
