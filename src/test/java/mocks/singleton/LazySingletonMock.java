package mocks.singleton;

/**
 * Mock class for the SingletonPattern, contains a private static field of LazySingletonMock, a
 * private constructor aswell as a public static method which calls the constructor.
 */
//@DesignPattern(pattern={Pattern.SINGLETON})
public class LazySingletonMock {

    //  Private Static field of LazySingletonMock, should pass first predicate
    private static LazySingletonMock instance;

    /**
     * Private constructor, should pass second predicate
     */
    private LazySingletonMock() {
        System.out.println("Constructor called");
    }

    /**
     * Private constructor, should pass second predicate
     */
    private LazySingletonMock(String s) {
        System.out.println("Constructor called with parameter " + s);
    }

    /**
     * private static method getInstance which calls the constructor, should pass third predicate if
     * called
     *
     * @return The Singleton instance of LazySingletonMock
     */
    public static LazySingletonMock getInstance() {
        if (instance == null) {
            instance = new LazySingletonMock();
        }
        return instance;
    }

}
