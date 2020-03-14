package designpatternmocks.singletonpattern;

/**
 * Mock class for the SingletonPattern, contains a private static field of SingletonMock, two
 * private constructors aswell as a private static method which calls the constructor. the
 * getInstance method is called from other methods public static method.
 */
//@DesignPattern(pattern={Pattern.SINGLETON})
public class SingletonMockMultipleConstructors {

    //  Private Static field of SingletonMock, should pass first predicate
    private static SingletonMockMultipleConstructors instance;

    /**
     * Private constructor, should pass second predicate
     */
    private SingletonMockMultipleConstructors() {
        System.out.println("Constructor called");
    }

    private SingletonMockMultipleConstructors(String unusedString) {
        System.out.println("Overloaded constructor called!");
    }

    /**
     * private static method getInstance which calls the constructor twice, should fail third
     * predicate if called. Calls constructor in else statement which should fail it.
     *
     * @return The Singleton instance of SingletonMock
     */
    private static SingletonMockMultipleConstructors getInstance() {
        if (instance == null) {
            instance = new SingletonMockMultipleConstructors();
        }
        return instance;
    }

    /**
     * Calls getInstance, should help passing the third predicate
     *
     * @return The Singleton instance of SingletonMock
     */
    private static SingletonMockMultipleConstructors callGetInstance() {
        return getInstance();
    }

    /**
     * Test method used to test out nested calls from private methods
     *
     * @return
     */
    private static SingletonMockMultipleConstructors callCallGetInstance() {
        return callGetInstance();
    }

    /**
     * Test method used to test out nested calls from private methods
     *
     * @return
     */
    public static SingletonMockMultipleConstructors callCallCallGetInstance() {
        return callCallGetInstance();
    }
}
