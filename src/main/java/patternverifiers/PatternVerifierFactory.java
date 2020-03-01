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
     * @param p the pattern to verify
     *
     * @return the corresponding verifier for the given pattern
     */
    public static IPatternVerifier getVerifier(Pattern p) {
        switch (p) {
            case SINGLETON:
                return new SingletonVerifier();
            case IMMUTABLE:
                return new ImmutableVerifier();
            default:
                throw new IllegalStateException();
        }
    }
}
