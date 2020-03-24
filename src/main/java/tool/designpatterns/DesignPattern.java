package tool.designpatterns;

/**
 * Instantiation can be done according to the following example. Using curly brackets to enter an
 * array.
 * <p>
 *
 * <pre>
 *      @ DesignPattern(pattern = {Pattern.IMMUTABLE, Pattern.SINGLETON})
 *      public class Design{}
 * </pre>
 */
public @interface DesignPattern {

    /**
     * List of Design patterns that are supposed to be present in the class this Annotation is
     * placed in.
     */
    Pattern[] pattern();
}
