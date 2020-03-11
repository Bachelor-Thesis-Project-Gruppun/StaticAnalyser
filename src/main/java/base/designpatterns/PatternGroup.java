package base.designpatterns;

import base.designpatterns.verifiers.IPatternGroupVerifier;
import base.designpatterns.verifiers.singleclassverifiers.ImmutableVerifier;
import base.designpatterns.verifiers.SingleClassVerifier;
import base.designpatterns.verifiers.singleclassverifiers.SingletonVerifier;

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

    public IPatternGroupVerifier getVerifier() {
        return verifier;
    }
}
