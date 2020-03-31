package tool.designpatterns;

/**
 * An enum that contains all patterns we are able to identify and verify. If a new pattern has been
 * added, then the enum must be expanded manually.
 */
public enum Pattern {
    IMMUTABLE(PatternGroup.IMMUTABLE),
    SINGLETON(PatternGroup.SINGLETON),

    COMPOSITE_COMPONENT(PatternGroup.COMPOSITE),
    COMPOSITE_NODES(PatternGroup.COMPOSITE),
    COMPOSITE_LEAF(PatternGroup.COMPOSITE);

    private PatternGroup group;

    Pattern(PatternGroup group) {
        this.group = group;
    }

    PatternGroup getGroup() {
        return this.group;
    }
}
