package base;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

import patternverifiers.SingletonVerifier;

/**
 * The main entry point for the analysis.
 */
public final class MainProgram {

    // private int i = 0;       // Used for debugging VariableReader
    private static String str = "0";     // Used for debugging VariableReader
    private static MainProgram mp = new MainProgram();

    private MainProgram() {
    }

    /**
     * The main method.
     *
     * @param args commandline arguments.
     */
    public static void main(String[] args) {
        //Just use this project for now (src), will have to change
        //to the target project with the gradle stuff
        List<CompilationUnit> compUnits = ProjectParser.projectToAst("src/test");
        //System.out.println(VariableReader.readVariables(compUnits.get(1)));
        SingletonVerifier verifier = new SingletonVerifier();
        System.out.println("Passing Mock: " + verifier.verify(compUnits.get(2)));
        System.out.println(
            "Passing Mock with multiple constructors: " + verifier.verify(compUnits.get(0)));
        System.out.println("Failing Mock: " + verifier.verify(compUnits.get(1)));
        startAnalyse(new String[] {"src"});
    }

    /**
     * Main entrance point to the program.
     *
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

    public class InnerClass {

    }
}
