package patternverifiers;

import base.Pattern;

/**
 * A static factory for constructing pattern verifiers.
 */
public final class PatternVerifierFactory {

    private PatternVerifierFactory() {
    }

    /**
     * Returns the corresponding verifier to the enum.
     *
     * @param pattern the pattern to verify
     *
     * @return the corresponding verifier for the given pattern
     */
    @SuppressWarnings("PMD.TooFewBranchesForASwitchStatement")
    public static IPatternVerifier getVerifier(Pattern pattern) {
        switch (pattern) {
            case SINGLETON:
                return new SingletonVerifier();
            case IMMUTABLE:
                return new ImmutableVerifier();
            default:
                throw new IllegalStateException();
        }
    }
}
