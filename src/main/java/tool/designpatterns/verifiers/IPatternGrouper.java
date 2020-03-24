package tool.designpatterns.verifiers;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import tool.designpatterns.Pattern;
import tool.feedback.PatternGroupFeedback;

/**
 * Verifies a pattern group.
 */
public interface IPatternGrouper {

    /**
     * Verifies all patterns of a given PatternGroup in the project.
     *
     * @param map the map to verify.
     *
     * @return the feedback of the verification.
     */
    PatternGroupFeedback verifyGroup(Map<Pattern, List<ClassOrInterfaceDeclaration>> map);
}
