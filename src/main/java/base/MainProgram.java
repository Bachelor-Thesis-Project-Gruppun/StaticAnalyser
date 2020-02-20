package base;

import java.io.IOException;

import com.github.javaparser.ast.CompilationUnit;

public final class MainProgram {

    public static void main(String[] args) throws IOException {
        ProjectParser pp = new ProjectParser(
            "src"); // Just use this project for
        // now, will have to change to the target project with the gradle stuff

        for (CompilationUnit cu : pp.getCompilationUnits()) {
            cu.accept(new AnnotationVisitor(), null);
        }
    }
}
