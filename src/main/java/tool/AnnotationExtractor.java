package tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;

/**
 * <p>A visitor that retrieves Java annotations and checks if a given annotation
 * indicates the presence of a design pattern. </p>
 * <p>If a pattern annotation is identified, then the visitor invokes the
 * corresponding pattern verifier.</p>
 */
@DesignPattern(pattern = {Pattern.IMMUTABLE})
class AnnotationExtractor {

    private final Map<Pattern, List<ClassOrInterfaceDeclaration>> patternClassMap;

    public AnnotationExtractor() {

        patternClassMap = new HashMap<>();
    }

    public Map<Pattern, List<ClassOrInterfaceDeclaration>> getPatternClassMap() {
        return this.patternClassMap;
    }

    /**
     * The method that is called for every annotation in the Class or Interface. Verifies the
     * implementation for every annotation marking a design pattern and prints the result to
     * system.out
     *
     * @param annotationHolders classes or intefaces that may have an Annotation.
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void findAnnotations(
        List<ClassOrInterfaceDeclaration> annotationHolders) {

        for (ClassOrInterfaceDeclaration annotationHolder : annotationHolders) {
            Optional<AnnotationExpr> annotation = annotationHolder.getAnnotationByName(
                "DesignPattern");
            if (annotation.isPresent()) {
                NormalAnnotationExpr annExpr = annotation.get().asNormalAnnotationExpr();
                for (Pattern pattern : getPatternsFromAnnotation(annExpr)) {
                    if (patternClassMap.containsKey(pattern)) {
                        patternClassMap.get(pattern).add(annotationHolder);
                    } else {
                        List<ClassOrInterfaceDeclaration> classes = new ArrayList<>();
                        classes.add(annotationHolder);
                        patternClassMap.put(pattern, classes);
                    }
                }
            }
        }
    }

    private List<Pattern> getPatternsFromAnnotation(NormalAnnotationExpr annotation) {
        NodeList<MemberValuePair> pairs = annotation.getPairs();
        List<Pattern> patterns = new ArrayList<>();

        for (MemberValuePair pair : pairs) {
            if (isPatternKey(pair)) {
                var patternsInPair = pair.getValue().asArrayInitializerExpr().getValues().toArray(
                    Expression[]::new);
                for (Expression expr : patternsInPair) {
                    for (Pattern pattern : Pattern.values()) {
                        if (isDesignPatternEnum(expr.toString(), pattern)) {
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

    private boolean isDesignPatternEnum(String parsedEnumName, Pattern pattern) {
        return parsedEnumName.equalsIgnoreCase("pattern." + pattern.toString());
    }

}
