package patternverifiers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;

import base.Pattern;

/**
 * A class that will verify a PatternGroup that only has one pattern (i.e. single class patterns)
 */
public class SingleClassVerifier implements IPatternGroupVerifier {

    private final transient IPatternVerifier verifier;

    /**
     * The constructor for this PatternGroup verifier.
     *
     * @param verifier the verifier to use when verifying classes.
     */
    public SingleClassVerifier(IPatternVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public Feedback verifyGroup(Map<Pattern, List<CompilationUnit>> map) {
        Iterator<Pattern> itr1 = map.keySet().iterator();
        Pattern pattern = itr1.hasNext() ? itr1.next() : null;

        Iterator<List<CompilationUnit>> itr2 = map.values().iterator();
        List<CompilationUnit> compUnits = itr2.hasNext() ? itr2.next() : null;

        Feedback validMapFeedback = validateMap(map, pattern, compUnits);

        if (!validMapFeedback.getValue()) {
            throw new IllegalArgumentException(validMapFeedback.getMessage());
        }

        boolean verifySuccesful = true;

        StringBuilder baseMsg = new StringBuilder("Verification of pattern ");
        baseMsg.append(pattern.toString());
        StringBuilder message = new StringBuilder();

        for (CompilationUnit compUnit : compUnits) {
            Feedback feedback = verifier.verify(compUnit);
            if (!feedback.getValue()) {
                verifySuccesful = false;
                message.append('\n');
                message.append(feedback.getMessage());
            }
        }

        if (verifySuccesful) {
            baseMsg.append(" was successful");
        } else {
            baseMsg.append(" failed due to");
            baseMsg.append(message);
        }
        return new Feedback(verifySuccesful, baseMsg.toString());
    }

    /**
     * Validates if the map is valid for this class.
     *
     * @param map the map to validate.
     *
     * @return the result.
     */
    private Feedback validateMap(
        Map<Pattern, List<CompilationUnit>> map, Pattern pattern, List<CompilationUnit> compUnits) {
        // Assumes that map only has one entry.
        final int maxMapLength = 1;

        if (map.size() != maxMapLength) {
            return new Feedback(false, "Only allows verification of PatternGroups containing " +
                                       "exactly 1 pattern.");
        }

        if (compUnits == null && pattern == null) {
            return new Feedback(false, "Invalid map provided");
        }

        return new Feedback(true);
    }
}
