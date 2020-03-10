package base;

import patternverifiers.AdapterVerifier;
import patternverifiers.IPatternGroupVerifier;
import patternverifiers.ImmutableVerifier;
import patternverifiers.SingleClassVerifier;
import patternverifiers.SingletonVerifier;

/**
 * A design pattern covering all parts of that pattern..
 */
public enum PatternGroup {
    SINGLETON(new SingleClassVerifier(new SingletonVerifier())),
    IMMUTABLE(new SingleClassVerifier(new ImmutableVerifier())),
    ADAPTER(new AdapterVerifier());

    private IPatternGroupVerifier verifier;

    PatternGroup(IPatternGroupVerifier groupVerifier) {
        verifier = groupVerifier;
    }

    IPatternGroupVerifier getVerifier() {
        return verifier;
    }
}
