package base;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;

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
    @DesignPattern(pattern = {Pattern.IMMUTABLE})
    public static void main(String[] args) {
        //Just use this project for now (src), will have to change
        //to the target project with the gradle stuff
        startAnalyse(new String[] {"src"});
    }

    /**
     * Main entrance point to the program.
     *
     * @param paths an array of paths to analyse.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @DesignPattern(pattern = {Pattern.SINGLETON})
    public static void startAnalyse(String[] paths) {
        AnnotationVisitor visitor = new AnnotationVisitor();
        for (String path : paths) {
            List<CompilationUnit> cus = ProjectParser.projectToAst(path);
            for (CompilationUnit cu : cus) {
                cu.accept(visitor, null);
            }
        }
        Map<Pattern, List<CompilationUnit>> patternCompMap = visitor.getPatternCompMap();
        System.out.println(patternCompMap.toString());
    }
}
