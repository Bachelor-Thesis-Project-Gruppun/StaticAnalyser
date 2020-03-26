package tool.designpatterns;

/**
 * An enum that contains all patterns we are able to identify and verify. If a new pattern has been
 * added, then the enum must be expanded manually.
 */
public enum Pattern {
    IMMUTABLE(PatternGroup.IMMUTABLE),
    SINGLETON(PatternGroup.SINGLETON),
    PROXY_INTERFACE(PatternGroup.PROXY),
    PROXY_SUBJECT(PatternGroup.PROXY),
    PROXY_PROXY(PatternGroup.PROXY);

    private PatternGroup group;

    Pattern(PatternGroup group) {
        this.group = group;
    }

    PatternGroup getGroup() {
        return this.group;
    }
}
