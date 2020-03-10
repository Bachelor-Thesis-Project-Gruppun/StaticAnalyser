package base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javaparser.ast.CompilationUnit;

import patternverifiers.Feedback;

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
    //@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @SuppressWarnings("PMD.SystemPrintln")
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
        Map<PatternGroup, Map<Pattern, List<CompilationUnit>>> patternGroupMap = mapToMap(
            patternCompMap);

        List<Feedback> feedbacks = new ArrayList<>();
        for (Map.Entry<PatternGroup, Map<Pattern, List<CompilationUnit>>> entry : patternGroupMap
            .entrySet()) {
            PatternGroup group = entry.getKey();
            Map<Pattern, List<CompilationUnit>> patternMap = entry.getValue();
            Feedback verFeedback = group.getVerifier().verifyGroup(patternMap);
            feedbacks.add(verFeedback);
        }

        feedbacks.forEach(feedback -> {
            System.out.println(" - " + feedback.getMessage());
        });
    }

    /**
     * Groups a map from patterns to compilation units in their patternGroups, i.e. a map containing
     * the keys "AdapterClient, AdapterInterface, Immutable" to lists of compilation units will be
     * converted to a map with keys "Adatpter, Immutable", with values same maps as earlier.
     *
     * @param map The map to convert.
     *
     * @return The converted map.
     */
    private static Map<PatternGroup, Map<Pattern, List<CompilationUnit>>> mapToMap(
        Map<Pattern, List<CompilationUnit>> map) {
        Map<PatternGroup, Map<Pattern, List<CompilationUnit>>> newMap = new ConcurrentHashMap<>();
        map.forEach((pattern, list) -> {
            PatternGroup group = pattern.getGroup();
            if (!newMap.containsKey(group)) {
                // The group does not exist and we therefore want to create a new map.
                newMap.put(group, new HashMap<>());
            }

            // The group exists so we have an initialized map.
            newMap.get(group).put(pattern, list);
        });

        return newMap;
    }
}
