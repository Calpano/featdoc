package de.xam.featdoc;

import javax.annotation.Nullable;
import java.util.Collection;

public class Util {
    public static <T> T add(Collection<T> coll, T element) {
        coll.add(element);
        return element;
    }

    public static @Nullable String combineStrings(@Nullable  String a, @Nullable  String b) {
        if (a == null) {
            if (b == null) {
                return null;
            } else {
                return b;
            }
        } else if (b == null) {
            return a;
        } else {
            return String.format("%s / %s", a, b);
        }
    }
}
