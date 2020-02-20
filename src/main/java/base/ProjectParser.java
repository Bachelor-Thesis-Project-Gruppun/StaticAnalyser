package base;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.SourceRoot;

@Nonnull
public class ProjectParser {

    private String sourceRoot;
    private List<CompilationUnit> compilationUnits;

    public ProjectParser(String sourceRoot) {
        this.sourceRoot = sourceRoot;

        compilationUnits = projectToAST();
    }

    private List<CompilationUnit> projectToAST() {
        Path pathToSource = Paths.get(sourceRoot);
        SourceRoot sourceRoot = new SourceRoot(pathToSource);
        try {
            sourceRoot.tryToParse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sourceRoot.getCompilationUnits();
    }

    public void verify() {
        for (CompilationUnit cu : compilationUnits) {
            cu.accept(new AnnotationGetter(), null);
        }
    }

    private static class AnnotationGetter extends VoidVisitorAdapter<Void> {

        @Override
        public void visit(
            MarkerAnnotationExpr annotationExpr, Void list) {
            super.visit(annotationExpr, list);

            // All of this is very rough, but it is something to start with
            if (isCustomAnnotation(annotationExpr)) {
                CompilationUnit cu =
                    annotationExpr.findRootNode().findCompilationUnit().get();
                System.out.println(
                    "Class: " + cu.getStorage().get().getFileName() +
                    "\nTested patterns:\n" + annotationExpr.getNameAsString() +
                    ": " + PatternVerifierFactory.getVerifier(Pattern.valueOf(
                        annotationExpr.getNameAsString().toUpperCase()))
                                                 .verify(cu));
            }

        }

        private boolean isCustomAnnotation(AnnotationExpr ann) {
            return ann.getNameAsString().equals("Singleton") ||
                   ann.getNameAsString().equals("Immutable");
        }
    }

}


