package de.xam.featdoc;

import de.xam.featdoc.annotations.FeatDoc;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * TODO escape characters
 */
public class FeatDocAnnotationTool {

    private static final Logger log = getLogger(FeatDocAnnotationTool.class);

    public static void scan(String[] packageRoots) {
        try (ScanResult scanResult =                // Assign scanResult in try-with-resources
                     new ClassGraph()                    // Create a new ClassGraph instance
                             //.verbose()                      // If you want to enable logging to stderr
                             .enableAllInfo()                // Scan classes, methods, fields, annotations
                             .acceptPackages(packageRoots)      // Scan com.xyz and subpackages
                             .ignoreClassVisibility().ignoreFieldVisibility().ignoreMethodVisibility().scan()) {                      // Perform the scan and return a ScanResult
            // Use the ScanResult within the try block, e.g.
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(FeatDoc.class)) {
                AnnotationInfo annotationInfo = classInfo.getAnnotationInfo(FeatDoc.class);
                FeatDoc featDoc = (FeatDoc) annotationInfo.loadClassAndInstantiate();
                String name = featDoc.name();
                log.info("C feature name " + name + " -- defined in " + classInfo.getName());
            }
            for (ClassInfo classInfo : scanResult.getClassesWithMethodAnnotation(FeatDoc.class)) {
                //   log.info("Class {} has a method with FeatDoc", classInfo.getName());
                for (MethodInfo mi : classInfo.getMethodInfo()) {
                    //     log.info("mi="+mi);
                    AnnotationInfo annotationInfo = mi.getAnnotationInfo(FeatDoc.class);
                    if (annotationInfo != null) {
                        FeatDoc featDoc = (FeatDoc) annotationInfo.loadClassAndInstantiate();
                        String name = featDoc.name();
                        log.info("M feature name " + name + " -- defined in " + classInfo.getName() + "." + mi.getName());
                    }
                }
            }
            for (ClassInfo classInfo : scanResult.getClassesWithFieldAnnotation(FeatDoc.class)) {
                // log.info("Class {} has a field with FeatDoc", classInfo.getName());
                for (FieldInfo fi : classInfo.getFieldInfo()) {
                    AnnotationInfo annotationInfo = fi.getAnnotationInfo(FeatDoc.class);
                    if (annotationInfo != null) {
                        FeatDoc featDoc = (FeatDoc) annotationInfo.loadClassAndInstantiate();
                        String name = featDoc.name();
                        log.info("M feature name " + name + " -- defined in " + classInfo.getName() + "." + fi.getName());
                    }
                }
            }
        }
    }
}
