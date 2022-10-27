package de.xam.featdoc.system;

import javax.annotation.Nullable;

public interface CauseAndEffect {

    @Nullable
    String comment();

    Message message();

    @Nullable Rule rule();

    System system();


}
