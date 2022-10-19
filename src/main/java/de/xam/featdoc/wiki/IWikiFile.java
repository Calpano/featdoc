package de.xam.featdoc.wiki;

import de.xam.featdoc.markdown.MarkdownTool;
import org.jetbrains.annotations.Nullable;

public interface IWikiFile {

    String localTarget();

    /** null for root */
    @Nullable  String wikiFolder();

    default String wikiLink() {
        return (wikiFolder() == null ? "" : wikiFolder() + "/") + localTarget();
    }

}
