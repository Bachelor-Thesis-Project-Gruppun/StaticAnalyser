package base;

/**
 * An enum that contains all patterns we are able to identify and verify. If a new pattern has been
 * added, then the enum must be expanded manually.
 */
public enum Pattern {
    IMMUTABLE(PatternGroup.IMMUTABLE),
    SINGLETON(PatternGroup.SINGLETON);

    private PatternGroup group;

    Pattern(PatternGroup group) {
        this.group = group;
    }

    public PatternGroup getGroup() {
        return this.group;
    }
}
