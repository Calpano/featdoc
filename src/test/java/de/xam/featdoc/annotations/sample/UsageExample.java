package de.xam.featdoc.annotations.sample;

import de.xam.featdoc.annotations.FeatDoc;
import de.xam.featdoc.annotations.Sequence;
import de.xam.featdoc.annotations.Step;

@FeatDoc(name = "class-level")
public class UsageExample {


    @FeatDoc(name = "validation4")
    static String b;
    @FeatDoc(name = "validation3")
    String a;

    @FeatDoc(name = "validation2")
    @FeatDoc(name = "validation3")
    public void onStatusChanged() {
        // inform push system
        // update status in core system
    }

    @FeatDoc(name = "validation1", sequence = @Sequence(steps = {@Step(from = "A", to = "B", arrow = "foo")}))
    boolean isValid(String customerId) {
        return true;
    }

    @FeatDoc(name = "contact-copy", sequence = @Sequence(steps = {@Step(from = "A", to = "B", arrow = "foo")}))
    void loadContactFromCrm(String customerId) {

    }


}