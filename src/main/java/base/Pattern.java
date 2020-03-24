package base;

/**
 * An enum that contains all patterns we are able to identify and verify. If a new pattern has been
 * added, then the enum must be expanded manually.
 */
public enum Pattern {

    IMMUTABLE(PatternGroup.IMMUTABLE), SINGLETON(PatternGroup.SINGLETON),
    DECORATOR_INTERFACE_COMPONENT(PatternGroup.DECORATOR), DECORATOR_CONCRETE_COMPONENT(
        PatternGroup.DECORATOR), DECORATOR_ABSTRACT_DECORATOR(PatternGroup.DECORATOR),
    DECORATOR_CONCRETE_DECORATOR(PatternGroup.DECORATOR);

    private PatternGroup group;

    Pattern(PatternGroup group) {
        this.group = group;
    }

    PatternGroup getGroup() {
        return this.group;
    }

}
