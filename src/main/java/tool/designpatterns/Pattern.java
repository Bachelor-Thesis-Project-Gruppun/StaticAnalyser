package tool.designpatterns;

/**
 * An enum that contains all patterns we are able to identify and verify. If a new pattern has been
 * added, then the enum must be expanded manually.
 */
public enum Pattern {

    IMMUTABLE(PatternGroup.IMMUTABLE),
    SINGLETON(PatternGroup.SINGLETON),

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
