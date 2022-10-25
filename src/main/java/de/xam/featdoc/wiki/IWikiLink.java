package de.xam.featdoc.wiki;

import de.xam.featdoc.markdown.MarkdownTool;

public interface IWikiLink extends IWikiFile {

    String label();

    /**
     * local part
     */
    default String localTarget() {
        return MarkdownTool.fragmentId(label());
    }


}
