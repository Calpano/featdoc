package de.xam.featdoc.system;

import javax.annotation.Nullable;

public interface Effect extends CauseAndEffect {


    default Cause asCause() {
        return new Cause() {
            @Nullable
            @Override
            public String comment() {
                return Effect.this.comment();
            }

            @Nullable
            @Override
            public Message message() {
                return Effect.this.message();
            }

            @Nullable
            @Override
            public Rule rule() {
                return Effect.this.rule();
            }

            @Override
            public System system() {
                return Effect.this.system();
            }
        };
    }
}
