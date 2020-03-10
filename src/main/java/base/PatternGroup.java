package base;

import patternverifiers.IPatternGroupVerifier;
import patternverifiers.ImmutableVerifier;
import patternverifiers.SingleClassVerifier;
import patternverifiers.SingletonVerifier;

/**
 * A design pattern covering all parts of that pattern..
 */
public enum PatternGroup {
    SINGLETON(new SingleClassVerifier(new SingletonVerifier())),
    IMMUTABLE(new SingleClassVerifier(new ImmutableVerifier()));

    private IPatternGroupVerifier verifier;

    PatternGroup(IPatternGroupVerifier groupVerifier) {
        verifier = groupVerifier;
    }

    IPatternGroupVerifier getVerifier() {
        return verifier;
    }
}
