package tool.designpatterns;

/**
 * Class to provide utilities for pattern enum outside of this package.
 */

@DesignPattern(pattern = {Pattern.IMMUTABLE})
public final class PatternUtils {

    private PatternUtils() {

    }

    /**
     * Retrieve the PatternGroup that a specific Pattern belongs to.
     *
     * @param pattern the Pattern to get the group for.
     *
     * @return the PatternGroup.
     */
    public static PatternGroup patternGroupFromPattern(Pattern pattern) {
        return pattern.getGroup();
    }
}
