package tool.designpatterns.verifiers.singleclassverifiers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;

import tool.designpatterns.DesignPattern;
import tool.designpatterns.Pattern;
import tool.designpatterns.PatternUtils;
import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.IPatternVerifier;
import tool.feedback.Feedback;
import tool.feedback.PatternGroupFeedback;

/**
 * A class that will verify a PatternGroup that only has one pattern (i.e. single class patterns)
 */
@DesignPattern(pattern = {Pattern.IMMUTABLE})
public class SingleClassGrouper implements IPatternGrouper {

    private final IPatternVerifier verifier;

    /**
     * The constructor for this PatternGroup verifier.
     *
     * @param verifier the verifier to use when verifying classes.
     */
    public SingleClassGrouper(IPatternVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public PatternGroupFeedback verifyGroup(Map<Pattern, List<CompilationUnit>> map) {
        Iterator<Pattern> itr1 = map.keySet().iterator();
        Pattern pattern = itr1.hasNext() ? itr1.next() : null;

        Iterator<List<CompilationUnit>> itr2 = map.values().iterator();
        List<CompilationUnit> compUnits = itr2.hasNext() ? itr2.next() : null;

        if (!validateMap(map, pattern, compUnits)) {
            throw new IllegalArgumentException("Validation of map failed");
        }

        List<Feedback> childFeedbacks = new ArrayList<>();
        for (CompilationUnit compUnit : compUnits) {
            childFeedbacks.add(verifier.verify(compUnit));
        }

        return new PatternGroupFeedback(
            PatternUtils.patternGroupFromPattern(pattern), childFeedbacks);
    }

    /**
     * Validates if the map is valid for this class.
     *
     * @param map the map to validate.
     *
     * @return the result.
     */
    private boolean validateMap(
        Map<Pattern, List<CompilationUnit>> map, Pattern pattern, List<CompilationUnit> compUnits)
        throws IllegalArgumentException {
        // Assumes that map only has one entry.
        final int maxMapLength = 1;

        if (map.size() != maxMapLength) {
            throw new IllegalArgumentException(
                "Only allows verification of PatternGroups containing exactly 1 pattern.");
        }

        if (compUnits == null && pattern == null) {
            throw new IllegalArgumentException("Invalid map provided");
        }

        return true;
    }
}
