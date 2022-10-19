package de.xam.featdoc;

import java.util.Collection;

public class Util {
    public static <T> T add(Collection<T> coll, T element) {
        coll.add(element);
        return element;
    }
}
