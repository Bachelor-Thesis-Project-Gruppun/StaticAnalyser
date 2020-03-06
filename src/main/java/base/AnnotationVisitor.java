package base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

/**
 * <p>A visitor that retrieves Java annotations and checks if a given annotation
 * indicates the presence of a design pattern. </p>
 * <p>If a pattern annotation is identified, then the visitor invokes the
 * corresponding pattern verifier.</p>
 */
class AnnotationVisitor extends VoidVisitorAdapter<Void> {

    private final Map<Pattern, List<CompilationUnit>> patternCompMap;

    public AnnotationVisitor() {
        super();

        patternCompMap = new HashMap<>();
    }

    /**
     * The method that is called for every annotation in the compilationUnit. Verifies the
     * implementation for every annotation marking a design pattern and prints the result to
     * system.out
     *
     * @param annotationExpr The annotation
     * @param args           Required by visit, not used in this instance
     */

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void visit(
        NormalAnnotationExpr annotationExpr, Void args) {
        super.visit(annotationExpr, args);

        Optional<CompilationUnit> optional = annotationExpr.findRootNode().findCompilationUnit();
        if (optional.isEmpty()) {
            return;
        }

        CompilationUnit compilationUnit = optional.get();

        for (Pattern pattern : getPatternsFromAnnotation(annotationExpr)) {
            if (patternCompMap.containsKey(pattern)) {
                patternCompMap.get(pattern).add(compilationUnit);
            } else {
                List<CompilationUnit> compUnits = new ArrayList<>();
                compUnits.add(compilationUnit);
                patternCompMap.put(pattern, compUnits);
            }
        }
    }

    private List<Pattern> getPatternsFromAnnotation(NormalAnnotationExpr annotation) {
        NodeList<MemberValuePair> pairs = annotation.getPairs();
        List<Pattern> patterns = new ArrayList<>();

        for (int i = 0; i < pairs.size(); i++) {
            MemberValuePair pair = pairs.get(i);
            if (isPatternKey(pair)) {
                var patternsInPair = pair.getValue().asArrayInitializerExpr().getValues().toArray();
                for (int j = 0; j < patternsInPair.length; j++) {
                    for (Pattern pattern : Pattern.values()) {
                        if (isDesignPatternEnum(
                            patternsInPair[i].toString(), pattern, "pattern.")) {
                            patterns.add(pattern);
                        }
                    }
                }

            }
        }

        return patterns;
    }

    private boolean isPatternKey(MemberValuePair pair) {
        return pair.getName().asString().equalsIgnoreCase("pattern");
    }

    private boolean isDesignPatternEnum(String parsedEnumName, Pattern pattern, String prefix) {
        return parsedEnumName.equalsIgnoreCase(prefix + pattern.toString());
    }

    public Map<Pattern, List<CompilationUnit>> getPatternCompMap() {
        return this.patternCompMap;
    }
}
