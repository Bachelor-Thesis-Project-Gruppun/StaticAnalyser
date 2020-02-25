package patternverifiers;

import base.Pattern;

public final class PatternVerifierFactory {

    private PatternVerifierFactory() {
    }

    public static IPatternVerifier getVerifier(Pattern p) {
        switch (p) {
            case SINGLETON:
                return new SingletonVerifier();
            default:
                throw new IllegalStateException();
        }
    }
}
