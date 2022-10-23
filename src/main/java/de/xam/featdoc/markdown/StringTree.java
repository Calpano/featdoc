package de.xam.featdoc.markdown;

import com.google.common.collect.ListMultimap;
import de.xam.featdoc.LineWriter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StringTree implements Comparable<StringTree> {

    private static final String INDENT = "    ";
    private final @NonNull String label;
    private final List<StringTree> children = new ArrayList<>();

    public StringTree(@NonNull final String label) {
        this.label = label;
    }

    /**
     * Takes O(2n) space with n=number of pairs; Runs in O(n)
     *
     * @param pairs
     * @return a tree of depth n using all (source,target) pairs to construct the tree
     */
    public static StringTree toTree(final ListMultimap<String, String> pairs) {
        final Map<String, StringTree> map = new HashMap<>();

        final Set<String> roots = new HashSet<>();
        roots.addAll(pairs.keySet());
        pairs.forEach((s, t) -> {
            roots.remove(t);
            // get or create
            final StringTree sTree = map.computeIfAbsent(s, k -> new StringTree(s));
            // merge in tTree if present
            map.compute(t, (k, v) -> {
                if (v == null) {
                    return sTree.addChild(t);
                } else {
                    // there was already a tree, re-use it
                    sTree.addChildTree(v);
                    return v;
                }
            });
        });
        if (roots.isEmpty()) {
            return new StringTree("-EMPTY-");
        }
        if (roots.size() == 1) {
            return map.get(roots.iterator().next());
        }
        // several roots, create synthetic root
        final StringTree result = new StringTree("-TREE-");
        for (final String root : roots) {
            final StringTree tree = map.get(root);
            result.addChildTree(tree);
        }
        return result;
    }

    /**
     * @param label
     * @return the child
     */
    public StringTree addChild(@NonNull final String label) {
        final StringTree child = new StringTree(label);
        return addChildTree(child);
    }

    /**
     * @param child
     * @return the child
     */
    public StringTree addChildTree(final StringTree child) {
        this.children.add(child);
        return child;
    }

    public int compareTo(final StringTree o) {
        return getLabel().compareTo(o.getLabel());
    }

    public void dump() {
        dump("");
    }

    public Iterator<StringTree> getChildNodesIterator() {
        return this.children.iterator();
    }

    public Iterator<StringTree> getChildNodesIterator(final StringTree node) {
        return node.getChildNodesIterator();
    }

    public String getLabel() {
        return this.label;
    }

    public StringTree getOrCreateChild(@NonNull final String s) {
        for (final StringTree child : this.children) {
            if (child.getLabel().equals(s)) {
                return child;
            }
        }
        return addChild(s);
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public void sort() {
        Collections.sort(this.children);
        this.children.forEach((c) -> c.sort());
    }

    public void toMarkdownList(LineWriter lineWriter) {
        toMarkdownList(0, '*', lineWriter);
    }

    public static void toMarkdownList(List<StringTree> trees, LineWriter lineWriter) {
        for (StringTree tree : trees) {
            tree.toMarkdownList(0, '*', lineWriter);
        }
    }

    public String toString() {
        return this.label + "(" + this.children.size() + ")";
    }

    private void dump(final String indent) {
        System.out.println(indent + this.label + " hash=" + hashCode());
        for (final StringTree child : this.children) {
            child.dump(indent + "--");
        }
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

    private void toMarkdownList(int depth, char marker, LineWriter lineWriter) {
        String pre = "";
        for (int i = 0; i < depth; i++) {
            pre += INDENT;
        }
//        if (isJustAChain()) {
//            // render subtree as chain
//            lineWriter.write("%s%s %s", pre, "" + marker, label);
//            children.forEach(child -> lineWriter.write(" %s", child.label));
//            lineWriter.writeLine("");
//        } else {
        // render subtree as tree
        lineWriter.writeLine("%s%s %s", pre, "" + marker, label);
        children.forEach(child -> child.toMarkdownList(depth + 1, marker, lineWriter));
//        }
    }

}
