package tool.designpatterns;

/**
 * An enum that contains all patterns we are able to identify and verify.
 */
public enum Pattern {
    // Single class patterns.
    IMMUTABLE(PatternGroup.IMMUTABLE),
    SINGLETON(PatternGroup.SINGLETON),

    // Decorator
    DECORATOR_INTERFACE_COMPONENT(PatternGroup.DECORATOR),
    DECORATOR_CONCRETE_COMPONENT(PatternGroup.DECORATOR),
    DECORATOR_ABSTRACT_DECORATOR(PatternGroup.DECORATOR),
    DECORATOR_CONCRETE_DECORATOR(PatternGroup.DECORATOR),

    // Proxy
    PROXY_INTERFACE(PatternGroup.PROXY),
    PROXY_SUBJECT(PatternGroup.PROXY),
    PROXY_PROXY(PatternGroup.PROXY),

    // Adapter
    ADAPTER_ADAPTER(PatternGroup.ADAPTER),
    ADAPTER_ADAPTEE(PatternGroup.ADAPTER),

    // Composite
    COMPOSITE_COMPONENT(PatternGroup.COMPOSITE),
    COMPOSITE_CONTAINER(PatternGroup.COMPOSITE),
    COMPOSITE_LEAF(PatternGroup.COMPOSITE);

    private PatternGroup group;

    Pattern(PatternGroup group) {
        this.group = group;
    }

    PatternGroup getGroup() {
        return this.group;
    }
}
