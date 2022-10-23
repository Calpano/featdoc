package de.xam.featdoc.wiki;

import de.xam.featdoc.I18n;
import org.jetbrains.annotations.Nullable;

public interface IWikiFile {

    String localTarget();

    /** null for root */
    @Nullable  String wikiFolder(I18n i18n);

    default String wikiLink(I18n i18n) {
        return (wikiFolder(i18n) == null ? "" : wikiFolder(i18n) + "/") + localTarget();
    }

}
