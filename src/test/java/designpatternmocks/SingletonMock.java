package designpatternmocks;

/**
 * Mock class for the SingletonPattern, contains a private static field of SingletonMock, a private
 * constructor aswell as a private static method which calls the constructor.
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
    public static SingletonMock getInstance() {
        if (instance == null) {
            instance = new SingletonMock();
        }
        return instance;
    }

}
