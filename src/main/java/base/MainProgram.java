package base;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

public final class MainProgram {

    private MainProgram() {
    }

    public static void main(String[] args) {
        //Just use this project for now (src), will have to change
        //to the target project with the gradle stuff
        List<CompilationUnit> cus = ProjectParser.projectToAst("src");
        for (CompilationUnit cu : cus) {
            cu.accept(new AnnotationVisitor(), null);
        }
    }
}
