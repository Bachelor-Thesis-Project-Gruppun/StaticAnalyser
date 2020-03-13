package base;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;

import patternverifiers.DecoratorVerifier;

/**
 * The main entry point for the analysis.
 */
public final class MainProgram {

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
        startAnalyse(new String[] {"src"});
        List<CompilationUnit> compUnits = ProjectParser.projectToAst(
            "src/test/java" + "/patternimplementors" + "/decorator");
        List<CompilationUnit> testUnits = new ArrayList<>();
        testUnits.add(compUnits.get(0));
        testUnits.add(compUnits.get(1));
        testUnits.add(compUnits.get(3));
        DecoratorVerifier verifier = new DecoratorVerifier(compUnits.get(2)/*, testUnits*/);
        verifier.verify(compUnits.get(0));
        // DEBUG BELOW
        for (CompilationUnit compUnit : testUnits) {
            //     System.out.println(verifier.verify(compUnit) + " for compUnit: " +
            //                        compUnit.getPrimaryTypeName().get());
            verifier.verify(compUnit);
        }
    }

    /**
     * Main entrance point to the program.
     *
     * @param paths an array of paths to analyse.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public static void startAnalyse(String[] paths) {
        for (String path : paths) {
            List<CompilationUnit> cus = ProjectParser.projectToAst(path);
            for (CompilationUnit cu : cus) {
                cu.accept(new AnnotationVisitor(), null);
            }
        }
    }
}
