package designpatternmocks.singletonpattern;

/**
 * Mock class for the SingletonPattern, contains a private static field of EagerSingletonMock, a
 * private constructor aswell as a public static method returning the instancevariable.
 */
//@DesignPattern(pattern={Pattern.SINGLETON})
public class EagerSingletonMock {

    //  Private Static field of SingletonMock, should pass first predicate
    private static EagerSingletonMock instance = new EagerSingletonMock();

    /**
     * Private constructor, should pass second predicate
     */
    private EagerSingletonMock() {
        System.out.println("Constructor called");
    }

    /**
     * private static method getInstance which returns the instance, should pass tests
     *
     * @return The Singleton instance of SingletonMock
     */
    public static EagerSingletonMock getInstance() {
        return instance;
    }

}
