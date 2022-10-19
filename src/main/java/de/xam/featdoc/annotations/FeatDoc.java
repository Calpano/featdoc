package de.xam.featdoc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FeatDocs.class)
public @interface FeatDoc {

    String name();
    Sequence sequence() default @Sequence(steps = {});

}

/*

According to Oracle the valid types for annotation elements are:

1. Primitives (byte, char, int, long float, double)
2. Enums
3. Class (Think generics here Class <?>, Class<? extends/super T>>)
4. String
5. Array of the above (array[] of primitives, enums, String, or Class)
5. Another annotation.

All are public & abstract
 */