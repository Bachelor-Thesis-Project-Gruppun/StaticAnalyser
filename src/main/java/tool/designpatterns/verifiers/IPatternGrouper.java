package tool.designpatterns.verifiers;

import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;

import tool.designpatterns.Pattern;
import tool.util.Feedback;

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
    Feedback verifyGroup(Map<Pattern, List<CompilationUnit>> map);
}
