package de.xam.featdoc.markdown;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IMarkdownCustomizer {

    default MermaidBlockSyle mermaidBlockSyle() {
        return MermaidBlockSyle.Default;
    }

    default Optional<String> preamble() {
        return Optional.empty();
    }

}