package patternverifiers;

import base.PatternGroup;

/**
 * Factory for creating Verifiers.
 */
public final class VerifierFactory {

    private VerifierFactory() {
    }

    /**
     * Factory method for IPatternGroupVerifiers.
     * @param verifierType the type of the desired verifier
     */
    public static IPatternGroupVerifier createGroupVerifier(PatternGroup verifierType) {

        switch (verifierType) {
            case IMMUTABLE:
                return new SingleClassVerifier(new ImmutableVerifier());
            case SINGLETON:
                return new SingleClassVerifier(new SingletonVerifier());
            default:
                throw new IllegalArgumentException(
                    "The avaliable pattergroups and the factory " + "does not match.");
        }
    }
}
