package base;

import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

class AnnotationVisitor extends VoidVisitorAdapter<Void> {

    @Override
    public void visit(
        MarkerAnnotationExpr annotationExpr, Void list) {
        super.visit(annotationExpr, list);

        if (isPatternAnnotation(annotationExpr)) {
            Optional<CompilationUnit> optional =
                annotationExpr.findRootNode().findCompilationUnit();
            if (optional.isEmpty()) {
                return;
            }
            CompilationUnit cu = optional.get();

            Pattern pattern = Pattern.valueOf(
                annotationExpr.getNameAsString().toUpperCase());

            boolean validPattern = PatternVerifierFactory.getVerifier(pattern)
                                                         .verify(cu);

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
    }

    private boolean isPatternAnnotation(AnnotationExpr ann) {
        Pattern[] patterns = Pattern.class.getEnumConstants();
        for (Pattern p : patterns) {
            if (ann.getNameAsString().toUpperCase().equals(p.toString())) {
                return true;
            }
        }
        return false;
    }
}
