package base;

import java.util.Locale;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
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
        MarkerAnnotationExpr annotationExpr, Void args) {
        super.visit(annotationExpr, args);

        if (isPatternAnnotation(annotationExpr)) {
            Optional<CompilationUnit> optional =
                annotationExpr.findRootNode().findCompilationUnit();
            if (optional.isEmpty()) {
                return;
            }
            CompilationUnit compUnit = optional.get();

            Pattern pattern = Pattern.valueOf(
                annotationExpr.getNameAsString().toUpperCase(Locale.ENGLISH));

            PatternVerifierFactory.getVerifier(pattern).verify(compUnit);
        }
    }

    /**
     * Identifies whether or not a given annotation is associated with a known design pattern, which
     * and therefore indicates that said pattern should exist.
     *
     * @param ann the annotation to verify
     *
     * @return true if {@link Pattern} contains the given annotation
     */
    private boolean isPatternAnnotation(AnnotationExpr ann) {
        Pattern[] patterns = Pattern.class.getEnumConstants();
        for (Pattern p : patterns) {
            if (ann.getNameAsString().equalsIgnoreCase(p.toString())) {
                return true;
            }
        }
        return false;
    }
}
