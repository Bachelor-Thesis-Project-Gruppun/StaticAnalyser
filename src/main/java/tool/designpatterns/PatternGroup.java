package tool.designpatterns;

import tool.designpatterns.verifiers.IPatternGrouper;
import tool.designpatterns.verifiers.multiclassverifiers.adapter.AdapterVerifier;
import tool.designpatterns.verifiers.multiclassverifiers.decorator.DecoratorVerifier;
import tool.designpatterns.verifiers.multiclassverifiers.proxy.ProxyVerifier;
import tool.designpatterns.verifiers.singleclassverifiers.SingleClassGrouper;
import tool.designpatterns.verifiers.singleclassverifiers.immutable.ImmutableVerifier;
import tool.designpatterns.verifiers.singleclassverifiers.singleton.SingletonVerifier;

/**
 * A design pattern covering all parts of that pattern..
 */
public enum PatternGroup {
    SINGLETON(new SingleClassGrouper(new SingletonVerifier())),
    IMMUTABLE(new SingleClassGrouper(new ImmutableVerifier())),
    DECORATOR(new DecoratorVerifier()),
    PROXY(new ProxyVerifier()),
    ADAPTER(new AdapterVerifier());

    private IPatternGrouper verifier;

    PatternGroup(IPatternGrouper groupVerifier) {
        verifier = groupVerifier;
    }

    public IPatternGrouper getVerifier() {
        return verifier;
    }
}
