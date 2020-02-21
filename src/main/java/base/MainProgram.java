package base;

import java.io.IOException;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

public final class MainProgram {

    public static void main(String[] args) throws IOException {
        //Just use this project for now (src), will have to change
        //to the target project with the gradle stuff
        List<CompilationUnit> cus = ProjectParser.projectToAst("src");
        for (CompilationUnit cu : cus) {
            cu.accept(new AnnotationVisitor(), null);
        }
    }
}
