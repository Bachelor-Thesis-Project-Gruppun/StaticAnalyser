package designpatternmocks;

/**
 * Mock class for the SingletonPattern, contains a private static field of SingletonMock, a private
 * constructor aswell as a private static method which calls the constructor. the getInstance method
 * is called from a public static method.
 */
//@DesignPattern(pattern={Pattern.SINGLETON})
public class SingletonMock {

    //  Private Static field of SingletonMock, should pass first predicate
    private static SingletonMock instance;

    /**
     * Private constructor, should pass second predicate
     */
    private SingletonMock() {
        System.out.println("Constructor called");
    }

    /**
     * private static method getInstance which calls the constructor, should pass third predicate if
     * called
     *
     * @return The Singleton instance of SingletonMock
     */
    private static SingletonMock getInstance() {
        if (instance == null) {
            instance = new SingletonMock();
        }
        return instance;
    }

    /**
     * Calls getInstance, should help passing the third predicate
     *
     * @return The Singleton instance of SingletonMock
     */
    private static SingletonMock callGetInstance() {
        return getInstance();
    }

    /**
     * Test method used to test out nested calls from private methods
     *
     * @return
     */
    private static SingletonMock callCallGetInstance() {
        return callGetInstance();
    }

    /**
     * Test method used to test out nested calls from private methods
     *
     * @return
     */
    public static SingletonMock callCallCallGetInstance() {
        return callCallGetInstance();
    }
}
