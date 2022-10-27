package de.xam.featdoc.markdown;

import java.util.Optional;

public interface IMarkdownCustomizer {

    default MermaidBlockSyle mermaidBlockSyle() {
        return MermaidBlockSyle.Default;
    }

    default Optional<String> preamble() {
        return Optional.empty();
    }

}