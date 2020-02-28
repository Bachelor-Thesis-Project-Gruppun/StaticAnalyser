package base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import patternverifiers.PatternVerifierFactory;

/**
 * <p>A visitor that retrieves Java annotations and checks if a given annotation
 * indicates the presence of
 * a design pattern. </p>
 * <p>If a pattern annotation is identified, then the visitor invokes the
 * corresponding pattern verifier.</p>
 */
class AnnotationVisitor extends VoidVisitorAdapter<Void> {

    public AnnotationVisitor() {
        super();
    }

    /**
     * The method that is called for every annotation in the compilationUnit.
     * Verifies the implementation for every annotation marking a design pattern
     * and prints the result to system.out
     *
     * @param annotationExpr The annotation
     * @param args           Required by visit, not used in this instance
     */
    @Override
    public void visit(
        NormalAnnotationExpr annotationExpr, Void args) {
        super.visit(annotationExpr, args);

//        if (isPatternAnnotation(annotationExpr)) {
            Optional<CompilationUnit> optional =
                annotationExpr.findRootNode().findCompilationUnit();
            if (optional.isEmpty()) {
                return;
            }
            CompilationUnit cu = optional.get();

//            Pattern pattern = Pattern.valueOf(
//                annotationExpr.getNameAsString().toUpperCase(Locale.ENGLISH));

        for (Pattern pattern: getSingletonPattern(annotationExpr)) {

            boolean validPattern = PatternVerifierFactory.getVerifier(pattern)
                                                         .verify(cu);
            System.out.println(pattern);
            System.out.println(validPattern);
            // Everything below this is just to show something, will probably
            // not be here in the future
            String fileName;
            if (cu.getStorage().isEmpty()) {
                fileName = "File name not found";
            } else {
                fileName = cu.getStorage().get().getFileName();
            }

            System.out.println(
                "File: " + fileName + "\nTested patterns:\n" + pattern + ": "
                + validPattern);
        }

//        }
    }

//    /**
//     * Identifies whether or not a given annotation is associated with a known
//     * design pattern, which and therefore indicates that said pattern should
//     * exist.
//     *
//     * @param ann the annotation to verify
//     *
//     * @return true if {@link Pattern} contains the given annotation
//     */
//    private boolean isPatternAnnotation(NormalAnnotationExpr ann) {
//        System.out.println();
//        List<Pattern> annPatt = getSingletonPattern(ann);
//        Pattern[] patterns = Pattern.values();
//        for (Pattern pattern : annPatt) {
//
//            for (Pattern p : patterns) {
//                if (pattern.toString().equalsIgnoreCase(p.toString())) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private List<Pattern> getSingletonPattern(NormalAnnotationExpr annotation) {
        NodeList<MemberValuePair> pairs = annotation.getPairs();

        List<Pattern> patterns = new ArrayList<Pattern>();

        for (int i = 0; i < pairs.size() ; i++) {
            MemberValuePair pair = pairs.get(i);
            if (pair.getName().asString().equalsIgnoreCase("pattern")){
                var p =
                    pair.getValue().asArrayInitializerExpr().getValues().toArray(); ;
                for (int j = 0; j < p.length; j++) {
//                    System.out.println(p[i].toString());
//                    System.out.println(Pattern.SINGLETON);

                    for (Pattern pattern:Pattern.values()) {
                        if (p[i].toString().equalsIgnoreCase("pattern." + pattern.toString())) {
                            System.out.println("ddfdddd");
                            patterns.add(pattern);
                        }
                    }
                }

            }
            System.out.println();
        }

        System.out.println();
        return patterns;
    }
}
