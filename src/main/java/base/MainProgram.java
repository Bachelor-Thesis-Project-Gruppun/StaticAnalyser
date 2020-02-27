package base;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

public final class MainProgram {
    // private int i = 0;       // Used for debugging VariableReader
    // private static String str = "0";     // Used for debugging VariableReader
    private MainProgram() {
    }

    public static void main(String[] args) {
        //Just use this project for now (src), will have to change
        //to the target project with the gradle stuff
        // List<CompilationUnit> cus = ProjectParser.projectToAst("src");
        // VariableReader v = new VariableReader();
        // System.out.println(v.readVariables(cus.get(1)));
        startAnalyse(new String[]{"src"});
    }

    /**
     * Main entrance point to the program.
     * @param paths an array of paths to analyse.
     */
    public static void startAnalyse(String[] paths) {
        for (String path : paths) {
            List<CompilationUnit> cus = ProjectParser.projectToAst(path);
            for (CompilationUnit cu : cus) {
                cu.accept(new AnnotationVisitor(), null);
            }
        }
    }
}
