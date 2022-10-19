package de.xam.featdoc.annotations.sample;

import de.xam.featdoc.FeatDocAnnotationTool;

public class RunMe {

    public static void main(String[] args) {
        String[] packageRoots = new String[]{"de.xam"};
        FeatDocAnnotationTool.scan(packageRoots);
    }
}
