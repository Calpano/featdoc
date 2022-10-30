package de.xam.featdoc.system;

import javax.annotation.Nullable;

public record TerminalEffect(@Nullable Rule rule, Message message, @Nullable String comment,
                             System system) implements Effect {

    public static TerminalEffect of(Cause cause) {
        return new TerminalEffect(cause.rule(), cause.message(), null, cause.message().system());
    }

}
