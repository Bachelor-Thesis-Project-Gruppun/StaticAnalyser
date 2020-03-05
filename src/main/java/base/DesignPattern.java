package base;

/**
 * Instantiation can be done according to the following example. Using curly brackets to enter an
 * array.
 *
 * @ DesignPattern(pattern = {DesignEnum.IMMUTABLE, DesignEnum.SINGLETON}) public class
 *     Design{}
 */
public @interface DesignPattern {

    /**
     * List of Design patterns that are supposed to be present in the class this Annotation is
     * placed in.
     */
    Pattern[] pattern();
}
