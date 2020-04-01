package mocks.singleton;

/**
 * Mock class for the SingletonPattern, contains a private static field of
 * SingletonMockMultipleAccessMethods, a private constructor aswell as a public static method which
 * calls the constructor. Contains a public method which simply returns a new instance, thus should
 * fail tests.
 */
//@DesignPattern(pattern={Pattern.SINGLETON})
public class SingletonMockMultipleAccessMethods {

    //  Private Static field of SingletonMockMultipleAccessMethods, should pass first predicate
    private static SingletonMockMultipleAccessMethods instance;

    /**
     * Private constructor, should pass second predicate
     */
    private SingletonMockMultipleAccessMethods() {
        System.out.println("Constructor called");
    }

    /**
     * private static method getInstance which calls the constructor, should pass third predicate if
     * called
     *
     * @return The Singleton instance of SingletonMockMultipleAccessMethods
     */
    public static SingletonMockMultipleAccessMethods getInstance() {
        if (instance == null) {
            instance = new SingletonMockMultipleAccessMethods();
        }
        return instance;
    }

    /**
     * Public static method that returns a new instance of the class, should fail tests.
     *
     * @return A new instance of this SingletonMock class
     */
    public static SingletonMockMultipleAccessMethods getNewInstance() {
        return new SingletonMockMultipleAccessMethods();
    }

}
