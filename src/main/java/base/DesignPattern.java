package base;

/**
 * Instantiation can be done according to the following example.
 * Using curly brackets to enter an array.
 *
 *   @ DesignPattern(pattern = {DesignEnum.IMMUTABLE, DesignEnum.SINGLETON})
 *   public class Design{
 *
 *   }
 */
public @interface DesignPattern {
        Pattern[] pattern();
}
