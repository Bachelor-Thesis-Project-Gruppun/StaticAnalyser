package base;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import patternverifiers.PatternVerifierFactory;

/**
 * <p>A visitor that retrieves Java annotations and checks if a given annotation
 * indicates the presence of a design pattern. </p>
 * <p>If a pattern annotation is identified, then the visitor invokes the
 * corresponding pattern verifier.</p>
 */
class AnnotationVisitor extends VoidVisitorAdapter<Void> {

    public AnnotationVisitor() {
        super();
    }

    /**
     * The method that is called for every annotation in the compilationUnit. Verifies the
     * implementation for every annotation marking a design pattern and prints the result to
     * system.out
     *
     * @param annotationExpr The annotation
     * @param args           Required by visit, not used in this instance
     */
    @Override
    public void visit(
        NormalAnnotationExpr annotationExpr, Void args) {
        super.visit(annotationExpr, args);

        Optional<CompilationUnit> optional =
            annotationExpr.findRootNode().findCompilationUnit();
        if (optional.isEmpty()) {
            return;
        }

        CompilationUnit cu = optional.get();

        for (Pattern pattern : getPatternsFromAnnotation(annotationExpr)) {

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
                "File: " + fileName + "\nTested patterns:\n" + pattern + ": " +
                validPattern);
        }
    }


    private List<Pattern> getPatternsFromAnnotation(NormalAnnotationExpr annotation) {
        NodeList<MemberValuePair> pairs = annotation.getPairs();
        List<Pattern> patterns = new ArrayList<Pattern>();

        for (int i = 0; i < pairs.size() ; i++) {
            MemberValuePair pair = pairs.get(i);
            if (isPatternKey(pair)){
                var p = pair.getValue().asArrayInitializerExpr().getValues().toArray();
                for (int j = 0; j < p.length; j++) {
                    for (Pattern pattern:Pattern.values()) {
                        if (isDesignPatternEnum(p[i].toString(), pattern,
                                                "pattern.")) {
                            patterns.add(pattern);
                        }
                    }
                }

            }
        }

        return patterns;
    }

    private boolean isPatternKey(MemberValuePair pair){
        return pair.getName().asString().equalsIgnoreCase("pattern");
    }


    private boolean isDesignPatternEnum(String s, Pattern p, String prefix){
        return s.equalsIgnoreCase(prefix + p.toString());
    }

    private boolean isDesignPatternEnum(String s, Pattern p){
        return isDesignPatternEnum(s, p, "");
    }


}
