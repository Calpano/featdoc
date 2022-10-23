package de.xam.featdoc;

public class I18n {

    public enum Language {
        en, de;
    }

    private final Language language;

    public I18n(Language language) {
        this.language = language;
    }

    public String resolve(Term term) {
        return term.in(language);
    }

}
