package tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;

import org.gradle.api.GradleException;
import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternGroup;
import tool.designpatterns.PatternUtils;
import tool.feedback.PatternGroupFeedback;

/**
 * The main entry point for the analysis.
 */
@DesignPattern(pattern = {Pattern.IMMUTABLE})
public final class MainProgram {

    private MainProgram() {
    }

    /**
     * The main method.
     *
     * @param args commandline arguments.
     */
    public static void main(String[] args) {
        // Just use this project for now (src), will have to change
        // to the target project with the gradle stuff
        startAnalyse(new String[] {"src"});
    }

    /**
     * Main entrance point to the program.
     *
     * @param paths an array of paths to analyse.
     */
    @SuppressWarnings("PMD.SystemPrintln")
    public static void startAnalyse(String[] paths) {
        AnnotationExtractor extracter = new AnnotationExtractor();

        for (String path : paths) {
            List<ClassOrInterfaceDeclaration> annotationHolders =
                ProjectParser.findAllClassesAndInterfaces(path);
            extracter.findAnnotations(annotationHolders);
        }

        Map<Pattern, List<ClassOrInterfaceDeclaration>> patternAnnotMap =
            extracter.getPatternClassMap();

        ResolvedReferenceTypeDeclaration rcli = patternAnnotMap.get(Pattern.IMMUTABLE).get(0)
                                                               .resolve();
        System.out.println("RCLI: " + rcli.getClassName());

        Map<PatternGroup, Map<Pattern, List<ClassOrInterfaceDeclaration>>> patternGroupMap =
            mapToMap(patternAnnotMap);

        List<PatternGroupFeedback> feedbacks = new ArrayList<>();
        for (Map.Entry<PatternGroup, Map<Pattern, List<ClassOrInterfaceDeclaration>>> entry :
            patternGroupMap
            .entrySet()) {
            PatternGroup group = entry.getKey();
            Map<Pattern, List<ClassOrInterfaceDeclaration>> patternMap = entry.getValue();
            PatternGroupFeedback verFeedback = group.getVerifier().verifyGroup(patternMap);
            feedbacks.add(verFeedback);
        }

        List<String> failingFeedbacks = new ArrayList<>();
        feedbacks.forEach(feedback -> {
            if (feedback.hasError()) {
                failingFeedbacks.add(feedback.getFullMessage());
            }
        });

        if (!failingFeedbacks.isEmpty()) {
            failBuild(failingFeedbacks);
        }
    }

    /**
     * Fails the build and prints the failing feedbacks into a nice message.
     *
     * @param failingFeedbacks the feedbacks of what went wrong.
     */
    private static void failBuild(List<String> failingFeedbacks) {
        StringBuilder msg = new StringBuilder(100);
        msg.append("\n\nStaticAnalyser found the following errors: \n------------------\n");
        failingFeedbacks.forEach(feedback -> {
            msg.append(feedback);
            msg.append("------------------\n");
        });

        throw new GradleException(msg.toString());
    }

    /**
     * Groups a map from patterns to class or interface in their patternGroups, i.e. a map
     * containing the keys "AdapterClient, AdapterInterface, Immutable" to lists of classes or
     * interfaces will be converted to a map with keys "Adatpter, Immutable", with values same maps
     * as earlier.
     *
     * @param map The map to convert.
     *
     * @return The converted map.
     */
    private static Map<PatternGroup, Map<Pattern, List<ClassOrInterfaceDeclaration>>> mapToMap(
        Map<Pattern, List<ClassOrInterfaceDeclaration>> map) {
        Map<PatternGroup, Map<Pattern, List<ClassOrInterfaceDeclaration>>> newMap =
            new ConcurrentHashMap<>();
        map.forEach((pattern, list) -> {
            PatternGroup group = PatternUtils.patternGroupFromPattern(pattern);
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
