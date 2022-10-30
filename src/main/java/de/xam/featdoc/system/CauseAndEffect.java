package de.xam.featdoc.system;

import javax.annotation.Nullable;

public interface CauseAndEffect {

    @Nullable
    String comment();

    default boolean hasComment() {
        return comment()!=null;
    }

    Message message();

    @Nullable Rule rule();

    System system();


}
