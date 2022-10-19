package de.xam.featdoc.wiki;

import de.xam.featdoc.markdown.IMarkdownCustomizer;

import java.io.File;

public interface IWikiContext {


    IMarkdownCustomizer markdownCustomizer();

    default File markdownFile(IWikiFile wikiFile) {
        return new File(rootDir(), path(wikiFile) + ".md");
    }

    ;

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
