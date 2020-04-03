package tool.designpatterns;

/**
 * An enum that contains all patterns we are able to identify and verify. If a new pattern has been
 * added, then the enum must be expanded manually.
 */
public enum Pattern {
    // Single class patterns
    IMMUTABLE(PatternGroup.IMMUTABLE),
    SINGLETON(PatternGroup.SINGLETON),

    // Proxy
    PROXY_INTERFACE(PatternGroup.PROXY),
    PROXY_SUBJECT(PatternGroup.PROXY),
    PROXY_PROXY(PatternGroup.PROXY),

    // Adapter
    ADAPTER_ADAPTER(PatternGroup.ADAPTER),
    ADAPTER_ADAPTEE(PatternGroup.ADAPTER);

    private PatternGroup group;

    Pattern(PatternGroup group) {
        this.group = group;
    }

    PatternGroup getGroup() {
        return this.group;
    }
}
