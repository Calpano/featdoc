package de.xam.featdoc;

import de.xam.featdoc.markdown.StringTree;
import de.xam.featdoc.system.Cause;
import de.xam.featdoc.system.CauseAndEffect;
import de.xam.featdoc.system.Effect;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CausalTree {

    private final CauseAndEffect causeAndEffect;
    private final List<CausalTree> children = new ArrayList<>();

    private CausalTree(CauseAndEffect causeAndEffect) {
        this.causeAndEffect = causeAndEffect;
    }

    public static CausalTree create(Cause cause) {
        return new CausalTree(cause);
    }

    private static void dump(CausalTree causalTree, StringTree stringTree) {
        causalTree.getChildNodesIterator().forEachRemaining(causalChild -> {
            StringTree stringChild = stringTree.addChild("" +
                    causalChild.getCauseAndEffect().system()+" {"+
                    causalChild.getCauseAndEffect().message().system()+"."+
                    causalChild.getCauseAndEffect().message().name()+"-"+
                    causalChild.getCauseAndEffect().message().direction()+"}"
            );
            dump(causalChild, stringChild);
        });
    }

    public CausalTree addCause(@NonNull final Cause cause) {
        final CausalTree child = new CausalTree(cause);
        return addChildTree(child);
    }

    public CausalTree addEffect(@NonNull final Effect effect) {
        final CausalTree child = new CausalTree(effect);
        return addChildTree(child);
    }

    public void dump() {
        StringTree stringTree = new StringTree("CausalTree on " + this.causeAndEffect);
        dump(this, stringTree);
        stringTree.dump();
    }

    public CauseAndEffect getCauseAndEffect() {
        return this.causeAndEffect;
    }

    public Iterator<CausalTree> getChildNodesIterator() {
        return this.children.iterator();
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public String toString() {
        return this.causeAndEffect + "(" + this.children.size() + ")";
    }

    private CausalTree addChildTree(final CausalTree child) {
        this.children.add(child);
        return child;
    }

    private boolean isJustAChain() {
        switch (children.size()) {
            case 0:
                return true;
            case 1:
                return children.get(0).isJustAChain();
            default:
                return false;
        }
    }

}
