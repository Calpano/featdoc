package de.xam.featdoc.system;

import java.util.ArrayList;
import java.util.List;

import static de.xam.featdoc.Util.add;

public class Condition {

    /**
     * @param label
     * @param probability does not need to add up to 1, is auto-normalized.
     *                    Just use '1' everywhere for equal probability
     */
    public record Variant(Condition condition, String label, double probability) {
    }

    private final String label;
    private List<Variant> variants = new ArrayList<>();

    public Condition(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /**
     * First variant is always the main/happy case
     */
    public Variant variant(String label) {
        return add(variants, new Variant(this, label, 1.0));
    }

    /**
     * First variant is always the main/happy case
     */
    public Variant variant(String label, double probability) {
        return add(variants, new Variant(this, label, probability));
    }
}
